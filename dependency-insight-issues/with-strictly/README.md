# Dependency Insight with strictly

Up to Gradle 6.4, when I have a dependency with a `strict` version declaration and it's also brought in transitively, then the dependency insight reasoning shows "unknown" as the reason.

```
> Task :dependencyInsight
com.google.guava:guava:19.0 (unknown)
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]

com.google.guava:guava:{strictly 19.0} -> 19.0
\--- compileClasspath

com.google.guava:guava:23.0 -> 19.0
\--- test.nebula:brings-guava:1.0.0
     \--- compileClasspath

test.nebula:brings-guava:1.0.0
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]

test.nebula:brings-guava:1.0.0
\--- compileClasspath
```
