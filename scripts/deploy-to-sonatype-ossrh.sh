#!/usr/bin/env bash

set -e 

echo 'Deploying to Sonatype OSSRH'

bash ./scripts/import-signing-key.sh

mvn deploy -B -P build-extras,sign-binaries,code-signing-credentials,sonatype-ossrh-deployment \
--settings ./maven/sonatype-ossrh-settings.xml

echo 'Deployed to Sonatype OSSRH'