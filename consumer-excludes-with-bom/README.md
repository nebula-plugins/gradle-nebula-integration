Dependency Excludes with IMPROVED_POM_SUPPORT
=============================================

A dependency that is excluded from a first-order ends up used in conflict resolution in anyway when it is brought in transitively.


Without IMPROVED_POM_SUPPORT
----------------------------

```
$ ./gradlew -DimprovedPom=false -q dependencies --configuration compileClasspath

------------------------------------------------------------
Root project
------------------------------------------------------------

compileClasspath - Compile classpath for source set 'main'.
+--- org.springframework:spring-beans:5.0.6.RELEASE
|    \--- org.springframework:spring-core:5.0.6.RELEASE
|         \--- org.springframework:spring-jcl:5.0.6.RELEASE
\--- nebulatest:mylib:latest.release -> 1.0.1
     +--- org.yaml:snakeyaml:1.19
     \--- org.springframework:spring-beans:5.0.6.RELEASE (*)

(*) - dependencies omitted (listed previously)
```

With IMPROVED_POM_SUPPORT
-------------------------

```
$ ./gradlew -DimprovedPom=true -q dependencies --configuration compileClasspath

------------------------------------------------------------
Root project
------------------------------------------------------------

compileClasspath - Compile classpath for source set 'main'.
+--- org.springframework:spring-beans:5.0.6.RELEASE
|    +--- org.springframework:spring-core:5.0.6.RELEASE
|    |    \--- org.springframework:spring-jcl:5.0.6.RELEASE
|    \--- org.yaml:snakeyaml:1.20
\--- nebulatest:mylib:latest.release -> 1.0.1
     +--- org.yaml:snakeyaml:1.19 -> 1.20
     \--- org.springframework:spring-beans:5.0.6.RELEASE (*)

(*) - dependencies omitted (listed previously)
```
