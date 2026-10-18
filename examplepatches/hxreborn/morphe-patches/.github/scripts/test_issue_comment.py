#!/usr/bin/env python3
"""python3 .github/scripts/test_issue_comment.py"""

import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from issue_comment import timestamp, CLOSED_MARKER, PING_MARKER, is_early_status_ping, is_request, is_status_ping, ping_reply, closed_issue_reply

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

PINGS = [
    "@hxreborn say something please, are you working on this or not..",
    "@hxreborn patches please..",
    "Why the dev didn't see this yet I also want this patch",
    "any update?",
    "Any updates on this?",
    "eta?",
    "is this still alive?",
    "bump",
    "?",
    "When will this be added?",
    "Is anyone working on this?",
    "@hxreborn say something",
    "@hxreborn",
    "@hxreborn 🙏",
    "hello?",
    "Waiting for this patiently..",
    "Most needed feature. Desperately waiting",
    "please do not forget about us",
    "any possibility of an update on this?",
    "Has this been added yet I think it would be helpful",
    "ANY UPDATE???",
]

NOT_PINGS = [
    "Yes, please",
    "Ok, got it. I will wait for the fix!",
    "Thanks anyway for looking into it. 😊\nIs there a chance that future updates will change that?",
    "Hello, Can you update it for the new 1.4 version?",
    "@hxreborn Please add one more patch 'remove app startup delay' thanks again for all previous patches",
    "Thank you @hxreborn please add one more patch to remove startup ad (pan masala banner)",
    "This app is not working properly, it needs a new patch.",
    "It could be rather good, also a custom accent color.",
    "did it work for you after the 1.19.0 update?",
    "Got it bro, that makes much more sense now. Thanks for figuring out the actual cause and working on the fix.",
    "Not working",
    "I'm having the same issue, and it's still happening with v3.3.0-dev.1",
    "Does any update of the app break the patch?",
    "```\nI/Download eta 12s\n```",
    "Sure, waiting for dev.12 release",
    "Don't forget morphe is a team of many devs",
    "@hxreborn here is the logcat you asked for",
    "+1",
    "@someone-else",
    "👍",
    "...",
    "Please take your time; there's no rush. We'll be waiting patiently for you.",
    "Any update? Just wanted to let you know, not to put any pressure on you.",
    "? `Morphe > Player > Force landscape in fullscreen`",
    "@hxreborn `FATAL EXCEPTION: main java.lang.NoSuchMethodError`",
    "Full localization support has not been implemented yet",
    "> any update?\n\nthanks, works on 1.52.0",
    "<!-- any updates? -->",
    "<!-- bump -->",
    "`bump`",
    "```\nany updates?",
    "",
]

for text in PINGS:
    ok = is_status_ping(text)
    failures += not ok
    print(f"{'ok  ' if ok else 'FAIL'} answers ping: {text[:70]!r}")
for text in NOT_PINGS:
    ok = not is_status_ping(text)
    failures += not ok
    print(f"{'ok  ' if ok else 'FAIL'} silent on: {text[:70]!r}")

ISSUE = {"created_at": "2026-10-09T06:22:46Z"}
ok = is_early_status_ping(ISSUE, {"created_at": "2026-10-13T10:00:00Z", "body": PINGS[0]})
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} a ping 4 days after opening gets the reply")

ok = not is_early_status_ping(ISSUE, {"created_at": "2026-11-09T06:22:47Z", "body": PINGS[0]})
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} a ping after 30 days gets no reply")

text = ping_reply()
ok = text == PING_MARKER + "\nThere's no ETA for this request yet. Any updates will be posted on this issue.\n"
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} the ping reply is the marker and the no-ETA sentence")

PATCHES = {"version": "1.49.1", "patches": [{"compatiblePackages": [
    {"name": "Proton VPN", "packageName": "ch.protonvpn.android", "targets": [{"version": "5.20.39.0"}, {"version": "5.20.57.0"}]},
    {"name": "Proton Mail", "packageName": "ch.protonmail.android", "targets": [{"version": "7.11.10"}]},
]}]}

COMMENTED_AT = timestamp("2026-10-15T10:00:00Z")
text = closed_issue_reply(COMMENTED_AT)
ok = text.startswith(CLOSED_MARKER + "\n") and "template=app_update.yml" in text and "issues/new/choose" in text
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} the closed-issue reply carries its marker and both forms")

ok = text.rstrip().endswith("The bot locks this issue on October 23, 2026 unless someone comments before then.")
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} the closed-issue reply names the day the lock job locks it")

text = closed_issue_reply(COMMENTED_AT, "[App Update]: Proton VPN 5.20.57.0", PATCHES)
ok = text.splitlines()[1] == "Version 1.49.1 of the bundle supports Proton VPN 5.20.39.0 and 5.20.57.0. This issue is closed. For anything new, open a new issue:"
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} a title naming a supported app opens with its targeted versions")

text = closed_issue_reply(COMMENTED_AT, "[Feature]: Hide Unnecessary boxes", PATCHES)
ok = text.splitlines()[1] == "This issue is closed. For anything new, open a new issue:"
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} a title naming no app keeps the plain opening")

started = time.monotonic()
flood = [is_status_ping("?" * 5000 + "x"), is_status_ping("\n" * 60000 + "x"), is_request("\n" * 60000 + "x")]
ok = flood == [False, False, False] and time.monotonic() - started < 2
failures += not ok
print(f"{'ok  ' if ok else 'FAIL'} long runs of question marks and newlines are read in linear time")

total = len(REQUESTS) + len(NOT_REQUESTS) + len(PINGS) + len(NOT_PINGS) + 8
print(f"\n{total - failures}/{total} passed")
sys.exit(1 if failures else 0)
