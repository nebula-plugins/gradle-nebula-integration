Excludes within BOM
===================

A dependency that is excluded within a bom ends up brought in anyway.

BOM
---

```
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
            <version>5.0.7.RELEASE</version>
            
            <exclusions>
                <exclusion>
                    <groupId>org.yaml</groupId>
                    <artifactId>snakeyaml</artifactId>
                </exclusion>
            </exclusions>
            
        </dependency>
    </dependencies>
</dependencyManagement>
```

build.gradle
------------

```
dependencies {
  implementation 'nebulatest:nebulabom:1.0.0'       // recommends a version of spring-beans excluding snakeyaml
  implementation 'org.springframework:spring-beans' // expecting snakeyaml to be excluded
  implementation 'org.yaml:snakeyaml:1.19'          // snakeyaml loses to spring-bean's snakeyaml
}
```

Dependencies Resolved
---------------------

```
$ ./gradlew -q dependencies --configuration compileClasspath

------------------------------------------------------------
Root project
------------------------------------------------------------

compileClasspath - Compile classpath for source set 'main'.
+--- nebulatest:nebulabom:1.0.0
|    \--- org.springframework:spring-beans:5.0.7.RELEASE
|         +--- org.springframework:spring-core:5.0.7.RELEASE
|         |    \--- org.springframework:spring-jcl:5.0.7.RELEASE
|         \--- org.yaml:snakeyaml:1.20                           <-- expected to be excluded
+--- org.springframework:spring-beans -> 5.0.7.RELEASE (*)
\--- org.yaml:snakeyaml:1.19 -> 1.20

(*) - dependencies omitted (listed previously)
```
