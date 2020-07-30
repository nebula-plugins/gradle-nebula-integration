# Multiple Aligned Platforms

When running `dependencyInsight` on this project with 2 replacements and an alignment, then I see:

```
./gradlew dependencyInsight --dependency to
```

```
> Task :dependencyInsight
to:new-apricot:1.1.0
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
      - By constraint : belongs to platform aligned-group:to:1.1.0
      - By constraint : belongs to platform aligned-group:to:1.0.0
      - By conflict resolution : between versions 1.1.0 and 1.0.0

from:apricot:1.0.0 -> to:new-apricot:1.1.0
\--- compileClasspath

to:new-berry:1.1.0
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
      - By constraint : belongs to platform aligned-group:to:1.1.0
      - Selected by rule : ✭ replacement - The group & id changed

from:berry:1.0.0 -> to:new-berry:1.1.0
\--- compileClasspath

to:new-apricot:1.0.0 -> 1.1.0
\--- compileClasspath

to:new-berry:1.1.0
\--- compileClasspath
```

Where we can see the contents `belongs to platform aligned-group` twice, indicating that this dependency matches 2 platform versions.


However, when the dependenies are re-ordered, the the output looks like:

```
./gradlew dependencyInsight --dependency to -DalternateOrdering=true
```

```
> Task :dependencyInsight
to:new-apricot:1.1.0
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
      - By constraint : belongs to platform aligned-group:to:1.1.0
      - Selected by rule : ✭ replacement - The group & id changed

from:apricot:1.0.0 -> to:new-apricot:1.1.0
\--- compileClasspath

to:new-berry:1.1.0
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
      - By constraint : belongs to platform aligned-group:to:1.1.0
      - By conflict resolution : between versions 1.1.0 and 1.0.0

from:berry:1.0.0 -> to:new-berry:1.1.0
\--- compileClasspath

to:new-apricot:1.1.0
\--- compileClasspath

to:new-berry:1.0.0 -> 1.1.0
\--- compileClasspath
```

After this re-ordering, then the dependencies only match a single platform version.
