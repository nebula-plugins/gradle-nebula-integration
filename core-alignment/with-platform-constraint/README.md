# Jackson dependency example of version downgrades with core alignment with a platform constraint

## Purpose

We may wish to align dependencies that specifically match the virtual platform. To do this, we may want to align to a static known version or request the latest possible version in a range/ dynamic version. For example, we may wish to continue getting updates to a dependency group up to the next major version, and we would like them all to be aligned. We then may request `major.+` or a specific range.

## Setup

We can add a constraint on the virtual platform and see different results, when given the following declared dependencies:

```
dependencies {
    resolutionRules files('rules.json') // to get the alignment rule
    implementation 'com.fasterxml.jackson.core:jackson-annotations:2.10.5'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.10.5'
    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.10.5'
    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.10.5'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-guava:2.10.5'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-joda:2.10.5'
    implementation 'com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.10.5'
}
```

## Static aligned platform constraint

Given the following constraint:

```
dependencies {
    constraints {
        implementation ("aligned-platform:rules-0-for-com.fasterxml.jackson.core-or-dataformat-or-datatype-or-jaxrs-or-jr-or-module") {
            version { strictly ("2.9.9") }
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
+--- com.fasterxml.jackson.core:jackson-annotations:2.10.5 -> 2.9.9
+--- com.fasterxml.jackson.core:jackson-databind:2.10.5 -> 2.9.9
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.0 -> 2.9.9
|    \--- com.fasterxml.jackson.core:jackson-core:2.9.9
+--- com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.10.5 -> 2.9.9
|    \--- com.fasterxml.jackson.core:jackson-core:2.9.9
+--- com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.10.5 -> 2.9.9
|    \--- com.fasterxml.jackson.core:jackson-core:2.9.9
+--- com.fasterxml.jackson.datatype:jackson-datatype-guava:2.10.5 -> 2.9.9
|    +--- com.google.guava:guava:18.0
|    +--- com.fasterxml.jackson.core:jackson-core:2.9.9
|    \--- com.fasterxml.jackson.core:jackson-databind:2.9.9 (*)
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.10.5 -> 2.9.9
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.0 -> 2.9.9
|    +--- com.fasterxml.jackson.core:jackson-core:2.9.9
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9 (*)
|    \--- joda-time:joda-time:2.7
\--- com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.10.5 -> 2.9.9
     +--- com.fasterxml.jackson.core:jackson-annotations:2.9.0 -> 2.9.9
     +--- com.fasterxml.jackson.core:jackson-core:2.9.9
     \--- com.fasterxml.jackson.core:jackson-databind:2.9.9 (*)
```

Where everything is correctly aligned.

## Dynamic aligned platform constraint

Given the following constraint:

```
dependencies {
    constraints {
        implementation ("aligned-platform:rules-0-for-com.fasterxml.jackson.core-or-dataformat-or-datatype-or-jaxrs-or-jr-or-module") {
            version { strictly ("2.9.+") }
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
        implementation ("aligned-platform:rules-0-for-com.fasterxml.jackson.core-or-dataformat-or-datatype-or-jaxrs-or-jr-or-module") {
            version { strictly ("[2.9.0,2.10.0]") }
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

Given the following constraint:

```
dependencies {
    constraints {
        implementation ("aligned-platform:rules-0-for-com.fasterxml.jackson.core-or-dataformat-or-datatype-or-jaxrs-or-jr-or-module") {
            version { strictly ("2.9.9.3") }
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
+--- com.fasterxml.jackson.core:jackson-annotations:2.10.5 -> 2.9.0
+--- com.fasterxml.jackson.core:jackson-databind:2.10.5 -> 2.9.9.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.0
|    \--- com.fasterxml.jackson.core:jackson-core:2.9.9
+--- com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.10.5
|    \--- com.fasterxml.jackson.core:jackson-core:2.10.5 -> 2.9.9
+--- com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.10.5
|    \--- com.fasterxml.jackson.core:jackson-core:2.10.5 -> 2.9.9
+--- com.fasterxml.jackson.datatype:jackson-datatype-guava:2.10.5
|    +--- com.google.guava:guava:20.0
|    +--- com.fasterxml.jackson.core:jackson-core:2.10.5 -> 2.9.9
|    \--- com.fasterxml.jackson.core:jackson-databind:2.10.5 -> 2.9.9.3 (*)
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.10.5
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.10.5 -> 2.9.0
|    +--- com.fasterxml.jackson.core:jackson-core:2.10.5 -> 2.9.9
|    +--- com.fasterxml.jackson.core:jackson-databind:2.10.5 -> 2.9.9.3 (*)
|    \--- joda-time:joda-time:2.9.9
\--- com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.10.5
     +--- com.fasterxml.jackson.core:jackson-annotations:2.10.5 -> 2.9.0
     +--- com.fasterxml.jackson.core:jackson-core:2.10.5 -> 2.9.9
     +--- com.fasterxml.jackson.core:jackson-databind:2.10.5 -> 2.9.9.3 (*)
     +--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.2
     |    \--- jakarta.activation:jakarta.activation-api:1.2.1
     \--- jakarta.activation:jakarta.activation-api:1.2.1
```

These results are not aligned and have versions like `2.9.0`,  `2.9.9.3`, and `2.10.5`. As well, this output does not give an indication that the results are strangely aligned between 2.9.x and 2.10.x

When running with `dependencyInsight`, then not all the dependencies show a message like:

```
By constraint : belongs to platform aligned-platform:rules-0-for-com.fasterxml.jackson.core-or-dataformat-or-datatype-or-jaxrs-or-jr-or-module:2.9.9.3
```

Although! If the dependencies are written differently, such as the following:

```
dependencies {
    resolutionRules files('rules.json')
    implementation 'com.fasterxml.jackson.core:jackson-annotations:2.9.9'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.9.9'
    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.9.9'
    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.9.9'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.9'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-joda:2.9.9'
    implementation 'com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.9.9'
}
```

Then this results in a more aligned result:

```
compileClasspath - Compile classpath for source set 'main'.
+--- com.fasterxml.jackson.core:jackson-annotations:2.9.9
+--- com.fasterxml.jackson.core:jackson-databind:2.9.9 -> 2.9.9.3
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.0 -> 2.9.9
|    \--- com.fasterxml.jackson.core:jackson-core:2.9.9
+--- com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.9.9
|    \--- com.fasterxml.jackson.core:jackson-core:2.9.9
+--- com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.9.9
|    \--- com.fasterxml.jackson.core:jackson-core:2.9.9
+--- com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.9
|    +--- com.google.guava:guava:18.0
|    +--- com.fasterxml.jackson.core:jackson-core:2.9.9
|    \--- com.fasterxml.jackson.core:jackson-databind:2.9.9 -> 2.9.9.3 (*)
+--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.9.9
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.0 -> 2.9.9
|    +--- com.fasterxml.jackson.core:jackson-core:2.9.9
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.9 -> 2.9.9.3 (*)
|    \--- joda-time:joda-time:2.7
\--- com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.9.9
     +--- com.fasterxml.jackson.core:jackson-annotations:2.9.0 -> 2.9.9
     +--- com.fasterxml.jackson.core:jackson-core:2.9.9
     \--- com.fasterxml.jackson.core:jackson-databind:2.9.9 -> 2.9.9.3 (*)
```
