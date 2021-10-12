# Core alignment issue with replacements and transitive dependencies

--------------

**Update: this is resolved with Gradle 7.3-rc-1**

Now we see:

```
./gradlew dependencyInsight --dependency guava --configuration runtimeClasspath
```
showing
```
> Task :dependencyInsight
com.google.guava:guava:19.0
   variant "runtime" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-runtime
      org.gradle.libraryelements     = jar
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.environment     = standard-jvm
         org.gradle.jvm.version         = 8
   ]

com.google.guava:guava:19.0
\--- runtimeClasspath
```

--------------

We are seeing some odd items with a replacement in this example project, using Gradle 6.9.

In this one, a single dependency gets replaced, but the transitive dependencies remain. This leads to unexpected output like:

```
./gradlew dependencyInsight --dependency guava --configuration runtimeClasspath
```

```
com.google.guava:guava:29.0-android
   variant "runtime" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-runtime
      org.gradle.libraryelements     = jar
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - By conflict resolution : between versions 29.0-android and 19.0

com.google.guava:guava:19.0 -> 29.0-android
\--- runtimeClasspath
```

where it’s 1) unclear where guava `29.0-android` comes from and 2) it came from a replaced dependency, so the transitive guava dependency should also have been removed from the graph

In this case, this tree of dependencies:
```
\--- io.grpc:grpc-services:1.34.0
     +--- com.google.protobuf:protobuf-java-util:3.12.0
     |    +--- com.google.protobuf:protobuf-java:3.12.0
     |    \--- com.google.code.gson:gson:2.8.6
     +--- com.google.guava:guava:29.0-android
```
should be going away as `io.grpc:grpc-services` gets replaced by an internal version
```
io.grpc:grpc-services:1.34.0 -> berry.io.grpc:g-s-n:1.33.1
```
and so the `com.google.guava:guava:29.0-android` should no longer be brought in.

------

Full dependency listing with Gradle 6.9:
```
> Task :dependencies

------------------------------------------------------------
Root project 'with-replacement-and-transitives'
------------------------------------------------------------

annotationProcessor - Annotation processors and their dependencies for source set 'main'.
No dependencies

apiElements - API elements for main. (n)
No dependencies

archives - Configuration for archive artifacts. (n)
No dependencies

compileClasspath - Compile classpath for source set 'main'.
+--- com.google.guava:guava:19.0
+--- berry.grpc:n-g-m-n:1.33.3
|    \--- berry.grpc:n-g-s:1.33.3
|         \--- berry.grpc:n-g-c:1.33.3
|              \--- berry.io.grpc:g-s-n:1.33.1
\--- io.grpc:grpc-services:1.34.0 -> berry.io.grpc:g-s-n:1.33.1

compileOnly - Compile only dependencies for source set 'main'. (n)
No dependencies

configA
\--- com.google.guava:guava:19.0

configB
\--- berry.grpc:n-g-m-n:1.33.3
     \--- berry.grpc:n-g-s:1.33.3
          \--- berry.grpc:n-g-c:1.33.3
               \--- berry.io.grpc:g-s-n:1.33.1

configC
\--- io.grpc:grpc-services:1.34.0
     +--- com.google.protobuf:protobuf-java-util:3.12.0
     |    +--- com.google.protobuf:protobuf-java:3.12.0
     |    \--- com.google.code.gson:gson:2.8.6
     +--- com.google.guava:guava:29.0-android
     |    +--- com.google.guava:failureaccess:1.0.1
     |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
     |    +--- org.checkerframework:checker-compat-qual:2.5.5
     |    \--- com.google.j2objc:j2objc-annotations:1.3
     +--- com.google.errorprone:error_prone_annotations:2.3.4
     +--- org.codehaus.mojo:animal-sniffer-annotations:1.18
     \--- com.google.code.findbugs:jsr305:3.0.2

default - Configuration for default artifacts. (n)
No dependencies

implementation - Implementation only dependencies for source set 'main'. (n)
No dependencies

runtimeClasspath - Runtime classpath of source set 'main'.
+--- com.google.guava:guava:19.0 -> 29.0-android
|    +--- com.google.guava:failureaccess:1.0.1
|    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    +--- com.google.code.findbugs:jsr305:3.0.2
|    +--- org.checkerframework:checker-compat-qual:2.5.5
|    +--- com.google.errorprone:error_prone_annotations:2.3.4
|    \--- com.google.j2objc:j2objc-annotations:1.3
+--- berry.grpc:n-g-m-n:1.33.3
|    \--- berry.grpc:n-g-s:1.33.3
|         \--- berry.grpc:n-g-c:1.33.3
|              \--- berry.io.grpc:g-s-n:1.33.1
\--- io.grpc:grpc-services:1.34.0 -> berry.io.grpc:g-s-n:1.33.1

runtimeElements - Elements of runtime for main. (n)
No dependencies

runtimeOnly - Runtime only dependencies for source set 'main'. (n)
No dependencies

testAnnotationProcessor - Annotation processors and their dependencies for source set 'test'.
No dependencies

testCompileClasspath - Compile classpath for source set 'test'.
+--- com.google.guava:guava:19.0
+--- berry.grpc:n-g-m-n:1.33.3
|    \--- berry.grpc:n-g-s:1.33.3
|         \--- berry.grpc:n-g-c:1.33.3
|              \--- berry.io.grpc:g-s-n:1.33.1
\--- io.grpc:grpc-services:1.34.0 -> berry.io.grpc:g-s-n:1.33.1

testCompileOnly - Compile only dependencies for source set 'test'. (n)
No dependencies

testImplementation - Implementation only dependencies for source set 'test'. (n)
No dependencies

testRuntimeClasspath - Runtime classpath of source set 'test'.
+--- com.google.guava:guava:19.0 -> 29.0-android
|    +--- com.google.guava:failureaccess:1.0.1
|    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    +--- com.google.code.findbugs:jsr305:3.0.2
|    +--- org.checkerframework:checker-compat-qual:2.5.5
|    +--- com.google.errorprone:error_prone_annotations:2.3.4
|    \--- com.google.j2objc:j2objc-annotations:1.3
+--- berry.grpc:n-g-m-n:1.33.3
|    \--- berry.grpc:n-g-s:1.33.3
|         \--- berry.grpc:n-g-c:1.33.3
|              \--- berry.io.grpc:g-s-n:1.33.1
\--- io.grpc:grpc-services:1.34.0 -> berry.io.grpc:g-s-n:1.33.1

testRuntimeOnly - Runtime only dependencies for source set 'test'. (n)
No dependencies

(*) - dependencies omitted (listed previously)

A web-based, searchable dependency report is available by adding the --scan option.

BUILD SUCCESSFUL in 655ms
```


--------

## Other Gradle Versions

INTERESTINGLY, this was not an issue in Gradle 6.5.1, Gradle 6.6, & Gradle 6.6.1 which show:
```
testRuntimeClasspath - Runtime classpath of source set 'test'.
+--- com.google.guava:guava:19.0
+--- berry.grpc:n-g-m-n:1.33.3
|    \--- berry.grpc:n-g-s:1.33.3
|         \--- berry.grpc:n-g-c:1.33.3
|              \--- berry.io.grpc:g-s-n:1.33.1
\--- io.grpc:grpc-services:1.34.0 -> berry.io.grpc:g-s-n:1.33.1
```

The following tested Gradle versions show this issue 
- Gradle 6.7
- Gradle 6.7.1
- Gradle 6.8.3
- Gradle 6.9
- Gradle 7.1.1

So we can see that something changed from Gradle 6.6.1 to 6.7
