#!/usr/bin/env python3
"""python3 .github/actions/triage/test_app_update.py"""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from app_update import decide

PATCHES = {
    "version": "1.49.1",
    "patches": [{"compatiblePackages": [
        {"name": "Proton VPN", "packageName": "ch.protonvpn.android",
         "targets": [{"version": "5.20.39.0"}, {"version": "5.20.57.0"}]},
        {"name": "Showly", "packageName": "com.michaldrabik.showly2", "targets": [{"version": "3.72.0"}]},
    ]}],
}


def body(app, version):
    return f"### App\n\n{app}\n\n### New version\n\n{version}\n\n### APK source\n\nhttps://apkmirror.com/x\n"


def tracker(number, app, version, state="OPEN"):
    return {"number": number, "title": f"[App Update]: {app} {version}", "state": state}


CASES = {
    "newer version with no tracker gets the canonical title": (
        body("Proton VPN", "5.20.60.0"), [], ("pass", "", 0, "[App Update]: Proton VPN 5.20.60.0", None)),
    "version code in parentheses is dropped": (
        body("Proton VPN", "v5.20.60.0 (605206000)"), [], ("pass", "", 0, "[App Update]: Proton VPN 5.20.60.0", None)),
    "version already targeted closes as completed": (
        body("Proton VPN", "5.20.57.0"), [], ("close", "completed", 0, "", None)),
    "older version closes as completed": (
        body("Proton VPN", "5.20.21.0"), [], ("close", "completed", 0, "", None)),
    "same version as the open tracker closes as its duplicate": (
        body("Showly", "3.73.0"), [tracker(7, "Showly", "3.73.0")], ("close", "duplicate", 7, "", None)),
    "older than the open tracker closes as its duplicate": (
        body("Showly", "3.72.5"), [tracker(7, "Showly", "3.73.0")], ("close", "duplicate", 7, "", None)),
    "newer than the open tracker retitles it": (
        body("Showly", "3.74.0"), [tracker(7, "Showly", "3.73.0")],
        ("close", "duplicate", 7, "", {"number": 7, "title": "[App Update]: Showly 3.74.0"})),
    "closed tracker for the same version is not a duplicate target": (
        body("Showly", "3.73.0"), [tracker(7, "Showly", "3.73.0", "CLOSED")],
        ("pass", "", 0, "[App Update]: Showly 3.73.0", None)),
    "another app's tracker is ignored": (
        body("Showly", "3.73.0"), [tracker(7, "Proton VPN", "5.20.60.0")],
        ("pass", "", 0, "[App Update]: Showly 3.73.0", None)),
    "the issue itself is not its own tracker": (
        body("Showly", "3.73.0"), [tracker(9, "Showly", "3.73.0")], ("pass", "", 0, "[App Update]: Showly 3.73.0", None)),
    "unknown app asks for info": (
        body("Nope", "1.0"), [], ("flag", "", 0, "", None)),
    "missing version asks for info": (
        body("Showly", "_No response_"), [], ("flag", "", 0, "", None)),
}

failures = 0
for name, (issue_body, issues, expected) in CASES.items():
    result = decide(9, issue_body, issues, PATCHES)
    actual = (result["verdict"], result["reason"], result["duplicate_of"], result["title"], result["retitle"])
    ok = actual == expected
    failures += not ok
    print(f"{'ok  ' if ok else 'FAIL'} {name}: expected {expected}, got {actual}")

comment = decide(9, body("Proton VPN", "5.20.57.0"), [], PATCHES)["comment"]
ok = comment == "<!-- app-update-bot -->\n1.49.1 already targets Proton VPN 5.20.57.0"
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} already-targeted comment names the bundle version and the app")

flag_labels = decide(9, body("Nope", "1.0"), [], PATCHES)["labels"]
ok = flag_labels == ["needs info"]
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} a flag carries the needs info label")

total = len(CASES) + 2
print(f"\n{total - failures}/{total} passed")
sys.exit(1 if failures else 0)
