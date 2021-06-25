# Core alignment with a substitution and a force
## When a substitution is triggered from a transitive dependency and forces are on all aligned dependencies

We can run
```
./gradlew dependencyInsight --dependency test.nebula
```
and then we see:
```
> Task :dependencyInsight
test.nebula:a:1.1.0 FAILED
   Selection reasons:
      - Forced
      - By constraint : belongs to platform aligned-group:test.nebula:1.1.0
   Failures:
      - Could not resolve test.nebula:a:1.1.0.
          - Multiple forces on different versions for virtual platform aligned-group:test.nebula

test.nebula:a:1.1.0 FAILED
\--- compileClasspath

test.nebula:a:1.2.0 FAILED
   Selection reasons:
      - Forced
      - Selected by rule : substitution from 'test.nebula:a:1.2.0' to 'test.nebula:a:1.3.0'
   Failures:
      - Could not resolve test.nebula:a:1.2.0. (already reported)

test.nebula:a:1.2.0 FAILED
\--- test.other:z:1.0.0
     \--- compileClasspath

test.nebula:b:1.0.0 (forced) FAILED
   Failures:
      - Could not resolve test.nebula:b:1.0.0. (already reported)

test.nebula:b:1.0.0 FAILED
\--- compileClasspath

test.nebula:c:1.0.0 (forced) FAILED
   Failures:
      - Could not resolve test.nebula:c:1.0.0. (already reported)

test.nebula:c:1.0.0 FAILED
\--- compileClasspath
```
