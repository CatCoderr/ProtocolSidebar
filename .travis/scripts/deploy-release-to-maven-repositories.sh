#!/usr/bin/env bash

bash ./.travis/scripts/import-signing-key.sh

echo 'Deploying artifacts'
bash ./.travis/scripts/deploy-to-sonatype-ossrh.sh
echo 'Deployed artifacts'