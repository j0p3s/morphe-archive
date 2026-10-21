#!/usr/bin/env bash
set -eo pipefail

n=$1
list=$2
here=$(dirname "$0")
discussion() { bash -eo pipefail "$here/discussion.sh" "$@"; }

jq -e --argjson n "$n" 'any(.[]; .number == $n and .state == "OPEN")' requests.json >/dev/null || exit 0
jq -e --argjson n "$n" '.[] | select(.number == $n) | .labels | map(.name) | index("bot-wrong")' \
  requests.json >/dev/null && exit 0
jq -r --argjson n "$n" '.[] | select(.number == $n) | .body // ""' requests.json > body.md
previous=$(jq -r --argjson n "$n" '[.[] | select(.number == $n) | .comments[]
  | select(.author.login == "github-actions" and (.body | contains("<!-- app-request-bot -->"))) | .body] | last // empty' \
  requests.json)
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
[ "$ACTION" != created ] && [ "$verdict" = pass ] && [ -z "$previous" ] && exit 0
if [ "$verdict" = close ]; then
  if [ "$(cat comment.md)" = "$previous" ]; then
    echo "#$n: already closed once for the same reason and reopened, left alone"
    exit 0
  fi
  original=$(jq -r .duplicate_of verdict.json)
  if [ "$original" != 0 ] && [ "$(discussion view "$original" | jq -r .state)" != OPEN ]; then
    echo "#$n: #$original is no longer open, not closed as its duplicate"
    exit 0
  fi
fi
echo "#$n: $(jq -c 'del(.comment)' verdict.json)"
[ "$DRY_RUN" = true ] && exit 0

if [ "$ACTION" = created ] && [ "$title" != "$current" ]; then
  discussion title "$n" "$title"
fi
if [ -s comment.md ] && [ "$(cat comment.md)" != "$(printf '%s' "$previous" | sed -E 's/ \([0-9]+ stars\)//g')" ]; then
  mapfile -t labels < <(jq -r '.labels[]' verdict.json)
  [ "${#labels[@]}" -eq 0 ] || discussion add-labels "$n" "${labels[@]}"
  discussion comment "$n" comment.md
fi
if [ -n "$previous" ]; then
  mapfile -t outdated < <(jq -r --argjson n "$n" --slurpfile verdict verdict.json \
    '.[] | select(.number == $n) | .labels[].name | select(. == "needs info" or . == "duplicate")
     | select(. as $label | $verdict[0].labels | index($label) | not)' requests.json)
  [ "${#outdated[@]}" -eq 0 ] || discussion remove-labels "$n" "${outdated[@]}"
fi
if [ "$verdict" = close ]; then
  case "$(jq -r .reason verdict.json)" in
    duplicate) reason=DUPLICATE ;;
    completed) reason=RESOLVED ;;
    *) reason=OUTDATED ;;
  esac
  discussion close "$n" "$reason"
fi
