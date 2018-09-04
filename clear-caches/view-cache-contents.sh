#!/usr/bin/env bash
set -e

now=$(gdate "+%Y%m%d_%H%M%S") # "+%Y%m%d_%H%M%S_%3N" for more precision

list_cache_files() {
  currentTime=$now+$(gdate "+%3N")

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

  sorts=( '' '-t' )
  for sortBy in "${sorts[@]}"; do
    while read -r title depth folders; do
      mkdir -p "tmp/alphabetically/$title"
      mkdir -p "tmp/by_timestamp/$title"

      fileName="tmp/by_timestamp/${title}/${currentTime}"
      if [ -z "$sortBy" ]; then
        fileName="tmp/alphabetically/${title}/${currentTime}"
      fi

      echo "Listing files in $folders"
      echo "    at $fileName"
      echo
      tree -D -L ${depth} ${sortBy} $(eval echo $folders) | sed 's%/Users/.*/.gradle%~/.gradle%g' > ${fileName}
    done< <(echo $data | jq -r '.[] | "\(.title) \(.depth) \(.folders)"')
  done
}

we_can_expect() {
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
}

list_distribution_modified_times() {
  sorts=('-n' '-k3')
  for sortBy in "${sorts[@]}"; do
    mkdir -p "tmp/last_modified/gc_properties/alphabetically"
    mkdir -p "tmp/last_modified/gc_properties/by_timestamp"
    mkdir -p "tmp/last_modified/file_hashes/alphabetically"
    mkdir -p "tmp/last_modified/file_hashes/by_timestamp"

    gcFileName="tmp/last_modified/gc_properties/by_timestamp/${now}"
    hashFileName="tmp/last_modified/file_hashes/by_timestamp/${now}"
    if [ "$sortBy" = "-k3" ]; then
      gcFileName="tmp/last_modified/gc_properties/alphabetically/${now}"
      hashFileName="tmp/last_modified/file_hashes/alphabetically/${now}"
    fi

    find ~/.gradle/caches -name gc.properties -maxdepth 2 -exec stat -f '%Sm %N' -t "%Y-%m-%d %H:%M" '{}' + | sort ${sortBy} | sed 's%/Users/.*/.gradle%~/.gradle%g' > "${gcFileName}"
    find ~/.gradle/caches -name fileHashes.lock -maxdepth 3 -exec stat -f '%Sm %N' -t "%Y-%m-%d %H:%M" '{}' + | sort ${sortBy} | sed 's%/Users/.*/.gradle%~/.gradle%g' > "${hashFileName}"
  done

  echo
  echo "We can expect to see the following version-specific caches and distributions remaining (at tmp/last_modified/expect_to_see)"
  gcVersions=$(find ~/.gradle/caches -name gc.properties -maxdepth 2 | sed 's%/Users/.*/.gradle%~/.gradle%g' | sed 's%~/.gradle/caches/%%g' | sed 's%/gc.properties%%g' | sort)
  hashVersions=$(find ~/.gradle/caches -name fileHashes.lock -maxdepth 3 | sed 's%/Users/.*/.gradle%~/.gradle%g' | sed 's%~/.gradle/caches/%%g' | sed 's%/fileHashes/fileHashes.lock%%g' | sort)
  versions_to_see=$(echo "${gcVersions[@]}" "${hashVersions[@]}" | sort -n | uniq)
  echo $versions_to_see | tr ' ' '\n'
  echo

  mkdir -p tmp/last_modified/expect_to_see
  echo $versions_to_see | tr ' ' '\n' > tmp/last_modified/expect_to_see/${now}
}

setup_for_re_running() {
  echo "=== For re-running distribution cleanup ==="
  echo "When was the last time gc.properties was modified?"
  find ~/.gradle/caches -name gc.properties -maxdepth 2 -exec stat -f '%Sm %N' '{}' +
  echo
  echo " - Which of these were within the last two days?"
  find ~/.gradle/caches -name gc.properties -maxdepth 2 -newermt $(gdate "+%Y%m%d" --date="2 days ago") -exec stat -f '%Sm %N' '{}' +
  echo
  echo " - Running 'touch -mt `gdate "+%Y%m%d" --date="2 days ago"`0000 <path>/gc.properties'"
  find ~/.gradle/caches -name gc.properties -maxdepth 2 -newermt $(gdate "+%Y%m%d" --date="2 days ago") | xargs touch -mt `gdate "+%Y%m%d" --date="2 days ago"`0000
  echo

  echo "=== For re-running version-specific cache cleanup ==="
  echo "When was the last time fileHashes/fileHashes.lock was modified?"
  find ~/.gradle/caches -name fileHashes.lock -maxdepth 3 -exec stat -f '%Sm %N' '{}' +
  echo
  echo " - Which of these were within the last two days?"
  find ~/.gradle/caches -name fileHashes.lock -maxdepth 3 -newermt $(gdate "+%Y%m%d" --date="2 days ago") -exec stat -f '%Sm %N' '{}' +
  echo
  echo " - Running 'touch -mt `gdate "+%Y%m%d" --date="2 days ago"`0000 <path>/fileHashes.lock'"
  find ~/.gradle/caches -name fileHashes.lock -maxdepth 3 -newermt $(gdate "+%Y%m%d" --date="2 days ago") | xargs touch -mt `gdate "+%Y%m%d" --date="2 days ago"`0000
  echo
}

we_can_expect
setup_for_re_running

# Before changes
list_distribution_modified_times
echo "=== Before we run ./gradlew clean build ==="
list_cache_files
echo

# Run Gradle daemon and stop it gracefully
gw
gw clean build -x test
gw --stop -i
echo

# After changes
echo "=== After stopping the Gradle daemon ==="
list_cache_files
