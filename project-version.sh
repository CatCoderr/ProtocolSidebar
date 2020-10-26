#!/usr/bin/env bash
mvn -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec -q