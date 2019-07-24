Replication of an issue involving forcing and substituting dependencies. 

Here are the result when running `./gradlew dependencyInsight --dependency test.nebula`

With Gradle 5.4.1, we see the following, which resolves to `1.0.0`:
```
> Task :dependencyInsight
test.nebula:a:1.0.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.category            = library (not requested)

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - Forced
      - Selected by rule : bad version

test.nebula:a:1.0.0
\--- compileClasspath

test.nebula:a:1.1.0 -> 1.0.0
\--- test.nebula:brings-a-transitively:0.15.0
     \--- compileClasspath (requested test.nebula:brings-a-transitively:latest.release)

test.nebula:brings-a-transitively:0.15.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.category            = library (not requested)

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]

test.nebula:brings-a-transitively:latest.release -> 0.15.0
\--- compileClasspath

```

With Gradle 5.5.1, we see the following, which resolves to `1.1.2`:
```
> Task :dependencyInsight
test.nebula:a:1.1.2
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.category            = library (not requested)

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - Forced
      - Selected by rule : bad version

test.nebula:a:1.0.0 -> 1.1.2
\--- compileClasspath

test.nebula:a:1.1.0 -> 1.1.2
\--- test.nebula:brings-a-transitively:0.15.0
     \--- compileClasspath (requested test.nebula:brings-a-transitively:latest.release)

test.nebula:brings-a-transitively:0.15.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.category            = library (not requested)

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]

test.nebula:brings-a-transitively:latest.release -> 0.15.0
\--- compileClasspath
```