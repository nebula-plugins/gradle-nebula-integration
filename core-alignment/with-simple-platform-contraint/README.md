# Simple example of version downgrades with core alignment with a platform constraint

## Purpose

We may wish to align dependencies that specifically match the virtual platform. To do this, we may want to align to a static known version or request the latest possible version in a range/ dynamic version. For example, we may wish to continue getting updates to a dependency group up to the next major version, and we would like them all to be aligned. We then may request `major.+` or a specific range.

## Static aligned platform constraint
Given the following constraint:
```
dependencies {
    constraints {
        implementation ("test.nebula:test.nebula") {
            version { strictly ("1.0.0") }
            because("this version is required for compatibility")
        }
    }
}

```

And running:
```
./gradlew dependencies --configuration compileClasspath
```

Results in:
```
compileClasspath - Compile classpath for source set 'main'.
+--- test.nebula:a:1.0.0
\--- test.nebula:b:1.2.0 -> 1.0.0
```

Where everything is correctly aligned.

## Dynamic aligned platform constraint
Given the following constraint:
```
dependencies {
    constraints {
        implementation ("test.nebula:test.nebula") {
            version { strictly ("1.+") }
            because("this version is required for compatibility")
        }
    }
}
```

And running:
```
./gradlew dependencies --configuration compileClasspath
```

Results in:
```
compileClasspath - Compile classpath for source set 'main'.

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':dependencies'.
> Could not resolve all dependencies for configuration ':compileClasspath'.
   > fromIndex = -1
```

where the error comes from `org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.VirtualPlatformState.getCandidateVersions(VirtualPlatformState.java:94)` (link to [VirtualPlatformState](https://github.com/gradle/gradle/blob/v6.7.0/subprojects/dependency-management/src/main/java/org/gradle/api/internal/artifacts/ivyservice/resolveengine/graph/builder/VirtualPlatformState.java#L81-L94))

## Ranged aligned platform constraint
Given the following constraint:
```
dependencies {
    constraints {
        implementation ("test.nebula:test.nebula") {
            version { strictly ("[1.0.0,1.3.0]") }
            because("this version is required for compatibility")
        }
    }
}
```

And running:
```
./gradlew dependencies --configuration compileClasspath
```

Results in:
```
compileClasspath - Compile classpath for source set 'main'.

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':dependencies'.
> Could not resolve all dependencies for configuration ':compileClasspath'.
   > fromIndex = -1
```

## Static micro-patch aligned platform constraint

Such as seen with Jackson libraries, where a dependency but not all of them have a micro-patch version.

Given the following constraint:
```
dependencies {
    constraints {
        implementation ("test.nebula:test.nebula") {
            version { strictly ("1.2.0.1") }
            because("this version is required for compatibility")
        }
    }
}

```
And running:
```
./gradlew dependencies --configuration compileClasspath
```

Results in:
```
compileClasspath - Compile classpath for source set 'main'.
+--- test.nebula:a:1.0.0 -> 1.2.0.1
\--- test.nebula:b:1.2.0
```

Where everything is correctly aligned.
