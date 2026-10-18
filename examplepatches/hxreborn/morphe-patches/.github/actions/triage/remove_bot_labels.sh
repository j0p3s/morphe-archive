#!/usr/bin/env bash
set -eo pipefail

issue=$1
shift
for label in "$@"; do
  added_by=$(gh api "repos/$REPO/issues/$issue/timeline" --paginate \
    --jq ".[] | select(.event == \"labeled\" and .label.name == \"$label\") | .actor.login" | tail -n 1)
  [ "$added_by" != "github-actions[bot]" ] || gh issue edit "$issue" --repo "$REPO" --remove-label "$label"
done
