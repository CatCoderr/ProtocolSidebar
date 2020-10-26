#!/usr/bin/env bash

bash ./.travis/scripts/import-signing-key.sh

echo 'Deploying artifacts'
bash ./.travis/scripts/deploy-to-github-package-registry.sh
echo 'Deployed artifacts'