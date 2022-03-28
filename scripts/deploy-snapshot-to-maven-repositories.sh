#!/usr/bin/env bash

bash ./scripts/import-signing-key.sh

echo 'Deploying artifacts'
bash ./scripts/deploy-to-sonatype-ossrh.sh
echo 'Deployed artifacts'