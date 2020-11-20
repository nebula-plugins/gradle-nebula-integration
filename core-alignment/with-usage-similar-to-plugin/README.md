## Issue with similar usage to the Resolution Rules plugin

I am seeing there’s a change from Gradle 6.7.1 to 6.8-20201117230037+0000 and perhaps more narrowed down, from working gradle-6.8-milestone-1 to not-working gradle-6.8-milestone-2 where core alignment is having an issue.

Specifically, there is an issue when there is a copied and resolved configuration, which results in alignment rules not getting applied.

#### Output with 6.8-milestone-1
```
./gradlew  dependencies --configuration compileClasspath 
```

```
compileClasspath - Compile classpath for source set 'main'.
+--- test.nebula:a:1.0.0 -> 1.2.0
+--- test.nebula:b:1.2.0
\--- test.nebula:c:0.42.0 -> 1.2.0
```
These are aligned.

#### Output with 6.8-milestone-2
```
./gradlew  dependencies --configuration compileClasspath 
```

```
compileClasspath - Compile classpath for source set 'main'.
+--- test.nebula:a:1.0.0
+--- test.nebula:b:1.2.0
\--- test.nebula:c:0.42.0
```
These are not aligned.

This issue is not seen when the `rules.json` file and the configuration below is not added, which is why many core alignment tests outside of this plugin are succeeding.
```
dependencies {
    resolutionRules files('rules.json')
}
```

In this case, the file contents do not seem to matter. I have them as:
```
{
  "deny": [], "reject": [], "substitute": [], "replace": [], "align": []
}
```
