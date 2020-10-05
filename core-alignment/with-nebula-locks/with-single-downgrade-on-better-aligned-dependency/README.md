# Example of version downgrades with core alignment and Nebula locking: with a single downgrade

This example shows a good-option of using a critical single-downgraded version which was added as a direct dependency:
```
configurations.all {
    resolutionStrategy {
        resolutionStrategy.force 'com.fasterxml.jackson.core:jackson-core:2.9.+'
    }
}
dependencies {
    implementation 'com.fasterxml.jackson.core:jackson-core:2.10.5'
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

When you run dependencyInsight once more, then this succeeds.

```
./gradlew dependencyInsight --dependency jackson
```
```
BUILD SUCCESSFUL
```
