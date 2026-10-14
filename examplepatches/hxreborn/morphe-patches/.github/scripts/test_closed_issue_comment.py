#!/usr/bin/env python3
"""python3 .github/scripts/test_closed_issue_comment.py"""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from closed_issue_comment import MARKER, is_request, reply

REQUESTS = [
    "Update the latest support version bro",
    "Hello, Can you update it for the new 1.4 version?",
    "Hello,  the app just got an update and the patch no longer works with the new version.",
    "Remove rating prompts patch not works, when app open popups stills visible, please check @hxreborn",
    "And one more issue I can't access this app without internet, please add a patches to solve this issue.. thanks",
    "@hxreborn Please add one more patch 'remove app startup delay' thanks again for all previous patches",
    "Hey\nCan you patch this app\n\nhttps://athan-prayer-times-quran-more.en.uptodown.com/android",
    "You close it as completed but terabox has no clone patch please bring terabox clone patch",
    "Didn't work",
    "I dunno if I am doing it wrong, it says 1.47 at the bottom but still only see 7 patches",
    "This alert appears on opening the app",
    "please update patches to 6.83.0",
    "This has been closed without a fix?",
    "Any solutions to this yet?",
    "New version changed a bit, remove review in mainactivity changed class. Library patch also changed bytes",
]

NOT_REQUESTS = [
    "Thank you so much for taking my suggestion; I am very grateful. Best regards.",
    "Bro, you're a godsend ❤️ Thanks for all the hard work! 🙏",
    "Many thanks",
    "you are fast, my friend. thank you very much, sir",
    "Thank you ! it s fixed now !!!",
    "Fixed after reinstalling app\n👍",
    "Yes, it's perfect.",
    "Great. Thank you very much.",
    "Completed in hxreborn patches 1.47.0",
    "Yeah thanks , Updated and working perfectly now .",
    "No problem. Thanks for the work you and other devs are doing here in your spare time.",
    "> This is included in [v1.34.0](https://github.com/x/releases/tag/v1.34.0). Update the bundle in Morphe Manager and repatch.\n\nThis is actually crazy automation bro.",
    "> Thanks for the request! This is in v1.31.0. Update the bundle and repatch, and comment below if something is missing.\n\nnice",
    "",
]

failures = 0
for text in REQUESTS:
    ok = is_request(text)
    failures += not ok
    print(f"{'ok  ' if ok else 'FAIL'} replies to: {text[:70]!r}")
for text in NOT_REQUESTS:
    ok = not is_request(text)
    failures += not ok
    print(f"{'ok  ' if ok else 'FAIL'} silent on: {text[:70]!r}")

PATCHES = {"version": "1.49.1", "patches": [{"compatiblePackages": [
    {"name": "Proton VPN", "packageName": "ch.protonvpn.android", "targets": [{"version": "5.20.39.0"}, {"version": "5.20.57.0"}]},
    {"name": "Proton Mail", "packageName": "ch.protonmail.android", "targets": [{"version": "7.11.10"}]},
]}]}

text = reply()
ok = text.startswith(MARKER + "\n") and "template=app_update.yml" in text and "issues/new/choose" in text
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} the reply carries the marker and both forms")

text = reply("[App Update]: Proton VPN 5.20.57.0", PATCHES)
ok = text.splitlines()[1] == "1.49.1 targets Proton VPN 5.20.39.0, 5.20.57.0. this issue is closed, anything new is a new issue:"
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} a title naming a supported app opens with its targeted versions")

text = reply("[Feature]: Hide Unnecessary boxes", PATCHES)
ok = text.splitlines()[1] == "this issue is closed. anything new is a new issue, not a comment here:"
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} a title naming no app keeps the plain opening")

total = len(REQUESTS) + len(NOT_REQUESTS) + 3
print(f"\n{total - failures}/{total} passed")
sys.exit(1 if failures else 0)
