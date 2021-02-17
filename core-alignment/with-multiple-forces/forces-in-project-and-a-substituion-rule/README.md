# Project with multiple forces coming from a project and a substitution rule

## Context

When there are multiple forces acting upon a dependency which is part of an aligned group, it would be great if the outputs from `build` and/or `dependencyInsight` would display specifically which versions are forced and where this is taking place, so that folks can more easily fix these issues without having in-depth knowledge of what plugins may be doing behind the scenes.

## Current behavior:

When there are multiple forces acting upon a dependency which is part of an aligned group and there is a substitution rule for only a single module, then the project fails with an error message.

```
❯ ./gradlew build

FAILURE: Build failed with an exception.

* What went wrong:
Could not determine the dependencies of task ':compileJava'.
> Could not resolve all task dependencies for configuration ':compileClasspath'.
   > Could not resolve apricot:apricot-apache:2.0.0.
     Required by:
         project : > test.nebula:b:1.0.0
      > Multiple forces on different versions for virtual platform aligned-platform:apricot
   > Could not resolve apricot:apricot-aws:2.0.0.
     Required by:
         project : > test.nebula:b:1.0.0
      > Multiple forces on different versions for virtual platform aligned-platform:apricot
   > Could not resolve apricot:apricot-core:2.0.0.
     Required by:
         project : > test.nebula:b:1.0.0
      > Multiple forces on different versions for virtual platform aligned-platform:apricot
   > Could not resolve apricot:apricot-apache:4.0.0.
     Required by:
         project : > test.nebula:d:1.0.0
      > Multiple forces on different versions for virtual platform aligned-platform:apricot
   > Could not resolve apricot:apricot-aws:4.0.0.
     Required by:
         project : > test.nebula:d:1.0.0
      > Multiple forces on different versions for virtual platform aligned-platform:apricot
   > Could not resolve apricot:apricot-core:4.0.0.
     Required by:
         project : > test.nebula:d:1.0.0
      > Multiple forces on different versions for virtual platform aligned-platform:apricot
   > Could not resolve apricot:apricot-apache:5.0.0.
     Required by:
         project : > test.nebula:e:1.0.0
      > Multiple forces on different versions for virtual platform aligned-platform:apricot
   > Could not resolve apricot:apricot-aws:5.0.0.
     Required by:
         project : > test.nebula:e:1.0.0
      > Multiple forces on different versions for virtual platform aligned-platform:apricot
   > Could not resolve apricot:apricot-core:5.0.0.
     Required by:
         project : > test.nebula:e:1.0.0
      > Multiple forces on different versions for virtual platform aligned-platform:apricot
```

While this message shows the dependencies with an issue, information about what exactly the issue is and how it can be fixed is not clear.

When more information is requested via `dependencyInsight`, we see the following:
```
❯ ./gradlew dependencyInsight --dependency apricot:apricot-apache

> Task :dependencyInsight
apricot:apricot-apache:2.0.0 (forced) FAILED
   Failures:
      - Could not resolve apricot:apricot-apache:2.0.0.
          - Multiple forces on different versions for virtual platform aligned-platform:apricot

apricot:apricot-apache:2.0.0 FAILED
\--- test.nebula:b:1.0.0
     \--- compileClasspath

apricot:apricot-apache:4.0.0 FAILED
   Selection reasons:
      - Forced
      - By constraint : belongs to platform aligned-platform:apricot:4.0.0
   Failures:
      - Could not resolve apricot:apricot-apache:4.0.0. (already reported)

apricot:apricot-apache:4.0.0 FAILED
\--- test.nebula:d:1.0.0
     \--- compileClasspath

apricot:apricot-apache:5.0.0 (forced) FAILED
   Failures:
      - Could not resolve apricot:apricot-apache:5.0.0. (already reported)

apricot:apricot-apache:5.0.0 FAILED
\--- test.nebula:e:1.0.0
     \--- compileClasspath
```

And
```
❯ ./gradlew dependencyInsight --dependency apricot:apricot-aws

> Task :dependencyInsight
apricot:apricot-aws:2.0.0 (forced) FAILED
   Failures:
      - Could not resolve apricot:apricot-aws:2.0.0.
          - Multiple forces on different versions for virtual platform aligned-platform:apricot

apricot:apricot-aws:2.0.0 FAILED
\--- test.nebula:b:1.0.0
     \--- compileClasspath

apricot:apricot-aws:4.0.0 FAILED
   Selection reasons:
      - Forced
      - By constraint : belongs to platform aligned-platform:apricot:4.0.0
   Failures:
      - Could not resolve apricot:apricot-aws:4.0.0. (already reported)

apricot:apricot-aws:4.0.0 FAILED
\--- test.nebula:d:1.0.0
     \--- compileClasspath

apricot:apricot-aws:5.0.0 (forced) FAILED
   Failures:
      - Could not resolve apricot:apricot-aws:5.0.0. (already reported)

apricot:apricot-aws:5.0.0 FAILED
\--- test.nebula:e:1.0.0
     \--- compileClasspath
```

And
```
❯ ./gradlew dependencyInsight --dependency apricot:apricot-core

> Task :dependencyInsight
apricot:apricot-core:2.0.0 FAILED
   Selection reasons:
      - Forced
      - Selected by rule : substitution from 'apricot:apricot-core:2.0.0' to 'apricot:apricot-core:3.0.0'
   Failures:
      - Could not resolve apricot:apricot-core:2.0.0.
          - Multiple forces on different versions for virtual platform aligned-platform:apricot

apricot:apricot-core:2.0.0 FAILED
\--- test.nebula:b:1.0.0
     \--- compileClasspath

apricot:apricot-core:4.0.0 FAILED
   Selection reasons:
      - Forced
      - By constraint : belongs to platform aligned-platform:apricot:4.0.0
   Failures:
      - Could not resolve apricot:apricot-core:4.0.0. (already reported)

apricot:apricot-core:4.0.0 FAILED
\--- test.nebula:d:1.0.0
     \--- compileClasspath

apricot:apricot-core:5.0.0 (forced) FAILED
   Failures:
      - Could not resolve apricot:apricot-core:5.0.0. (already reported)

apricot:apricot-core:5.0.0 FAILED
\--- test.nebula:e:1.0.0
     \--- compileClasspath
```

In this case, we can see that there is a substitution of a single module in an aligned group, which is normally fine since it will bump up all of the dependencies. However, it acts as a force in this case:

```
configurations.all {
    resolutionStrategy.dependencySubstitution.all {
        def substituteFromVersion = "2.0.0"
        def substituteToVersion = "3.0.0"
        def substitutionReason = "substitution from '${it.requested.group}:${it.requested.module}:$substituteFromVersion' to '${it.requested.group}:${it.requested.module}:$substituteToVersion'"
        def selector = VERSION_SCHEME.parseSelector(substituteFromVersion)
        if (it.requested.group.startsWith("apricot") && it.requested.module.equals("apricot-core") && selector.accept(it.requested.version)) {
            it.useTarget("${it.requested.group}:${it.requested.module}:${substituteToVersion}", substitutionReason)
        }
    }
}
```

But from the messages seen, it's not clear what is causing the multiple forces issues.

### Side note: a fix to resolve the multiple forces issue in the build

As a solution to get out of the multiple forces, we can add something like:
```
configurations.all {
    resolutionStrategy {
        eachDependency { DependencyResolveDetails details ->
            if (details.requested.group.startsWith('apricot')) {
                details.because("pinned to 4.0.0")
                      .useVersion "4.0.0"
            }
        }
    }
}
```
Instead of:
```
configurations.all {
    resolutionStrategy {
        force 'apricot:apricot-apache:4.0.0'
        force 'apricot:apricot-aws:4.0.0'
        force 'apricot:apricot-core:4.0.0'
    }
}
```
In which case we get a successful resolution:
```
❯ ./gradlew dependencies --configuration compileClasspath

> Task :dependencies

------------------------------------------------------------
Root project
------------------------------------------------------------

compileClasspath - Compile classpath for source set 'main'.
+--- test.nebula:b:1.0.0
|    +--- apricot:apricot-apache:2.0.0 -> 4.0.0
|    +--- apricot:apricot-aws:2.0.0 -> 4.0.0
|    \--- apricot:apricot-core:2.0.0 -> 4.0.0
+--- test.nebula:d:1.0.0
|    +--- apricot:apricot-apache:4.0.0
|    +--- apricot:apricot-aws:4.0.0
|    \--- apricot:apricot-core:4.0.0
\--- test.nebula:e:1.0.0
     +--- apricot:apricot-apache:5.0.0 -> 4.0.0
     +--- apricot:apricot-aws:5.0.0 -> 4.0.0
     \--- apricot:apricot-core:5.0.0 -> 4.0.0
```

## Ideal behavior

It would be preferable to have the failed dependencies list something like:

```
Multiple forces on different versions for virtual platform aligned-platform:com.google.inject
Found forces for:
- apricot:apricot-core:3.0.0 from substitution from 'apricot:apricot-core:2.0.0' to 'apricot:apricot-core:3.0.0'
- apricot:apricot-core:4.0.0 from ${rootProject}/build.gradle
```
