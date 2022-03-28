#!/usr/bin/env bash

echo 'Deploying to Sonatype OSSRH'
mvn deploy -B -P build-extras,sign-binaries,code-signing-credentials,sonatype-ossrh-deployment \
--settings ./maven/sonatype-ossrh-settings.xml
echo 'Deployed to Sonatype OSSRH'