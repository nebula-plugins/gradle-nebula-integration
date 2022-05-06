# Alignment and non-cached missing resources

When we have an aligned platform with missing artifacts at that version (such as Jackson's micropatch versions), then we see a lot of trips for these missing artifacts.

The missing artifact metadata does not get cached as missing, so we see the following on each Gradle invocation (with parallel enabled):
```
./gradlew compileJava -i --rerun-tasks
```

```
> Task :sub3:compileJava
Resource missing. [HTTP GET: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-core/2.12.6.1/jackson-core-2.12.6.1.pom]
Resource missing. [HTTP GET: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.12.6.1/jackson-annotations-2.12.6.1.pom]
Resource missing. [HTTP GET: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/jackson-bom/2.12.6.1/jackson-bom-2.12.6.1.pom]

> Task :sub1:compileJava
Resource missing. [HTTP GET: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-core/2.12.6.1/jackson-core-2.12.6.1.pom]

> Task :sub2:compileJava
Resource missing. [HTTP GET: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-core/2.12.6.1/jackson-core-2.12.6.1.pom]
```

In debug logging, we see the following kinds of messages for found metadata/ artifacts:
```
[org.gradle.api.internal.artifacts.ivyservice.ivyresolve.CachingModuleComponentRepository] Using cached module metadata for module 'com.fasterxml.jackson.core:jackson-databind:2.12.6.1' in 'MavenRepo'
```

It would be helpful if:

1. the missing artifact metadata gets cached as missing within the same build (we see repeats of `Resource missing` when `org.gradle.parallel=true`)
2. the missing artifact metadata gets cached as missing across builds similar to storing found artifacts
