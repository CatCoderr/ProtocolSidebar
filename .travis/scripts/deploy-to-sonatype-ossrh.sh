#!/usr/bin/env bash

echo 'Deploying to Sonatype OSSRH'
mvn deploy -P build-extras,sign-binaries,code-signing-credentials,sonatype-ossrh-deployment \
--settings .travis/maven/sonatype-ossrh-settings.xml
echo 'Deployed to Sonatype OSSRH'