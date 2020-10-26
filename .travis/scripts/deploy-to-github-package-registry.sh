#!/usr/bin/env bash

echo 'Deploying to GitHub Package Registry'
mvn deploy -P build-extras,sign-binaries,code-signing-credentials,github-package-registry-deployment \
--settings .travis/maven/github-package-registry-settings.xml
echo 'Deployed to GiHub Package Registry'