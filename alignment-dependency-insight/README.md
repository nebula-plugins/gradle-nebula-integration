# dependency insight when alignment is involved

Given an alignment rule for a group such as `org.springframework.boot`:

```
{
  "align": [
    {
      "name": "spring-boot-align",
      "group": "org\\.springframework\\.boot",
      "includes": [],
      "excludes": [],
      "reason": "Align Spring Boot libraries",
      "author": "Roberto Perez <roberto@perezalcolea.info>",
      "date": "2020-05-13"
    }
  ],
  "deny": [],
  "exclude": [],
  "reject": [],
  "replace" : [],
  "substitute": []
}
```

When a transitive dependency brings a new version, for example:

```
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>test.nebula</groupId>
  <artifactId>c</artifactId>
  <version>1.0.0</version>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <version>2.2.7.RELEASE</version>
      <type>jar</type>
      <scope>compile</scope>
    </dependency>
  </dependencies>
</project>
```

In this case, the version is higher than the one requested in the build.gradle

As expected, all versions for the group will be bumped and aligned to `2.2.7.RELEASE`

However, when executing 

```
./gradlew dI --dependency spring-boot-starter-data-rest                                                       ✔  10161  at 13:03:29   1.8.0_242 
```

Results in

```
> Task :dependencyInsight
org.springframework.boot:spring-boot-starter-data-rest:2.2.7.RELEASE
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
      - Selected by rule : aligned to 2.2.7.RELEASE by align-spring aligning group 'org\.springframework\.boot'
                with reasons: nebula.resolution-rules uses: align-spring.json

org.springframework.boot:spring-boot-starter-data-rest:2.1.14.RELEASE -> 2.2.7.RELEASE
\--- compileClasspath

```

It would be ideal if Gradle could provide information on “largest contributor” or “final contributor” for the group to be aligned to that version

Ideally, getting this piece somehow:

```
\--- test.nebula:a:1.0.0
     \--- test.nebula:b:1.0.0
          \--- test.nebula:c:1.0.0
               \--- org.springframework.boot:spring-boot-starter-web:2.2.7.RELEASE (*)
```