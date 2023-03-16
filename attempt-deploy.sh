#!/usr/bin/env bash

set -e 

readonly DEPLOY_BRANCH='master'

echo 'Attempting to deploy artifacts'

# Verify that JAVA_HOME is set
if [[ -z ${JAVA_HOME} ]]; then # Exit if JAVA_HOME is unset
    echo 'JAVA_HOME variable is unset, exiting'
    exit 1;
fi
echo "JAVA_HOME = ${JAVA_HOME}"

if [[ "${GITHUB_REF_NAME}" != "${DEPLOY_BRANCH}" ]]; then
    echo "Not deploying artifacts because this is not a ${DEPLOY_BRANCH} branch"
    exit 0
fi

echo 'Retrieving project version'
# Get project version using special script
project_version=$(bash project-version.sh)
echo "Got project version: ${project_version}"

if [[ ${project_version} == *-SNAPSHOT ]]; then # Try to deploy snapshot if version ends with '-SNAPSHOT'
    echo 'This is a snapshot version'
    echo "Deploying version ${project_version} to snapshot repositories"
    bash ./scripts/deploy-to-sonatype-ossrh.sh
else # Try to deploy release if version doesn't end with '-SNAPSHOT'
    echo 'This is a release version'
    # Release deployment happens only for `release` branch excluding pull requests to it (but including merges)
    echo "Deploying version ${project_version} to release repositories"
    bash ./scripts/deploy-to-sonatype-ossrh.sh
    
fi