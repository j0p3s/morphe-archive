#!/usr/bin/env python3
"""python3 app_request.py <issue-number> <issue-body-file> <requests.json> <patches-list.json> [bundles.json]"""

import json
import os
import re
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path
from urllib.parse import parse_qs, urlparse

sys.path.insert(0, str(Path(__file__).parent))
from fields import field, normalize, parse_fields

MARKER = "<!-- app-request-bot -->"
REPO = os.environ.get("GITHUB_REPOSITORY", "")
TITLE_PREFIX = re.compile(r"^\s*(?:\[[^\]]{0,30}\]|app\s*request\b)\s*[:|\-\u2013\u2014\uff1a]?\s*", re.I)
MAX_TITLE = 256
URL = re.compile(r"https?://\S+")
MARKDOWN_LINK = re.compile(r"\[([^\[\]]*)\]\([^)]*\)")
REPO_PATH = re.compile(r"[\w.-]+/[\w.-]+")
PACKAGE = re.compile(r"\b[A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z0-9_]+)+\b")
DOMAIN_ENDINGS = {"com", "net", "org", "io", "ph", "me", "app", "co", "in", "tv", "apk", "xyz", "to"}
NAME_NOISE = {"app", "android", "apk", "for", "the"}
STORE_QUERY = {"play.google.com": "id", "h5-api.aoneroom.com": "p"}
MAX_LISTED = 5
QUIET_DAYS = 14


def is_package(text):
    parts = text.split(".")
    return bool(PACKAGE.fullmatch(text)) and not (len(parts) == 2 and parts[1].lower() in DOMAIN_ENDINGS)


def store_package(url):
    try:
        parts = urlparse(url)
    except ValueError:
        return ""
    value = parse_qs(parts.query).get(STORE_QUERY.get(parts.hostname), [""])[0]
    linked = PACKAGE.match(value)
    return linked.group(0) if linked else ""


def find_package(body):
    fields = parse_fields(body)
    declared = field(fields, "Package name", "Package").strip().strip("`").strip()
    if is_package(declared):
        return declared
    return next(filter(None, map(store_package, URL.findall(field(fields, "App")))), "")


def comparable_name(raw):
    raw = re.split(r"[,(:]| - ", URL.sub(" ", raw), maxsplit=1)[0]
    return "".join(w for w in map(normalize, re.findall(r"\w+", raw)) if w not in NAME_NOISE)


def app_name(body, title=""):
    return comparable_name(field(parse_fields(body), "App") or TITLE_PREFIX.sub("", title))


def shown_name(body):
    raw = URL.sub(" ", MARKDOWN_LINK.sub(r"\1", field(parse_fields(body), "App")))
    return next(iter(raw.splitlines()), "").strip(" \t,;:|`-\u2013\u2014")


def canonical_title(title, body):
    rest = TITLE_PREFIX.sub("", title).strip() or shown_name(body) or find_package(body)
    return f"[App Request]: {rest}"[:MAX_TITLE] if rest else title


def supported_apps(patches_list):
    return {
        package["packageName"].lower(): package["name"]
        for patch in patches_list["patches"]
        for package in patch.get("compatiblePackages") or []
    }


def discussed_recently(issue, now):
    cutoff = now - timedelta(days=QUIET_DAYS)
    return any(
        (c.get("author") or {}).get("login") != "github-actions"
        and datetime.fromisoformat(c["createdAt"].replace("Z", "+00:00")) > cutoff
        for c in issue.get("comments") or []
    )


def link(issue):
    return f"#{issue['number']}"


def listed_bundles(registry, package):
    return sorted(
        (b for b in registry["bundles"]
         if isinstance(b, dict) and REPO_PATH.fullmatch(str(b.get("repo"))) and b["repo"].lower() != REPO.lower()
         and isinstance(b.get("targetApps"), list) and package in b["targetApps"]),
        key=lambda b: -b["stars"] if isinstance(b.get("stars"), int) else 0,
    )


def bundle_name(bundle):
    return re.sub(r"[^A-Za-z0-9 .'+&-]", "", str(bundle.get("name") or ""))[:60].strip() or bundle["repo"]


def decide(number, body, requests, patches_list, registry=None, title="", now=None):
    package = find_package(body)
    name = app_name(body, title)
    supported = supported_apps(patches_list)
    earlier = sorted((r for r in requests if r["number"] < number), key=lambda r: r["number"])
    same_package = [r for r in earlier if package and find_package(r["body"] or "").lower() == package.lower()]
    open_same = [r for r in same_package if r["state"] == "OPEN"]
    verdict, reason, duplicate_of, labels, lines = "pass", "", 0, [], []

    if package and package.lower() in supported:
        verdict, reason = "close", "completed"
        lines.append(
            f"The package `{package}` is already supported as {supported[package.lower()]}."
        )
    elif open_same:
        canonical = open_same[0]
        verdict, reason, duplicate_of = "close", "duplicate", canonical["number"]
        lines.append(
            f"This duplicates issue {link(canonical)}, which already requests `{package}`."
        )
    elif same_package:
        prior = same_package[-1]
        verdict, labels = "flag", ["duplicate"]
        lines.append(
            f"Closed issue {link(prior)} already requested `{package}`."
        )

    this = next((r for r in requests if r["number"] == number), {})
    if verdict == "close" and discussed_recently(this, now or datetime.now(timezone.utc)):
        verdict, reason, duplicate_of, labels, lines = "hold", "", 0, [], []

    if verdict == "pass" and name:
        lookalikes = [r for r in earlier if r["state"] == "OPEN" and r not in same_package
                      and app_name(r["body"] or "", r.get("title", "")) == name]
        lookalikes += [{"number": 0, "name": n} for p, n in supported.items()
                       if comparable_name(n) == name and p != (package or "").lower()]
        if lookalikes:
            verdict = "flag"
            if "duplicate" not in labels:
                labels.append("duplicate")
            refs = ", ".join(f"{r['name']} (already supported)" if not r["number"] else link(r) for r in lookalikes)
            lines.append(
                f"This may be the same app as {refs}, but the package name differs or is missing."
            )

    bundles = []
    if package and verdict in ("pass", "flag") and registry:
        bundles = listed_bundles(registry, package)
        if bundles:
            host = {"github": "https://github.com", "gitlab": "https://gitlab.com"}
            rows = [
                f"- [{bundle_name(b)}]({host.get(b.get('source'), host['github'])}/{b['repo']})"
                for b in bundles[:MAX_LISTED]
            ]
            if len(bundles) > MAX_LISTED:
                rows.append(f"- {len(bundles) - MAX_LISTED} more bundles")
            if verdict == "pass":
                verdict = "flag"
            if "needs info" not in labels:
                labels.append("needs info")
            if len(bundles) == 1:
                lines.append(
                    f"{rows[0][2:]} already patches `{package}`. Which of the requested features does it not cover?"
                )
            else:
                lines.append(
                    f"These bundles already patch `{package}`:\n\n" + "\n".join(rows)
                    + "\n\nWhich of the requested features do they not cover?"
                )

    return {
        "package": package,
        "verdict": verdict,
        "reason": reason,
        "duplicate_of": duplicate_of,
        "labels": labels,
        "matches": [b["repo"] for b in bundles],
        "title": canonical_title(title, body),
        "comment": MARKER + "\n" + "\n\n".join(lines) if lines else "",
    }


if __name__ == "__main__":
    number = int(sys.argv[1])
    body = Path(sys.argv[2]).read_text(encoding="utf-8")
    requests = json.loads(Path(sys.argv[3]).read_text(encoding="utf-8"))
    patches_list = json.loads(Path(sys.argv[4]).read_text(encoding="utf-8"))
    registry_path = Path(sys.argv[5]) if len(sys.argv) > 5 else None
    registry = None
    if registry_path and registry_path.is_file():
        try:
            registry = json.loads(registry_path.read_text(encoding="utf-8"))
        except ValueError:
            pass
    if not (isinstance(registry, dict) and isinstance(registry.get("bundles"), list)):
        registry = None
    title = next((r.get("title", "") for r in requests if r["number"] == number), "")
    print(json.dumps(decide(number, body, requests, patches_list, registry, title)))
