#!/usr/bin/env python3
"""python3 issue_comment.py <issue_comment-event.json> [patches-list.json]"""

import json
import os
import re
import sys
from datetime import datetime, timedelta
from pathlib import Path

REPO = os.environ.get("GITHUB_REPOSITORY", "hxreborn/morphe-patches")
CLOSED_MARKER = "<!-- closed-issue-bot -->"
PING_MARKER = "<!-- status-ping-bot -->"
PING_WINDOW = timedelta(days=30)
LOCK_DELAY = timedelta(days=8)

REQUEST = re.compile(
    r"\b(update|upgrade|version|latest|newest|new|support|please|pls|plz|can you|could you|add|bring|fix|solutions?"
    r"|any news|eta)\b"
    r"|\b(not|doesn'?t|does not|didn'?t|did not|won'?t|isn'?t|stopped|no longer) work"
    r"|\b(still|broken|crash|error|fail|failure|missing|alert|popup|toast|dialog)",
    re.I,
)
RESOLVED = re.compile(r"\b(work(s|ing)? (fine|perfectly|great|now)|now (it )?works|it works|fixed now|solved)\b", re.I)
QUOTED = re.compile(r"^\s*>.*$", re.M)
MENTION = re.compile(r"@[\w-]+")
OWNER_MENTION = re.compile(rf"@{re.escape(REPO.split('/')[0])}\b", re.I)
CODE = re.compile(r"```.*?```|`[^`]*`", re.S)
PING = re.compile(
    r"\bany\s+(updates?|news|progress)\b(?!\s+(of|to|from)\b)"
    r"|\bany\s+(chance|possibility)\s+(of|for)\s+(an?\s+)?update"
    r"|\b(are|is)\s+(you|anyone|someone|somebody)\s+(still\s+)?working\s+on\s+(it|this)\b"
    r"|\bworking\s+on\s+(it|this)\s+or\s+not\b"
    r"|\beta\b"
    r"|\bwhen\s+will\b"
    r"|\bstill\s+(alive|planned)\b"
    r"|\bsay\s+something\b"
    r"|\b(has|is)\s+(this|it)\s+(been\s+)?(added|done|implemented|ready)\s+yet\b"
    r"|\b(patiently|desperately)\s+waiting\b|\bwaiting\b[^.?!]{0,20}\b(patiently|desperately)\b"
    r"|\b(don'?t|do\s+not)\s+forget\s+(about\s+)?(us|me|this|it)\b"
    r"|\b(dev|you|maintainer)\s+(didn'?t|did\s+not|hasn'?t|has\s+not|haven'?t)\s+(even\s+)?(see|look|repl|respon)",
    re.I,
)
NO_RUSH = re.compile(r"\bno\s+rush\b|\btake\s+your\s+time\b|\bno\s+pressure\b|\bnot\s+to\s+put\s+any\s+pressure\b", re.I)
BARE_PING = re.compile(r"\W*(bump|up|(hello|hi|hey)?\s*\?+|patch(es)?\s+(please|pls|plz))\W*", re.I)


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


def is_status_ping(comment):
    own = QUOTED.sub("", comment)
    unmentioned = MENTION.sub("", own).strip()
    if NO_RUSH.search(unmentioned):
        return False
    if PING.search(CODE.sub("", unmentioned)):
        return True
    if BARE_PING.fullmatch(unmentioned):
        return True
    return OWNER_MENTION.search(own) is not None and not re.search(r"\w", unmentioned)


def timestamp(value):
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def is_early_status_ping(issue, comment):
    age = timestamp(comment["created_at"]) - timestamp(issue["created_at"])
    return is_status_ping(comment["body"] or "") and age <= PING_WINDOW


def ping_reply():
    return (
        f"{PING_MARKER}\n"
        "There's no ETA for this request yet. Any updates will be posted on this issue.\n"
    )


def closed_issue_reply(commented_at, issue_title="", patches_list=None, discussion=False):
    lock_day = commented_at + LOCK_DELAY
    new = f"https://github.com/{REPO}/issues/new"
    lead = "This issue is closed. For anything new, open a new issue:"
    if patches_list:
        apps = targets(patches_list)
        app = targeted_app(issue_title, apps)
        if app and apps[app]:
            versions = sorted(apps[app])
            named = " and ".join([", ".join(versions[:-1]), versions[-1]]) if len(versions) > 1 else versions[0]
            lead = (f"Version {patches_list['version']} of the bundle supports {app} {named}. "
                    "This issue is closed. For anything new, open a new issue:")
    return (
        f"{CLOSED_MARKER}\n"
        f"{lead}\n"
        "\n"
        f"- A newer app version: an [app update request]({new}?template=app_update.yml), one per version\n"
        f"- Another patch for a supported app: a [feature request]({new}?template=feature_request.yml)\n"
        f"- A problem on a version the README lists: a [bug report]({new}/choose)\n"
        + ("" if discussion else
           f"\nThe bot locks this issue on {lock_day:%B} {lock_day.day}, {lock_day.year} unless someone comments before then.\n")
    )


if __name__ == "__main__":
    event = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
    issue, comment = event["issue"], event["comment"]
    if issue["state"] == "open":
        if not is_early_status_ping(issue, comment):
            sys.exit(1)
        print(ping_reply(), end="")
    else:
        if not is_request(comment["body"] or ""):
            sys.exit(1)
        patches_list = json.loads(Path(sys.argv[2]).read_text(encoding="utf-8")) if len(sys.argv) > 2 else None
        labels = {label["name"] for label in issue.get("labels") or []}
        print(closed_issue_reply(timestamp(comment["created_at"]), issue.get("title") or "", patches_list, "discussion" in labels), end="")
