#!/bin/bash -e

: ${GITHUB_TOKEN:?Exiting release: No GITHUB_TOKEN variable}
: ${GRADLE_PUBLISH_KEY:?Exiting release: No GRADLE_PUBLISH_KEY variable}
: ${GRADLE_PUBLISH_SECRET:?Exiting release: No GRADLE_PUBLISH_SECRET variable}

if [[ -z "$RELEASE" ]]; then
  echo "Exiting release: RELEASE env variable not found"
  exit 0;
fi

if [[ "$TRAVIS_BRANCH" != "master" ]] && [[ -n "$TRAVIS_PULL_REQUEST_SHA" ]]; then
  echo "Exiting release: Release is enabled on master branch only"
  exit 0;
fi

git config --local user.name "travis@travis-ci.org"
git config --local user.email "Travis CI"
git checkout "$TRAVIS_BRANCH" >/dev/null 2>&1

.scripts/release.sh "$RELEASE"
