Executing `./gradlew dI --dependency org.springframework:spring-web --configuration runtimeClasspath` results in

```
> Task :dependencyInsight
org.springframework:spring-web:5.3.1
   variant "runtimeElements" [
      org.gradle.category                 = library
      org.gradle.dependency.bundling      = external
      org.gradle.jvm.version              = 8
      org.gradle.libraryelements          = jar
      org.gradle.usage                    = java-runtime
      org.jetbrains.kotlin.localToProject = public (not requested)
      org.jetbrains.kotlin.platform.type  = jvm (not requested)
      org.gradle.status                   = release (not requested)
   ]

org.springframework:spring-web:5.3.1
\--- netflix:foo-client:1.0.0
     \--- runtimeClasspath

A web-based, searchable dependency report is available by adding the --scan option.
```

While doing a dependency insight on compile classpath fails:

```
./gradlew dI --dependency org.springframework:spring-web --configuration compileClasspath                   

> Task :dependencyInsight
org.springframework:spring-web:5.3.0 FAILED
   Failures:
      - Could not resolve org.springframework:spring-web:{strictly 5.3.0}.
          - Cannot find a version of 'org.springframework:spring-web' that satisfies the version constraints:
               Dependency path ':consistent-resolution-with-rich-versions-tests:unspecified' --> 'org.springframework:spring-web:{strictly 5.3.0}'
               Constraint path ':consistent-resolution-with-rich-versions-tests:unspecified' --> 'org.springframework:spring-web:{strictly 5.3.1}' because of the following reason: version resolved in configuration ':runtimeClasspath' by consistent resolution

org.springframework:spring-web:{strictly 5.3.0} -> 5.3.0 FAILED
\--- compileClasspath

org.springframework:spring-web:5.3.1 FAILED
   Selection reasons:
      - By constraint : version resolved in configuration ':runtimeClasspath' by consistent resolution
   Failures:
      - Could not resolve org.springframework:spring-web:{strictly 5.3.1}. (already reported)

org.springframework:spring-web:{strictly 5.3.1} -> 5.3.1 FAILED
\--- compileClasspath

A web-based, searchable dependency report is available by adding the --scan option.

```