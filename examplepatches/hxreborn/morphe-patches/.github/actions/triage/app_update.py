#!/usr/bin/env python3
"""python3 app_update.py <issue-number> <issue-body-file> <updates.json> <patches-list.json>"""

import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
sys.path.insert(0, str(Path(__file__).parents[2] / "scripts"))
from fields import field, normalize, parse_fields
from versions import compare, version_key

MARKER = "<!-- app-update-bot -->"
TITLE_PREFIX = "[App Update]: "
VERSION_CODE = re.compile(r"\s*\(\d+\)\s*$")


def targets(patches_list):
    apps = {}
    for patch in patches_list["patches"]:
        for package in patch.get("compatiblePackages") or []:
            versions = apps.setdefault(package["name"], set())
            versions.update(t["version"] for t in package.get("targets") or [] if t.get("version"))
    return apps


def requested_version(body):
    raw = field(parse_fields(body), "New version", "Version").strip().strip("`")
    raw = VERSION_CODE.sub("", raw)
    return re.sub(r"^v(?=\d)", "", raw, flags=re.I).strip()


def requested_app(body, apps):
    name = normalize(field(parse_fields(body), "App"))
    return next((app for app in apps if normalize(app) == name), "")


def open_tracker(app, number, issues):
    prefix = f"{TITLE_PREFIX}{app} "
    same = [i for i in issues if i["number"] != number and i["state"] == "OPEN" and i["title"].startswith(prefix)]
    return max(same, key=lambda i: version_key(i["title"][len(prefix):]), default=None)


def decide(number, body, issues, patches_list):
    apps = targets(patches_list)
    app = requested_app(body, apps)
    version = requested_version(body)
    verdict, reason, duplicate_of, labels, title, retitle, lines = "pass", "", 0, [], "", None, []

    if not app:
        verdict, labels = "flag", ["needs info"]
        lines.append("The App field doesn't name an app this bundle supports.")
    elif not re.search(r"\d", version):
        verdict, labels = "flag", ["needs info"]
        lines.append("No version number was found in the New version field.")
    else:
        current = max(apps[app], key=version_key, default="")
        tracker = open_tracker(app, number, issues)
        if current and compare(version, current) == 0:
            verdict, reason = "close", "completed"
            lines.append(f"Version {patches_list['version']} of the bundle already supports {app} {version}.")
        elif current and compare(version, current) < 0:
            verdict, reason = "close", "completed"
            lines.append(f"Version {patches_list['version']} of the bundle supports {app} {current}, which is newer than {version}.")
        elif tracker:
            tracked = tracker["title"][len(TITLE_PREFIX) + len(app) + 1:]
            verdict, reason, duplicate_of = "close", "duplicate", tracker["number"]
            if compare(version, tracked) > 0:
                retitle = {"number": tracker["number"], "title": f"{TITLE_PREFIX}{app} {version}"}
                lines.append(f"Issue #{tracker['number']} now tracks {app} {version}.")
            else:
                lines.append(f"Issue #{tracker['number']} already tracks {app} {tracked}.")
        else:
            title = f"{TITLE_PREFIX}{app} {version}"

    return {
        "verdict": verdict,
        "reason": reason,
        "duplicate_of": duplicate_of,
        "labels": labels,
        "title": title,
        "retitle": retitle,
        "comment": MARKER + "\n" + "\n".join(lines) if lines else "",
    }


if __name__ == "__main__":
    number = int(sys.argv[1])
    body = Path(sys.argv[2]).read_text(encoding="utf-8")
    issues = json.loads(Path(sys.argv[3]).read_text(encoding="utf-8"))
    patches_list = json.loads(Path(sys.argv[4]).read_text(encoding="utf-8"))
    print(json.dumps(decide(number, body, issues, patches_list)))
