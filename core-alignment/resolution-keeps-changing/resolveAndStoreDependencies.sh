#!/bin/bash -efx

echo "deleting old results"
find . -type d -depth 1 | grep "results" | xargs rm -r

echo "resolve and store dependencies for Nebula alignment"
gw clean writeOutSelectedConfigurations resolveDependencies -DuseNebulaAlignment=true --rerun-tasks
find . -type d -depth 1 | grep "build" | sed 'p;s|build|results-NebulaAlignment|' | xargs -n2 cp -rfv

echo "resolve ans store dependencies for core Gradle alignment"
gw clean writeOutSelectedConfigurations resolveDependencies -DuseNebulaAlignment=false --rerun-tasks
find . -type d -depth 1 | grep "build" | sed 'p;s|build|results-CoreGradleAlignment|' | xargs -n2 cp -rfv
