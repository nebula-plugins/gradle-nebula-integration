# Project with multiple forces coming from a project and a plugin

## Context

When there are multiple forces acting upon a dependency which is part of an aligned group, it would be great if the outputs from `build` and/or `dependencyInsight` would display specifically which versions are forced and where this is taking place, so that folks can more easily fix these issues without having in-depth knowledge of what plugins may be doing behind the scenes.

## Current behavior:

When there are multiple forces acting upon a dependency which is part of an aligned group, then the project fails with an error message.

```
❯ ./gradlew build
> Task :compileGroovy FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':compileGroovy'.
> Could not resolve all files for configuration ':compileClasspath'.
   > Could not resolve com.google.inject:guice:3.0.
     Required by:
         project : > test.nebula:a:1.0.0
      > Multiple forces on different versions for virtual platform aligned-platform:com.google.inject
   > Could not resolve com.google.inject.extensions:guice-assistedinject:4.1.0.
     Required by:
         project : > test.nebula:b:1.0.0
         project : > test.nebula:c:1.0.0 > com.google.inject.extensions:guice-grapher:4.1.0
      > Multiple forces on different versions for virtual platform aligned-platform:com.google.inject
   > Could not resolve com.google.inject:guice:4.1.0.
     Required by:
         project : > test.nebula:b:1.0.0 > com.google.inject.extensions:guice-assistedinject:4.1.0
         project : > test.nebula:c:1.0.0 > com.google.inject.extensions:guice-grapher:4.1.0
         project : > test.nebula:c:1.0.0 > com.google.inject.extensions:guice-grapher:4.1.0 > com.google.inject.extensions:guice-multibindings:4.1.0
      > Multiple forces on different versions for virtual platform aligned-platform:com.google.inject
```

While this message shows the dependencies with an issue (`com.google.inject.extensions:guice-assistedinject` and `com.google.inject:guice`), information about what exactly the issue is and how it can be fixed is not clear.

When more information is requested via `dependencyInsight`, we see the following:
```
❯ ./gradlew dependencyInsight --dependency com.google.inject

> Task :dependencyInsight
com.google.inject:guice:3.0 (forced) FAILED
   Failures:
      - Could not resolve com.google.inject:guice:3.0.
          - Multiple forces on different versions for virtual platform aligned-platform:com.google.inject

com.google.inject:guice:3.0 FAILED
\--- test.nebula:a:1.0.0
     \--- compileClasspath

com.google.inject:guice:4.1.0 (forced) FAILED
   Failures:
      - Could not resolve com.google.inject:guice:4.1.0. (already reported)

com.google.inject:guice:4.1.0 FAILED
+--- com.google.inject.extensions:guice-grapher:4.1.0
|    \--- test.nebula:c:1.0.0
|         \--- compileClasspath
\--- com.google.inject.extensions:guice-multibindings:4.1.0
     \--- com.google.inject.extensions:guice-grapher:4.1.0 (*)

com.google.inject.extensions:guice-assistedinject:4.1.0 (forced) FAILED
   Failures:
      - Could not resolve com.google.inject.extensions:guice-assistedinject:4.1.0. (already reported)

com.google.inject.extensions:guice-assistedinject:4.1.0 FAILED
+--- com.google.inject.extensions:guice-grapher:4.1.0
|    \--- test.nebula:c:1.0.0
|         \--- compileClasspath
\--- test.nebula:b:1.0.0
     \--- compileClasspath

com.google.inject.extensions:guice-grapher:4.1.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes+resources)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - By constraint : belongs to platform aligned-platform:com.google.inject:4.1.0
      - Forced

com.google.inject.extensions:guice-grapher:4.1.0
\--- test.nebula:c:1.0.0
     \--- compileClasspath

com.google.inject.extensions:guice-multibindings:4.1.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes+resources)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - By constraint : belongs to platform aligned-platform:com.google.inject:4.1.0
      - Forced

com.google.inject.extensions:guice-multibindings:4.1.0
\--- com.google.inject.extensions:guice-grapher:4.1.0
     \--- test.nebula:c:1.0.0
          \--- compileClasspath

(*) - dependencies omitted (listed previously)
```

While this shows the failed dependencies (`com.google.inject.extensions:guice-assistedinject` and `com.google.inject:guice`), we can look at each message individually:

For `com.google.inject:guice`:
```
com.google.inject:guice:3.0 (forced) FAILED
   Failures:
      - Could not resolve com.google.inject:guice:3.0.
          - Multiple forces on different versions for virtual platform aligned-platform:com.google.inject

com.google.inject:guice:3.0 FAILED
\--- test.nebula:a:1.0.0
     \--- compileClasspath

com.google.inject:guice:4.1.0 (forced) FAILED
   Failures:
      - Could not resolve com.google.inject:guice:4.1.0. (already reported)
```

It looks like there is a force on `4.1.0` and possibly on `3.0`. In fact, there is a force on `4.1.0` from a plugin and on `4.0` inside of the project itself.

For `com.google.inject.extensions:guice-assistedinject`:
```
com.google.inject.extensions:guice-assistedinject:4.1.0 (forced) FAILED
   Failures:
      - Could not resolve com.google.inject.extensions:guice-assistedinject:4.1.0. (already reported)

com.google.inject.extensions:guice-assistedinject:4.1.0 FAILED
+--- com.google.inject.extensions:guice-grapher:4.1.0
|    \--- test.nebula:c:1.0.0
|         \--- compileClasspath
\--- test.nebula:b:1.0.0
     \--- compileClasspath
```

This looks like there is a force on `4.1.0`, which is true, but that this has failed and it's not clear why.

Finally, we can look at the successfully resolved dependencies:
```
com.google.inject.extensions:guice-grapher:4.1.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes+resources)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - By constraint : belongs to platform aligned-platform:com.google.inject:4.1.0
      - Forced

com.google.inject.extensions:guice-grapher:4.1.0
\--- test.nebula:c:1.0.0
     \--- compileClasspath

com.google.inject.extensions:guice-multibindings:4.1.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes+resources)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - By constraint : belongs to platform aligned-platform:com.google.inject:4.1.0
      - Forced

com.google.inject.extensions:guice-multibindings:4.1.0
\--- com.google.inject.extensions:guice-grapher:4.1.0
     \--- test.nebula:c:1.0.0
          \--- compileClasspath
```
These end up resolving successfully to `4.1.0` and show that they are forced, however there are no forces other than the aligned platform itself.

## Ideal behavior

It would be preferable to have the failed dependencies list something like:

```
Multiple forces on different versions for virtual platform aligned-platform:com.google.inject
Found forces for:
- com.google.inject:guice:4.1.0 from MyPluginWithForces
- com.google.inject:guice:4.0 from ${rootProject}/build.gradle
```
