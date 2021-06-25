# Core alignment with a substitution and a force
## When a substitution is triggered from a direct dependency and forces are on one dependency

We can run
```
./gradlew dependencyInsight --dependency test.nebula
```
and then we see:
```
> Task :dependencyInsight
test.nebula:a:1.3.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - By constraint : belongs to platform aligned-group:test.nebula:1.3.0
      - Forced
      - By conflict resolution : between versions 1.3.0 and 1.1.0

test.nebula:a:1.1.0 -> 1.3.0
\--- compileClasspath

test.nebula:b:1.3.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - By constraint : belongs to platform aligned-group:test.nebula:1.3.0
      - Forced
      - By conflict resolution : between versions 1.3.0 and 1.0.0

test.nebula:b:1.0.0 -> 1.3.0
\--- compileClasspath

test.nebula:c:1.3.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - Forced
      - Selected by rule : substitution from 'test.nebula:c:1.2.0' to 'test.nebula:c:1.3.0'
      - By constraint : belongs to platform aligned-group:test.nebula:1.3.0

test.nebula:c:1.2.0 -> 1.3.0
\--- compileClasspath
```
