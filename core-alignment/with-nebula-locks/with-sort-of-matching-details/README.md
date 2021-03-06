# Example of version downgrades with core alignment and Nebula locking: with sort-of matching details

Here is what folks may try to add to downgrade a dependency:

```
configurations.all {
    resolutionStrategy {
        eachDependency { DependencyResolveDetails details ->
            if (details.requested.group.startsWith('com.fasterxml.jackson')) {
                details.useVersion "2.9.+"
            }
        }
    }
}
```

When you initially run `dependencyInsight`, then dependencies resolve correctly.

```
./gradlew dependencyInsight --dependency jackson -PdependencyLock.ignore=true
```
```
BUILD SUCCESSFUL
```

When you generate Nebula locks, then this succeeds.

```
./gradlew generateLock saveLock
```
```
BUILD SUCCESSFUL
```

When you run dependencyInsight once more, then there are multiple forces which fail.

```
./gradlew dependencyInsight --dependency jackson
```
```
> Task :dependencyInsight
com.fasterxml.jackson.core:jackson-annotations:2.10.5 FAILED
   Selection reasons:
      - Selected by rule
      - Selected by rule : com.fasterxml.jackson.core:jackson-annotations locked to 2.9.10
                with reasons: nebula.dependency-lock locked with: dependencies.lock
   Failures:
      - Could not resolve com.fasterxml.jackson.core:jackson-annotations:2.10.5.
          - Multiple forces on different versions for virtual platform aligned-platform:rules-0-for-com.fasterxml.jackson.core-or-dataformat-or-datatype-or-jaxrs-or-jr-or-module
```

Have you thought about a concept to downgrade an aligned group?
