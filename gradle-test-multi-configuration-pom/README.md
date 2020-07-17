# gradle-test-multi-configuration-pom

Given the following POM file: 

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>netflix</groupId>
    <artifactId>mymodule</artifactId>
    <version>1.0.0</version>
    <dependencies>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>19.0</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>19.0</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</project>

```

If we add this module as a dependency 

```
plugins {
    id 'java'
}

repositories {
    jcenter()
    maven {
        url = 'mavenRepo'
    }
}

dependencies {
    implementation 'netflix:mymodule:1.0.0'
}
```

When running `./gradlew dependencies --configuration compileClasspath`, the result is:

```
------------------------------------------------------------
Root project
------------------------------------------------------------

compileClasspath - Compile classpath for source set 'main'.
\--- netflix:mymodule:1.0.0
```

When running `./gradlew dependencies --configuration runtimeClasspath`, the result is:

```
> Task :dependencies

------------------------------------------------------------
Root project
------------------------------------------------------------

runtimeClasspath - Runtime classpath of source set 'main'.
\--- netflix:mymodule:1.0.0
     \--- com.google.guava:guava:19.0
```

It seems that Gradle takes the last occurrence instead of the most meaningful which in this case is `compile`

Switching the order of the dependencies in the pom file has proper results: 

```
> Task :dependencies

------------------------------------------------------------
Root project
------------------------------------------------------------

compileClasspath - Compile classpath for source set 'main'.
\--- netflix:mymodule:1.0.0
     \--- com.google.guava:guava:19.0

```
