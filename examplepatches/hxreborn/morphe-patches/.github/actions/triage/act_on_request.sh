#!/usr/bin/env bash
set -eo pipefail

n=$1
list=$2
here=$(dirname "$0")
human_events() {
  gh api "repos/$REPO/issues/$n/timeline" --paginate \
    --jq ".[] | select(.event == \"$1\" and .actor.login != \"github-actions[bot]\") | .actor.login"
}

jq -e --argjson n "$n" 'any(.[]; .number == $n and .state == "OPEN")' requests.json >/dev/null || exit 0
jq -e --argjson n "$n" '.[] | select(.number == $n) | .labels | map(.name) | index("bot-wrong")' \
  requests.json >/dev/null && exit 0
jq -r --argjson n "$n" '.[] | select(.number == $n) | .body // ""' requests.json > body.md
previous=$(gh api "repos/$REPO/issues/$n/comments" --paginate \
  --jq '.[] | select(.user.login == "github-actions[bot]" and (.body | contains("<!-- app-request-bot -->"))) | .body | @json' |
  tail -n 1 | jq -r '. // empty')
printf '%s' "$previous" > previous.md
python3 "$here/app_request.py" "$n" body.md requests.json "$list" bundles.json elsewhere previous.md > verdict.json
verdict=$(jq -r .verdict verdict.json)
if [ "$verdict" = hold ]; then
  echo "#$n: held while people discuss it"
  exit 0
fi
title=$(jq -r .title verdict.json)
current=$(jq -r --argjson n "$n" '.[] | select(.number == $n) | .title' requests.json)
jq -j .comment verdict.json > comment.md
if [ -n "$previous" ] && [ ! -s bundles.json ]; then
  echo "#$n: skipped while the bundle registry is unavailable"
  exit 0
fi
if [ "$verdict" = pass ] && [ ! -s comment.md ] && [ -n "$previous" ]; then
  printf '<!-- app-request-bot -->\nThis app request now passes the automated checks.\n' > comment.md
fi
[ -z "$ISSUE" ] && [ "$verdict" = pass ] && [ -z "$previous" ] && [ "$title" = "$current" ] && exit 0
if [ "$verdict" = close ]; then
  reopened_by=$(human_events reopened)
  if [ -n "$reopened_by" ]; then
    echo "#$n: reopened by $reopened_by, not closed again"
    exit 0
  fi
  original=$(jq -r .duplicate_of verdict.json)
  if [ "$original" != 0 ]; then
    original_state=$(gh issue view "$original" --repo "$REPO" --json state --jq .state)
    if [ "$original_state" != OPEN ]; then
      echo "#$n: #$original is no longer open, not closed as its duplicate"
      exit 0
    fi
  fi
fi
echo "#$n: $(jq -c 'del(.comment)' verdict.json)"
[ "$DRY_RUN" = true ] && exit 0

if [ "$title" != "$current" ]; then
  renamed_by=$(human_events renamed)
  [ -n "$renamed_by" ] || gh issue edit "$n" --repo "$REPO" --title "$title"
fi
if [ -s comment.md ] && [ "$(cat comment.md)" != "$(printf '%s' "$previous" | sed -E 's/ \([0-9]+ stars\)//g')" ]; then
  labels=$(jq -r '.labels | join(",")' verdict.json)
  [ -z "$labels" ] || gh issue edit "$n" --repo "$REPO" --add-label "$labels"
  gh issue comment "$n" --repo "$REPO" --body-file comment.md
fi
if [ -n "$previous" ]; then
  mapfile -t outdated < <(jq -r --argjson n "$n" --slurpfile verdict verdict.json \
    '.[] | select(.number == $n) | .labels[].name | select(. == "needs info" or . == "duplicate")
     | select(. as $label | $verdict[0].labels | index($label) | not)' requests.json)
  bash -eo pipefail "$here/remove_bot_labels.sh" "$n" "${outdated[@]}"
fi
if [ "$verdict" = close ]; then
  if [ "$(jq -r .reason verdict.json)" = duplicate ]; then
    gh issue close "$n" --repo "$REPO" --duplicate-of "$original"
  else
    gh issue close "$n" --repo "$REPO" --reason "$(jq -r .reason verdict.json)"
  fi
fi
