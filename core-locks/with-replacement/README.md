# Core locking with a replacement and core alignment

```
./gradlew dependencyInsight --dependency log4j

> Task :dependencyInsight
org.slf4j:log4j-over-slf4j:1.7.10
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
      - By constraint : dependency was locked to version '1.7.10'
      - By constraint : belongs to platform aligned-group:org.slf4j:1.7.10
      - Forced
      - Selected by rule : ✭ replacement - use slf4j in place of log4j

log4j:log4j:1.2.16 -> org.slf4j:log4j-over-slf4j:1.7.10
\--- org.apache.zookeeper:zookeeper:3.4.9
     \--- compileClasspath

org.slf4j:log4j-over-slf4j:1.7.10
\--- compileClasspath

org.slf4j:log4j-over-slf4j:{strictly 1.7.10} -> 1.7.10
\--- compileClasspath

org.slf4j:slf4j-log4j12:1.7.10
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
      - By constraint : dependency was locked to version '1.7.10'
      - By ancestor
      - By constraint : belongs to platform aligned-group:org.slf4j:1.7.10
      - Forced

org.slf4j:slf4j-log4j12:{strictly 1.7.10} -> 1.7.10
\--- compileClasspath

org.slf4j:slf4j-log4j12:1.6.1 -> 1.7.10
\--- org.apache.zookeeper:zookeeper:3.4.9
     \--- compileClasspath
```