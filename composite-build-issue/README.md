# Run without included build

Comment out `includeBuild '.build/my-plugin'` in settings.gradle.

Execute `./gradlew :client:dependencies`, you will see:

```
runtimeClasspath - Runtime classpath of source set 'main'.
+--- project :common
|    \--- com.google.guava:guava:19.0
\--- netflix.test:some-library:1.0.0
     \--- netflix.test:another-library:1.0.0
          \--- netflix.test:client:1.0.0 -> project :client (*)
```

`another-library` brings the client transitively and Gradle is smart enough to translate that the project itself

When `./gradlew compileJava` works as expected

# Run with included build

When the includeBuild is enabled, we see the same dependencies:

```
runtimeClasspath - Runtime classpath of source set 'main'.
+--- project :common
|    \--- com.google.guava:guava:19.0
\--- netflix.test:some-library:1.0.0
     \--- netflix.test:another-library:1.0.0
          \--- netflix.test:client:1.0.0 -> project :client (*)
```

However, the compilation task fails with:

```

FAILURE: Build failed with an exception.

* What went wrong:
Circular dependency between the following tasks:
:client:compileJava
\--- :client:compileJava (*)

(*) - details omitted (listed previously)

```

