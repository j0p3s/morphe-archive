import re
import subprocess
import sys

CLOSING = re.compile(r"\b(?:close[sd]?|fix(?:e[sd])?|resolve[sd]?):?\s+(\S+?)?#(\d+)\b", re.I)


def closed_issues(repo, revisions):
    log = subprocess.run(["git", "log", "--format=%B", revisions], capture_output=True, text=True, check=True).stdout
    numbers = set()
    for owner_repo, number in CLOSING.findall(log):
        if owner_repo in ("", repo):
            numbers.add(int(number))
    return sorted(numbers)


if __name__ == "__main__":
    for number in closed_issues(sys.argv[1], sys.argv[2]):
        print(number)
