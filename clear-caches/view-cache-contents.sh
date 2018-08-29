#!/usr/bin/env bash
set -e

now=$(gdate "+%Y%m%d_%H%M%S") # "+%Y%m%d_%H%M%S_%3N" for more precision

data="[
  { \"title\": \"caches_for_specific_gradle_versions\",
    \"depth\": 1,
    \"folders\": \"~/.gradle/caches\"
  },
  {  \"title\": \"distributions_for_specific_gradle_versions\",
     \"depth\": 1,
     \"folders\": \"~/.gradle/wrapper/dists\"
  },
  {  \"title\": \"shared_caches_for_gradle_versions\",
    \"depth\": 2,
    \"folders\": \"~/.gradle/caches/modules-2\"
  },
  {  \"title\": \"shared_caches_for_dependency\",
    \"depth\": 1,
    \"folders\": \"~/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin\"
  }
]"

echo "What we can expect"
echo

echo "=== cleanup version specific global caches ==="
echo "When I stop the Gradle daemon gracefully, then"
echo " - I expect that version specific global caches are cleaned up (~/.gradle/caches)"
echo " - I expect that version specific distributions are cleaned up (~/.gradle/wrapper/dists)"
echo

echo "=== cleanup caches shared with different Gradle versions ==="
echo "When I run a Gradle build, then"
echo " - I expect that metadata folders are cleaned up (~/.gradle/caches/metadata-2.x)"
echo "When I stop the Gradle daemon gracefully, then"
echo " - I expect that shared caches are cleaned up (~/.gradle/caches/jars-2, etc.)"
echo

echo "=== cleanup files for unused files in shared caches (jars, modules, transforms) ==="
echo "When I run a Gradle build, then"
echo " - I expect that files in shared caches are deleted"
echo

echo "Notes"
echo " - Cleanup is applicable for the last 30 days"
echo " - See disk usage before and after with 'du -sh'"
echo

sorts=( '' '-t' )
for sortBy in "${sorts[@]}"; do
  while read -r title depth folders; do
    mkdir -p "tmp/alphabetically/$title"
    mkdir -p "tmp/by_timestamp/$title"

    fileName="tmp/by_timestamp/${title}/${now}"
    if [ -z "$sortBy" ]; then
      fileName="tmp/alphabetically/${title}/${now}"
    fi

    echo "Listing files in $folders"
    echo "    at $fileName"
    echo
    tree -D -L ${depth} ${sortBy} $(eval echo $folders) | sed 's%/Users/.*/.gradle%~/.gradle%g' > ${fileName}
  done< <(echo $data | jq -r '.[] | "\(.title) \(.depth) \(.folders)"')
done
