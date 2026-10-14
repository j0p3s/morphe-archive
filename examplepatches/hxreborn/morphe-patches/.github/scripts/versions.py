import re

PRERELEASE = {"alpha", "beta", "rc", "pre", "preview", "dev", "canary", "snapshot"}
BUILD_TAG = re.compile(r"[\s_-]*(?<![a-z0-9])(arm64(-v8a)?|armeabi(-v7a)?|armv7|x86(_64)?|tv|universal)(?![a-z0-9])")


def version_key(version):
    text = BUILD_TAG.sub("", (version or "").lower())
    first_digit = re.search(r"\d", text)
    if not first_digit:
        return ()
    tokens = []
    for token in re.findall(r"\d+|[a-z]+", text[first_digit.start():]):
        if token.isdigit():
            tokens.append((2, int(token), ""))
        elif token in PRERELEASE:
            tokens.append((0, 0, token))
        else:
            tokens.append((1, 0, token))
    while tokens and tokens[-1] == (2, 0, ""):
        tokens.pop()
    return tuple(tokens) + ((1, 0, ""),)


def compare(a, b):
    ka, kb = version_key(a), version_key(b)
    return (ka > kb) - (ka < kb)
