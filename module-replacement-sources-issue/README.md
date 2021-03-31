Running `./gradlew build` results in

```

* What went wrong:
Execution failed for task ':compileJava'.
> Could not resolve all files for configuration ':compileClasspath'.
   > Could not find validation-api-2.0.2-sources.jar (jakarta.validation:jakarta.validation-api:2.0.2).
     Searched in the following locations:
         https://repo.maven.apache.org/maven2/jakarta/validation/jakarta.validation-api/2.0.2/validation-api-2.0.2-sources.jar

* Try:
Run with --stacktrace opti
```

Looking at dependency insight:

```
> Task :dependencyInsight
jakarta.validation:jakarta.validation-api:2.0.2
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
      - Selected by rule : jakarta is the new

jakarta.validation:jakarta.validation-api:2.0.2
\--- compileClasspath

javax.validation:validation-api:1.0.0.GA -> jakarta.validation:jakarta.validation-api:2.0.2
\--- com.google.gwt:gwt-user:2.8.2
     \--- compileClasspath

```


And then ` com.google.gwt:gwt-user:2.8.2` contains:

```
<dependency>
<groupId>javax.validation</groupId>
<artifactId>validation-api</artifactId>
</dependency>
<dependency>
<groupId>javax.validation</groupId>
<artifactId>validation-api</artifactId>
<classifier>sources</classifier>
</dependency>
```

