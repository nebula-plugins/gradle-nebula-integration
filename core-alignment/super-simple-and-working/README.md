Super simple example of working alignment

Currently using Gradle 6.8-milestone-2

```
./gradlew dependencies --configuration compileClasspath
```
shows
```
> Task :dependencies

------------------------------------------------------------
Root project
------------------------------------------------------------

compileClasspath - Compile classpath for source set 'main'.
+--- test.nebula:a:1.0.0 -> 1.2.0
+--- test.nebula:b:1.2.0
\--- test.nebula:c:0.42.0 -> 1.2.0
```
