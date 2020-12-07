# consistent resolution with dependency substitution

when running `./gradlew dI --dependency mockito-all --configuration testRuntimeClasspath`, we get:

```
> Task :dependencyInsight
org.mockito:mockito-all:1.+ (selected by rule) FAILED
   Failures:
      - Could not resolve org.mockito:mockito-all:1.+.
          - Cannot find a version of 'org.mockito:mockito-core' that satisfies the version constraints:
               Dependency path ':consistent-resolution-with-substitution:unspecified' --> 'org.mockito:mockito-core:1.9.5'
               Dependency path ':consistent-resolution-with-substitution:unspecified' --> 'org.mockito:mockito-all:1.+'
               Constraint path ':consistent-resolution-with-substitution:unspecified' --> 'org.mockito:mockito-core:{strictly 1.9.5}' because of the following reason: version resolved in configuration ':runtimeClasspath' by consistent resolution

org.mockito:mockito-all:1.+ FAILED
\--- testRuntimeClasspath

A web-based, searchable dependency report is available by adding the --scan option.
```