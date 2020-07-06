# Core alignment with forced direct dependency

A project using core alignment with a forced dependency can run into the case where the forced dependency version is not the chosen version.

In this case, a note to the user or a build failure similar to multiple strict dependencies conflicting would be ideal.

## Setup

- Using core alignment for a group `test.nebula`
- Using a substitution rule that gets triggered to substitute the dependencies `test.nebula:a:1.2.0 -> 1.3.0`, `test.nebula:b:1.2.0 -> 1.3.0`, and `test.nebula:c:1.2.0 -> 1.3.0`
- a direct dependency force declaration for `test.nebula:a` on the bad version `1.2.0` such as:
    ```
    implementation('test.nebula:a:1.2.0') {
        force = true // force to bad version triggers a substitution
    }
    implementation 'test.nebula:b:1.0.0' // added for alignment
    implementation 'test.nebula:c:1.0.0' // added for alignment
    ```

## Observed result

```
./gradlew dependencies --configuration compileClasspath
```

```
compileClasspath - Compile classpath for source set 'main'.
+--- test.nebula:a:1.2.0 -> 1.3.0
+--- test.nebula:b:1.0.0 -> 1.3.0
\--- test.nebula:c:1.0.0 -> 1.3.0

BUILD SUCCESSFUL
```
