#!/usr/bin/env python3
"""python3 triage.py <issue-body-file> [patches-list.json]"""

import json
import os
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from fields import field, has_any, normalize, normalize_version, parse_fields

MIN_CHARS = 60

REPO = os.environ.get("GITHUB_REPOSITORY", "")
NEW_ISSUE = f"https://github.com/{REPO}/issues/new"

APP_NAME_FIELDS = ("App and version", "App name", "App", "Target app", "App version patched")
APP_VERSION_FIELDS = ("Other version", "App version", "Broken app version", "App version patched",
                      "App and version")
DESCRIPTION_FIELDS = ("Bug description", "What happens", "Describe the bug", "Summary")
LOG_FIELDS = ("Debug log", "Error logs", "Morphe logs", "Logs")
SOURCE_FIELDS = ("APK source", "APK source and type", "APK source and architecture")

REPACKAGERS = ("softonic", "happymod", "apkmody", "modyolo", "moddroid", "an1.com", "rexdl",
               "apkdone", "liteapks", "apkmb", "apk4free", "androeed", "apkrabi")

MARKER = "<!-- triage-bot -->"
NO_RESPONSE = "_no response_"

REPORT = re.compile(
    r"Exception|Error|INSTALL_FAILED|Failed to match|Manager:|manager=|\(API \d+\)|^\s+at \S", re.I | re.M
)

OPTION_PATCHING_FAILED = "patchingfailed"
OPTION_STOCK_FAILS_TOO = "theunpatchedappfailsthesameway"
OPTION_NOT_TRIED = "ihavenottried"
OPTIONS_RUNTIME_FAILURE = ("theappwillnotopenatall", "theappopensthencrashes")
OPTIONS_PATCH_INEFFECTIVE = (
    "thepatchedfeatureisstilllocked",
    "thepatchhasnovisibleeffect",
    "apatchismissingfromthelistinmanager",
)

body_path = Path(sys.argv[1])
list_path = Path(sys.argv[2]) if len(sys.argv) > 2 else Path("patches-list.json")


def supported_apps(data):
    apps = {}
    for patch in data["patches"]:
        for package in patch.get("compatiblePackages") or []:
            key = (package["name"], package["packageName"])
            versions = apps.setdefault(key, set())
            for target in package.get("targets") or []:
                if target.get("version"):
                    versions.add(target["version"])
    return apps


data = json.loads(list_path.read_text(encoding="utf-8"))
apps = supported_apps(data)
fields = parse_fields(body_path.read_text(encoding="utf-8"))

description = field(fields, *DESCRIPTION_FIELDS)
if not has_any(fields, *DESCRIPTION_FIELDS):
    description = max(fields.values(), key=len, default="")

app_name = field(fields, *APP_NAME_FIELDS)
reported_versions = [v for v in (field(fields, n) for n in APP_VERSION_FIELDS) if v]
app_version = " ".join(reported_versions)
typed_version = reported_versions[0] if reported_versions else ""
another_version = "another version" in app_name.lower() and not field(fields, "Other version")
shown_name = re.sub(r"\s*\(another version, type it below\)", "", app_name, flags=re.I)
reported = " ".join(dict.fromkeys(filter(None, (shown_name, typed_version))))
app_haystack = reported or "\n".join(fields.values())
version_words = [t for t in re.split(r"[\s(),`]+", app_version) if t.strip()]
version_tokens = {normalize_version(t) for t in version_words} | {
    normalize(a) + normalize(b) for a, b in zip(version_words, version_words[1:])
}
version_haystack = normalize(app_haystack)
what_happened = field(fields, "What happened")
apk_source = field(fields, *SOURCE_FIELDS)
repackager = next(
    (h for h in REPACKAGERS if re.search(rf"(?:^|[/.@\s]){re.escape(h)}(?![a-z0-9])", apk_source.lower())), ""
)
debug_log = field(fields, *LOG_FIELDS)
report_attached = bool(REPORT.search(debug_log))


def target_forms(version):
    numbered = [w for w in version.split() if any(c.isdigit() for c in w)]
    return {normalize_version(version)} | {normalize_version(w) for w in numbered}


blockers = []
flags = []
labels = []

if not fields:
    blockers.append(
        "The automated checks found no form fields in this issue."
    )

if fields and not report_attached and len(description) < MIN_CHARS:
    flags.append(
        f"The description is {len(description)} characters, and at least {MIN_CHARS} are needed."
    )
    labels.append("needs info")

matched = None
best = 0
normalized_app = normalize(app_haystack)
for (name, package), versions in apps.items():
    signals = [normalize(name), normalize(package)]
    signals += [normalize(word) for word in name.split() if len(word) > 3]
    score = max((len(sig) for sig in signals if sig and sig in normalized_app), default=0)
    if score > best:
        best, matched = score, (name, versions)

if repackager:
    blockers.append(
        f"The APK source `{repackager}` repackages apps, which may cause patching errors."
    )

if not fields:
    pass
elif not matched:
    supported = ", ".join(sorted(name for name, _ in apps))
    lead = (
        f"The reported app `{reported}` was not recognized as supported."
        if reported
        else "No supported app was identified in the report."
    )
    flags.append(
        f"{lead} Bundle version {data['version']} supports these apps: {supported}."
    )
    labels.append("needs info")
elif another_version:
    flags.append(
        "\"Another version\" is selected, but the version is missing."
    )
    labels.append("needs info")
elif matched[1] and not (
    any(form in version_tokens for v in matched[1] for form in target_forms(v)) if version_tokens
    else any(form in version_haystack for v in matched[1] for form in target_forms(v))
):
    name, versions = matched
    flags.append(
        f"Bundle version {data['version']} supports {name} {', '.join(sorted(versions))}. The reported value "
        f"`{typed_version or reported}` does not match a supported version. A newer version belongs in an [app update request]({NEW_ISSUE}?template=app_update.yml)."
    )
    labels.append("untargeted version")

if normalize(what_happened).startswith(OPTION_PATCHING_FAILED) and not report_attached:
    flags.append(
        "Patching failed, but no error report was found in the Error logs field."
    )
    labels.append("needs info")

failure = normalize(field(fields, "What does not work"))
log_text = re.sub(r"^\s*(```|~~~)\w*\s*$", "", debug_log, flags=re.M).strip()
if failure.startswith(OPTIONS_RUNTIME_FAILURE) and not log_text:
    flags.append(
        "The app crashes or won't open, but the Error logs field is empty."
    )
    labels.append("needs info")

stock = "" if failure.startswith(OPTIONS_PATCH_INEFFECTIVE) else normalize(
    field(fields, "Does the unpatched app do the same thing")
)
single_patch = normalize(field(fields, "Does it still happen with only one patch selected"))

if stock.startswith(OPTION_STOCK_FAILS_TOO):
    flags.append(
        "The same problem also occurs in the unpatched app."
    )
    labels.append("needs info")
elif stock.startswith(OPTION_NOT_TRIED):
    flags.append(
        "The unpatched app hasn't been tried."
    )
    labels.append("needs info")

if single_patch.startswith(OPTION_NOT_TRIED):
    flags.append(
        "It hasn't been tested with only one patch selected."
    )
    if "needs info" not in labels:
        labels.append("needs info")

if blockers:
    verdict = "close"
    labels = []
    lines = ["This issue is being closed automatically for the following reasons:", ""]
    lines += [f"- {b}" for b in blockers + flags]
elif flags:
    verdict = "flag"
    lines = [f"- {f}" for f in flags]
else:
    verdict = "pass"
    lines = []

print(json.dumps({
    "verdict": verdict,
    "labels": list(dict.fromkeys(labels)),
    "app": matched[0] if matched else "",
    "comment": MARKER + "\n" + "\n".join(lines) if lines else "",
}))
