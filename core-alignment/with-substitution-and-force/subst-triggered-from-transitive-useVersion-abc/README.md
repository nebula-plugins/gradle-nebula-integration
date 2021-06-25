# Core alignment with a substitution and useVersion
## When a substitution is triggered from a transitive dependency and useVersion is used for all aligned dependencies

We can run
```
./gradlew dependencyInsight --dependency test.nebula
```
and then we see:
```
> Task :dependencyInsight
test.nebula:a:1.1.0
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
      - Selected by rule : using useVersion to set this dependency version to 1.1.0
      - By constraint : belongs to platform aligned-group:test.nebula:1.1.0
      - Selected by rule : substitution from 'test.nebula:a:1.2.0' to 'test.nebula:a:1.3.0'

test.nebula:a:1.1.0
\--- compileClasspath

test.nebula:a:1.2.0 -> 1.1.0
\--- test.other:z:1.0.0
     \--- compileClasspath

test.nebula:b:1.1.0
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
      - Selected by rule : using useVersion to set this dependency version to 1.1.0
      - By constraint : belongs to platform aligned-group:test.nebula:1.1.0

test.nebula:b:1.0.0 -> 1.1.0
\--- compileClasspath

test.nebula:c:1.1.0
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
      - Selected by rule : using useVersion to set this dependency version to 1.1.0
      - By constraint : belongs to platform aligned-group:test.nebula:1.1.0

test.nebula:c:1.0.0 -> 1.1.0
\--- compileClasspath
```
