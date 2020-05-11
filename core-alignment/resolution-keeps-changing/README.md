# Project where dependency resolution keeps changing

## Overview

Run the following to see differences in resolution with core Gradle alignment. The results are trackable because they are moved to a git-tracked folder and are then committed. 

If you see `nothing to commit, working tree clean`, then the current dependency resolution is the same as the previous one. The `interesting-plugin` changes between 2 versions, so this happens sometimes. In that case, run the following again, and you will see the still-changing dependency resolution once more.

```
./resolveAndStoreDependencies.sh && git commit -am "re-resolve dependencies" \
  && ./resolveAndStoreDependencies.sh && git commit -am "run resolveAndStoreDependencies and see a different resolution." \
  && ./resolveAndStoreDependencies.sh && git commit -am "run resolveAndStoreDependencies and see a different resolution." \
  && ./resolveAndStoreDependencies.sh && git commit -am "run resolveAndStoreDependencies and see a different resolution."
```

The differences will be that the `interesting-plugin` changes versions, which also influences the transitive dependencies.

Results with Nebula alignment are also run in these commands, and you can see the results do not change.

---------------------------

## Example output

When running `dependencyInsight`, I also see the following outputs:

Output 1:
```
./gradlew dependencyInsight --dependency interesting-plugin

> Configure project :
Using core Gradle alignment

> Task :dependencyInsight
berry:interesting-plugin:1.54.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      Requested attributes not found in the selected variant:
         org.gradle.usage               = java-api
         org.gradle.category            = library
         org.gradle.dependency.bundling = external
         org.gradle.libraryelements     = classes
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - By conflict resolution : between versions 1.54.0 and 1.26.0

berry:interesting-plugin:1.26.0 -> 1.54.0
\--- berry:data-util:1.61.0
     \--- berry:customer-model:1.797.0
          \--- berry:skiing-core:2.1121.0
               \--- berry:vmd-client:69.11.4
                    \--- compileClasspath
```

Output 2, upon re-running a few times:
```
./gradlew dependencyInsight --dependency interesting-plugin

> Configure project :
Using core Gradle alignment

> Task :dependencyInsight
berry:interesting-plugin:1.26.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      Requested attributes not found in the selected variant:
         org.gradle.usage               = java-api
         org.gradle.category            = library
         org.gradle.dependency.bundling = external
         org.gradle.libraryelements     = classes
         org.gradle.jvm.version         = 8
   ]

berry:interesting-plugin:1.26.0
\--- berry:data-util:1.61.0
     \--- berry:customer-model:1.797.0
          \--- berry:skiing-core:2.1121.0
               \--- berry:vmd-client:69.11.4
                    \--- compileClasspath
```

These should also show the same results when I run this command repeatedly.