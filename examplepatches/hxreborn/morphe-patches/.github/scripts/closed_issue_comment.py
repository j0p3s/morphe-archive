#!/usr/bin/env python3
"""python3 closed_issue_comment.py <issue_comment-event.json> [patches-list.json]"""

import json
import os
import re
import sys
from pathlib import Path

REPO = os.environ.get("GITHUB_REPOSITORY", "hxreborn/morphe-patches")
MARKER = "<!-- closed-issue-bot -->"

REQUEST = re.compile(
    r"\b(update|upgrade|version|latest|newest|new|support|please|pls|plz|can you|could you|add|bring|fix|solutions?"
    r"|any news|eta)\b"
    r"|\b(not|doesn'?t|does not|didn'?t|did not|won'?t|isn'?t|stopped|no longer) work"
    r"|\b(still|broken|crash|error|fail|failure|missing|alert|popup|toast|dialog)",
    re.I,
)
RESOLVED = re.compile(r"\b(work(s|ing)? (fine|perfectly|great|now)|now (it )?works|it works|fixed now|solved)\b", re.I)
QUOTED = re.compile(r"^\s*>.*$", re.M)


def normalize(text):
    return re.sub(r"[^a-z0-9]", "", text.lower())


def targets(patches_list):
    apps = {}
    for patch in patches_list["patches"]:
        for package in patch.get("compatiblePackages") or []:
            versions = apps.setdefault(package["name"], set())
            versions.update(t["version"] for t in package.get("targets") or [] if t.get("version"))
    return apps


def targeted_app(text, apps):
    haystack = normalize(text)
    names = [name for name in apps if normalize(name) in haystack]
    return max(names, key=len, default="")


def is_request(comment):
    own = QUOTED.sub("", comment)
    return bool(REQUEST.search(own)) and not RESOLVED.search(own)



def reply(issue_title="", patches_list=None):
    new = f"https://github.com/{REPO}/issues/new"
    lead = "this issue is closed. anything new is a new issue, not a comment here:"
    if patches_list:
        apps = targets(patches_list)
        app = targeted_app(issue_title, apps)
        if app and apps[app]:
            lead = (f"{patches_list['version']} targets {app} {', '.join(sorted(apps[app]))}. "
                    "this issue is closed, anything new is a new issue:")
    return (
        f"{MARKER}\n"
        f"{lead}\n"
        "\n"
        f"- a newer app version: open an [app update request]({new}?template=app_update.yml). one per version\n"
        f"- another patch for a supported app: open a [feature request]({new}?template=feature_request.yml)\n"
        f"- something broken on a version the README lists: open a [bug report]({new}/choose)\n"
        "\n"
        "this thread locks 7 days after closing\n"
    )


if __name__ == "__main__":
    event = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
    title = event["issue"].get("title") or ""
    if not is_request(event["comment"]["body"] or ""):
        sys.exit(1)
    patches_list = json.loads(Path(sys.argv[2]).read_text(encoding="utf-8")) if len(sys.argv) > 2 else None
    print(reply(title, patches_list), end="")
