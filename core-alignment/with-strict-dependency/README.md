# Core alignment with strict dependency

A project using core alignment with a strict dependency can run into the case where the strict dependency version is not the chosen version.

In this case, a note to the user or a build failure similar to multiple strict dependencies conflicting would be ideal.

## Setup

- Using core alignment for a group `test.nebula`
- Using a substitution rule that gets triggered to substitute the dependencies `test.nebula:a:1.2.0 -> 1.3.0`, `test.nebula:b:1.2.0 -> 1.3.0`, and `test.nebula:c:1.2.0 -> 1.3.0`
- a strict dependency declaration for `test.nebula:a` on the bad version `1.2.0` such as:
    ```
    implementation('test.nebula:a') {
        version { strictly '1.2.0' } // strict to bad version
    }
    implementation 'test.nebula:b:1.0.0'
    implementation 'test.nebula:c:1.0.0'
    ```

## Observed result

```
./gradlew dependencies --configuration compileClasspath
```

```
compileClasspath - Compile classpath for source set 'main'.
+--- test.nebula:a:{strictly 1.2.0} -> 1.3.0
+--- test.nebula:b:1.0.0 -> 1.3.0
\--- test.nebula:c:1.0.0 -> 1.3.0

BUILD SUCCESSFUL
```
