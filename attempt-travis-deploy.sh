#!/usr/bin/env bash

readonly SNAPSHOTS_BRANCH='dev'
readonly RELEASES_BRANCH='master'

echo 'Attempting to deploy artifacts'

# Verify that JAVA_HOME is set
if [[ -z ${JAVA_HOME} ]]; then # Exit if JAVA_HOME is unset
    echo 'JAVA_HOME variable is unset, exiting'
    exit 1;
fi
echo "JAVA_HOME = ${JAVA_HOME}"

echo 'Retrieving project version'
# Get project version using special script
project_version=$(bash project-version.sh)
echo "Got project version: ${project_version}"

if [[ ${project_version} == *-SNAPSHOT ]]; then # Try to deploy snapshot if version ends with '-SNAPSHOT'
    echo 'This is a snapshot version'
    # Snapshots deployment happens only for `development` branch excluding pull requests to it (but including merges)
    if [[ "${TRAVIS_BRANCH}" = "${SNAPSHOTS_BRANCH}" ]]; then
        echo "Deploying version ${project_version} to snapshot repositories"
        bash .travis/scripts/deploy-snapshot-to-maven-repositories.sh
    else
        echo "Not deploying snapshot as branch is not ${SNAPSHOTS_BRANCH}"
    fi
else # Try to deploy release if version doesn't end with '-SNAPSHOT'
    echo 'This is a release version'
    # Release deployment happens only for `release` branch excluding pull requests to it (but including merges)
    if [[ "${TRAVIS_BRANCH}" = "${RELEASES_BRANCH}" ]]; then
        echo "Deploying version ${project_version} to release repositories"
        bash .travis/scripts/deploy-release-to-maven-repositories.sh
    else
        echo "Not deploying release as branch is not \`${SNAPSHOTS_BRANCH}\`"
    fi
fi