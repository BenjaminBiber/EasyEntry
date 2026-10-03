#!/usr/bin/env bash
# Berechnet die naechste Version aus dem hoechsten Tag der Form vX.Y.Z.
#
# Aufruf:  next-version.sh patch|minor|major
# Ausgabe: version=, version_code= und tag= im Format fuer $GITHUB_OUTPUT
set -euo pipefail

bump="${1:-}"

fail() {
  echo "::error::$1" >&2
  exit 1
}

case "$bump" in
  major | minor | patch) ;;
  *) fail "Unbekannter Versionssprung '$bump' (erwartet: patch, minor oder major)" ;;
esac

# sed statt head: head beendet die Pipe vorzeitig, das wuerde pipefail ausloesen.
latest=$(git tag --list 'v*' --sort=-v:refname | { grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' || true; } | sed -n 1p)

if [[ -z "$latest" ]]; then
  # Erstes Release, unabhaengig vom gewaehlten Sprung.
  major=1 minor=0 patch=0
else
  IFS=. read -r major minor patch <<<"${latest#v}"
  # 10#: fuehrende Nullen nicht als Oktalzahl lesen.
  major=$((10#$major)) minor=$((10#$minor)) patch=$((10#$patch))
  case "$bump" in
    major) major=$((major + 1)) minor=0 patch=0 ;;
    minor) minor=$((minor + 1)) patch=0 ;;
    patch) patch=$((patch + 1)) ;;
  esac
fi

# versionCode = major*10000 + minor*100 + patch ist nur eindeutig, solange minor und patch
# zweistellig bleiben. 1.2.100 ergaebe sonst denselben Code wie 1.3.0.
if ((minor > 99 || patch > 99)); then
  fail "Version $major.$minor.$patch: minor und patch muessen <= 99 sein, bitte minor bzw. major erhoehen"
fi

version="$major.$minor.$patch"
if git rev-parse -q --verify "refs/tags/v$version" >/dev/null; then
  fail "Tag v$version existiert bereits"
fi

echo "version=$version"
echo "version_code=$((major * 10000 + minor * 100 + patch))"
echo "tag=v$version"
