# Test gradle shared dependency cache

Run with

```
./gradlew build -d --refresh-dependencies -g /tmp/gradlecache/mynewcache4
```

You will see that the artifact is getting pulled anyway 

```
2020-10-13T12:52:08.232-0700 [DEBUG] [org.gradle.internal.operations.DefaultBuildOperationExecutor] Build operation 'Resolve files of :compileClasspath' started
2020-10-13T12:52:08.236-0700 [DEBUG] [org.gradle.internal.operations.DefaultBuildOperationExecutor] Build operation 'Resolve guava-27.1-jre.jar (com.google.guava:guava:27.1-jre)' started
2020-10-13T12:52:08.236-0700 [DEBUG] [org.gradle.internal.operations.DefaultBuildOperationExecutor] Build operation 'Resolve failureaccess-1.0.1.jar (com.google.guava:failureaccess:1.0.1)' started
2020-10-13T12:52:08.236-0700 [DEBUG] [org.gradle.api.internal.artifacts.repositories.resolver.DefaultExternalResourceArtifactResolver] Loading https://repo.maven.apache.org/maven2/com/google/guava/guava/27.1-jre/guava-27.1-jre.jar
2020-10-13T12:52:08.237-0700 [DEBUG] [org.gradle.internal.operations.DefaultBuildOperationExecutor] Build operation 'Resolve animal-sniffer-annotations-1.17.jar (org.codehaus.mojo:animal-sniffer-annotations:1.17)' started
2020-10-13T12:52:08.237-0700 [DEBUG] [org.gradle.api.internal.artifacts.repositories.resolver.DefaultExternalResourceArtifactResolver] Loading https://repo.maven.apache.org/maven2/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar
2020-10-13T12:52:08.237-0700 [DEBUG] [org.gradle.internal.resource.transfer.DefaultCacheAwareExternalResourceAccessor] Constructing external resource: https://repo.maven.apache.org/maven2/com/google/guava/guava/27.1-jre/guava-27.1-jre.jar
2020-10-13T12:52:08.237-0700 [DEBUG] [org.gradle.internal.resource.transfer.DefaultCacheAwareExternalResourceAccessor] Constructing external resource: https://repo.maven.apache.org/maven2/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar
```