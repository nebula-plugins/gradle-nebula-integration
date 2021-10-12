# Issue with replacement rule causing a binary store exception/ InvalidUserCodeException/ lock out of date

--------------

**Update: this is resolved with Gradle 7.3-rc-1**

--------------

Initially, I was seeing following error in the `runtimeClasspath` when the dependencies were locked using core Gradle locking:
```
> Resolved dependencies were missing from the lock state:
    1. Did not resolve 'com.google.protobuf:protobuf-java:3.11.1' which is part of the dependency lock state for project
```
Then I was running into the following error when the dependencies were unlocked:
```
A build scan cannot be produced as an error occurred gathering build data.
Please report this problem via https://gradle.com/help/plugin and include the following via copy/paste:

----------
Gradle version: 6.9
Plugin version: 3.6.3

org.gradle.api.InvalidUserCodeException: Variant 'runtime' doesn't belong to resolved component '<dependency>'. A variant with the same name exists but is not the same instance. Most likely you are using a variant from another component to get the dependencies of this component.
```

I believe the key dependency in this graph is `com.nebula.jax:j-e-p:1.7` which has the following dependency only on the runtime classpath, which is the classpath giving me the above trouble.

```
<dependency>
  <groupId>com.google.protobuf</groupId>
  <artifactId>protobuf-java</artifactId>
  <version>2.6.1</version>
  <scope>runtime</scope>
</dependency>
```

Here I have reproduced a binary store exception (as checked into this repo) and the `InvalidUserCodeException` (with a small modification to the dependencies) that then ends with a lock out of date issue once the dependencies are locked

-------

## Binary Store Exception

To reproduce the binary store exception, we can reset the dependencies to the following & ensure there are no locks in place:
```
dependencies {
    configA 'com.nebula.gen:p:4.0.0-SNAPSHOT'
    configB("nebula.z.c:s-c:4.7.188")
    configC("nebula.com.proto:p-j-n:3.11.1")
    configD("com.nebula.spring:c-m:2.3.54") {
      exclude group: 'nebula.z.a', module: 'm-d'
    }
    configE("nebula.z.a:m-c:1.256.0")
}
```
and then run
```
./gradlew dependencies
```
which shows
```
> Task :dependencies

------------------------------------------------------------
Root project 'replacement-locking-alignment-serialization-issue'
------------------------------------------------------------

annotationProcessor - Annotation processors and their dependencies for source set 'main'.
No dependencies

apiElements - API elements for main. (n)
No dependencies

archives - Configuration for archive artifacts. (n)
No dependencies

compileClasspath - Compile classpath for source set 'main'.
+--- com.nebula.gen:p:4.0.0-SNAPSHOT
|    +--- com.google.protobuf:protobuf-java:3.7.1 -> nebula.com.proto:p-j-n:3.11.1
|    \--- com.google.protobuf:protobuf-java-util:3.7.1
|         \--- com.google.protobuf:protobuf-java:3.7.1 -> nebula.com.proto:p-j-n:3.11.1
+--- nebula.z.c:s-c:4.7.188
+--- nebula.com.proto:p-j-n:3.11.1
+--- com.nebula.spring:c-m:2.3.54
|    \--- nebula.z.b:p-j-c:0.49.0
|         \--- nebula.z.a:m-i:1.256.0
|              +--- nebula.z.a:m-i-c:1.256.0
|              |    \--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
|              \--- nebula.z.c:p-i:4.7.102 -> 4.7.188
|                   +--- nebula.z.c:p-t-z:4.7.188
|                   +--- nebula.z.c:s-c:4.7.102 -> 4.7.188
|                   \--- com.nebula.jax:j-e-p:1.7
\--- nebula.z.a:m-c:1.256.0
     \--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1

compileOnly - Compile only dependencies for source set 'main'. (n)
No dependencies

configA
\--- com.nebula.gen:p:4.0.0-SNAPSHOT
     +--- com.google.protobuf:protobuf-java:3.7.1
     \--- com.google.protobuf:protobuf-java-util:3.7.1
          \--- com.google.protobuf:protobuf-java:3.7.1

configB
\--- nebula.z.c:s-c:4.7.188

configC
\--- nebula.com.proto:p-j-n:3.11.1

configD
\--- com.nebula.spring:c-m:2.3.54
     \--- nebula.z.b:p-j-c:0.49.0
          \--- nebula.z.a:m-i:1.256.0
               +--- nebula.z.a:m-i-c:1.256.0
               |    \--- nebula.com.proto:p-j-n:3.7.2
               \--- nebula.z.c:p-i:4.7.102
                    +--- nebula.z.c:s-c:4.7.102
                    \--- com.nebula.jax:j-e-p:1.7
                         \--- com.google.protobuf:protobuf-java:2.6.1 -> nebula.com.proto:p-j-n:3.7.2

configE
\--- nebula.z.a:m-c:1.256.0
     \--- nebula.com.proto:p-j-n:3.7.2

default - Configuration for default artifacts. (n)
No dependencies

implementation - Implementation only dependencies for source set 'main'. (n)
No dependencies

runtimeClasspath - Runtime classpath of source set 'main'.

> Task :dependencies FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':dependencies'.
> Could not resolve all dependencies for configuration ':runtimeClasspath'.
   > Problems reading data from Binary store in /Users/achipman/.gradle/.tmp/gradle288943414976444677.bin offset 9534 exists? true

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

BUILD FAILED in 21s
1 actionable task: 1 executed

A build scan cannot be produced as an error occurred gathering build data.
Please report this problem via https://gradle.com/help/plugin and include the following via copy/paste:

----------
Gradle version: 7.1
Plugin version: 3.6.3

org.gradle.api.artifacts.ResolveException: Could not resolve all dependencies for configuration ':runtimeClasspath'.
        at org.gradle.api.internal.artifacts.ivyservice.ErrorHandlingConfigurationResolver.wrapException(ErrorHandlingConfigurationResolver.java:105)
        at org.gradle.api.internal.artifacts.ivyservice.ErrorHandlingConfigurationResolver$ErrorHandlingResolutionResult.getRoot(ErrorHandlingConfigurationResolver.java:212)
        at org.gradle.api.internal.artifacts.configurations.ResolveConfigurationResolutionBuildOperationResult.getRootComponent(ResolveConfigurationResolutionBuildOperationResult.java:59)
        at com.gradle.scan.plugin.internal.b.f.a.h$a.b(SourceFile:113)
        at com.gradle.scan.plugin.internal.b.f.a.h$a.a(SourceFile:66)
        at com.gradle.scan.plugin.internal.b.f.a.h.a(SourceFile:47)
        at com.gradle.scan.plugin.internal.b.f.a.g.a(SourceFile:147)
        at com.gradle.scan.plugin.internal.b.f.c.a(SourceFile:137)
        at com.gradle.scan.plugin.internal.l.a.f.a(SourceFile:12)
        at com.gradle.scan.plugin.internal.l.a$c.finished(SourceFile:154)
        at com.gradle.scan.plugin.internal.l.a.a(SourceFile:65)
        at com.gradle.scan.plugin.internal.l.l.a(SourceFile:58)
        at com.gradle.scan.plugin.internal.l.c.a(SourceFile:98)
        at com.gradle.scan.plugin.internal.l.g.a(SourceFile:51)
        at com.gradle.scan.plugin.internal.q.a$a.a(SourceFile:31)
        at com.gradle.scan.plugin.internal.q.a$a.a(SourceFile:20)
        at com.gradle.scan.plugin.internal.q.a.c(SourceFile:67)
Caused by: java.lang.RuntimeException: Problems reading data from Binary store in /Users/achipman/.gradle/.tmp/gradle288943414976444677.bin offset 9534 exists? true
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.store.DefaultBinaryStore$SimpleBinaryData.read(DefaultBinaryStore.java:132)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.StreamingResolutionResultBuilder$RootFactory.lambda$create$0(StreamingResolutionResultBuilder.java:189)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.store.CachedStoreFactory$SimpleStore.load(CachedStoreFactory.java:101)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.StreamingResolutionResultBuilder$RootFactory.create(StreamingResolutionResultBuilder.java:187)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.StreamingResolutionResultBuilder$RootFactory.create(StreamingResolutionResultBuilder.java:159)
        at org.gradle.api.internal.artifacts.result.DefaultResolutionResult.getRoot(DefaultResolutionResult.java:48)
        at org.gradle.api.internal.artifacts.ivyservice.ErrorHandlingConfigurationResolver$ErrorHandlingResolutionResult.getRoot(ErrorHandlingConfigurationResolver.java:210)
        at org.gradle.api.internal.artifacts.configurations.ResolveConfigurationResolutionBuildOperationResult.getRootComponent(ResolveConfigurationResolutionBuildOperationResult.java:59)
        at com.gradle.scan.plugin.internal.b.f.a.h$a.b(SourceFile:113)
        at com.gradle.scan.plugin.internal.b.f.a.h$a.a(SourceFile:66)
        at com.gradle.scan.plugin.internal.b.f.a.h.a(SourceFile:47)
        at com.gradle.scan.plugin.internal.b.f.a.g.a(SourceFile:147)
        at com.gradle.scan.plugin.internal.b.f.c.a(SourceFile:137)
        at com.gradle.scan.plugin.internal.l.a.f.a(SourceFile:12)
        at com.gradle.scan.plugin.internal.l.a$c.finished(SourceFile:154)
        at com.gradle.scan.plugin.internal.l.a.a(SourceFile:65)
        at com.gradle.scan.plugin.internal.l.l.a(SourceFile:58)
        at com.gradle.scan.plugin.internal.l.c.a(SourceFile:98)
        at com.gradle.scan.plugin.internal.l.g.a(SourceFile:51)
        at com.gradle.scan.plugin.internal.q.a$a.a(SourceFile:31)
        at com.gradle.scan.plugin.internal.q.a$a.a(SourceFile:20)
        at com.gradle.scan.plugin.internal.q.a.c(SourceFile:67)
Caused by: java.lang.IllegalStateException: Corrupt serialized resolution result. Cannot find selected module (10) for runtimeClasspath -> nebula.com.proto:p-j-n:3.11.1
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.DefaultResolutionResultBuilder.visitOutgoingEdges(DefaultResolutionResultBuilder.java:83)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.StreamingResolutionResultBuilder$RootFactory.deserialize(StreamingResolutionResultBuilder.java:239)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.store.DefaultBinaryStore$SimpleBinaryData.read(DefaultBinaryStore.java:130)
        ... 21 more
----------
```

----------

## InvalidUserCodeException

Setting the dependencies to
```
dependencies {
    configA 'com.nebula.gen:p:4.0.0-SNAPSHOT'
    configB("nebula.z.c:s-c:4.7.188")
    configC("nebula.com.proto:p-j-n:3.11.1")
    configD("com.nebula.spring:c-m:2.3.54")
}
```
resolves the binary store exception but introduces an `InvalidUserCodeException`

To reproduce the InvalidUserCodeException, we can run the following after using the dependency block above:
```
./gradlew dependencies
```
which shows
```
> Task :dependencies

------------------------------------------------------------
Root project 'replacement-locking-alignment-serialization-issue'
------------------------------------------------------------

annotationProcessor - Annotation processors and their dependencies for source set 'main'.
No dependencies

apiElements - API elements for main. (n)
No dependencies

archives - Configuration for archive artifacts. (n)
No dependencies

compileClasspath - Compile classpath for source set 'main'.
+--- com.nebula.gen:p:4.0.0-SNAPSHOT
|    +--- com.google.protobuf:protobuf-java:3.7.1 -> nebula.com.proto:p-j-n:3.11.1
|    \--- com.google.protobuf:protobuf-java-util:3.7.1 -> nebula.com.proto:p-j-u-n:3.11.1
|         \--- nebula.com.proto:p-j-n:3.11.1
+--- nebula.z.c:s-c:4.7.188
+--- nebula.com.proto:p-j-n:3.11.1
\--- com.nebula.spring:c-m:2.3.54
     \--- nebula.z.b:p-j-c:0.49.0
          +--- nebula.z.a:m-d:1.256.0
          |    +--- nebula.com.proto:p-j-u-n:3.7.2 -> 3.11.1 (*)
          |    +--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          |    +--- nebula.z.a:m-c:1.256.0
          |    |    \--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          |    \--- nebula.z.a:m-i-c:1.256.0
          |         \--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          \--- nebula.z.a:m-i:1.256.0
               +--- nebula.z.a:m-i-c:1.256.0 (*)
               \--- nebula.z.c:p-i:4.7.102 -> 4.7.188
                    +--- nebula.z.c:p-t-z:4.7.188
                    +--- nebula.z.c:s-c:4.7.102 -> 4.7.188
                    \--- com.nebula.jax:j-e-p:1.7

compileOnly - Compile only dependencies for source set 'main'. (n)
No dependencies

configA
\--- com.nebula.gen:p:4.0.0-SNAPSHOT
     +--- com.google.protobuf:protobuf-java:3.7.1
     \--- com.google.protobuf:protobuf-java-util:3.7.1
          \--- com.google.protobuf:protobuf-java:3.7.1

configB
\--- nebula.z.c:s-c:4.7.188

configC
\--- nebula.com.proto:p-j-n:3.11.1

configD
\--- com.nebula.spring:c-m:2.3.54
     \--- nebula.z.b:p-j-c:0.49.0
          +--- nebula.z.a:m-d:1.256.0
          |    +--- nebula.com.proto:p-j-u-n:3.7.2
          |    |    \--- nebula.com.proto:p-j-n:3.7.2
          |    +--- nebula.com.proto:p-j-n:3.7.2
          |    +--- nebula.z.a:m-c:1.256.0
          |    |    \--- nebula.com.proto:p-j-n:3.7.2
          |    \--- nebula.z.a:m-i-c:1.256.0
          |         \--- nebula.com.proto:p-j-n:3.7.2
          \--- nebula.z.a:m-i:1.256.0
               +--- nebula.z.a:m-i-c:1.256.0 (*)
               \--- nebula.z.c:p-i:4.7.102
                    +--- nebula.z.c:s-c:4.7.102
                    \--- com.nebula.jax:j-e-p:1.7
                         \--- com.google.protobuf:protobuf-java:2.6.1 -> nebula.com.proto:p-j-n:3.7.2

configE
No dependencies

default - Configuration for default artifacts. (n)
No dependencies

implementation - Implementation only dependencies for source set 'main'. (n)
No dependencies

runtimeClasspath - Runtime classpath of source set 'main'.
+--- com.nebula.gen:p:4.0.0-SNAPSHOT
|    +--- com.google.protobuf:protobuf-java:3.7.1 -> 3.11.1
|    \--- com.google.protobuf:protobuf-java-util:3.7.1 -> nebula.com.proto:p-j-u-n:3.11.1
|         \--- nebula.com.proto:p-j-n:3.11.1
+--- nebula.z.c:s-c:4.7.188
+--- nebula.com.proto:p-j-n:3.11.1
\--- com.nebula.spring:c-m:2.3.54
     \--- nebula.z.b:p-j-c:0.49.0
          +--- nebula.z.a:m-d:1.256.0
          |    +--- nebula.com.proto:p-j-u-n:3.7.2 -> 3.11.1 (*)
          |    +--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          |    +--- nebula.z.a:m-c:1.256.0
          |    |    \--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          |    \--- nebula.z.a:m-i-c:1.256.0
          |         \--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          \--- nebula.z.a:m-i:1.256.0
               +--- nebula.z.a:m-i-c:1.256.0 (*)
               \--- nebula.z.c:p-i:4.7.102 -> 4.7.188
                    +--- nebula.z.c:p-t-z:4.7.188
                    +--- nebula.z.c:s-c:4.7.102 -> 4.7.188
                    \--- com.nebula.jax:j-e-p:1.7
                         \--- com.google.protobuf:protobuf-java:2.6.1 -> 3.11.1

runtimeElements - Elements of runtime for main. (n)
No dependencies

runtimeOnly - Runtime only dependencies for source set 'main'. (n)
No dependencies

testAnnotationProcessor - Annotation processors and their dependencies for source set 'test'.
No dependencies

testCompileClasspath - Compile classpath for source set 'test'.
+--- com.nebula.gen:p:4.0.0-SNAPSHOT
|    +--- com.google.protobuf:protobuf-java:3.7.1 -> nebula.com.proto:p-j-n:3.11.1
|    \--- com.google.protobuf:protobuf-java-util:3.7.1 -> nebula.com.proto:p-j-u-n:3.11.1
|         \--- nebula.com.proto:p-j-n:3.11.1
+--- nebula.z.c:s-c:4.7.188
+--- nebula.com.proto:p-j-n:3.11.1
\--- com.nebula.spring:c-m:2.3.54
     \--- nebula.z.b:p-j-c:0.49.0
          +--- nebula.z.a:m-d:1.256.0
          |    +--- nebula.com.proto:p-j-u-n:3.7.2 -> 3.11.1 (*)
          |    +--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          |    +--- nebula.z.a:m-c:1.256.0
          |    |    \--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          |    \--- nebula.z.a:m-i-c:1.256.0
          |         \--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          \--- nebula.z.a:m-i:1.256.0
               +--- nebula.z.a:m-i-c:1.256.0 (*)
               \--- nebula.z.c:p-i:4.7.102 -> 4.7.188
                    +--- nebula.z.c:p-t-z:4.7.188
                    +--- nebula.z.c:s-c:4.7.102 -> 4.7.188
                    \--- com.nebula.jax:j-e-p:1.7

testCompileOnly - Compile only dependencies for source set 'test'. (n)
No dependencies

testImplementation - Implementation only dependencies for source set 'test'. (n)
No dependencies

testRuntimeClasspath - Runtime classpath of source set 'test'.
+--- com.nebula.gen:p:4.0.0-SNAPSHOT
|    +--- com.google.protobuf:protobuf-java:3.7.1 -> 3.11.1
|    \--- com.google.protobuf:protobuf-java-util:3.7.1 -> nebula.com.proto:p-j-u-n:3.11.1
|         \--- nebula.com.proto:p-j-n:3.11.1
+--- nebula.z.c:s-c:4.7.188
+--- nebula.com.proto:p-j-n:3.11.1
\--- com.nebula.spring:c-m:2.3.54
     \--- nebula.z.b:p-j-c:0.49.0
          +--- nebula.z.a:m-d:1.256.0
          |    +--- nebula.com.proto:p-j-u-n:3.7.2 -> 3.11.1 (*)
          |    +--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          |    +--- nebula.z.a:m-c:1.256.0
          |    |    \--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          |    \--- nebula.z.a:m-i-c:1.256.0
          |         \--- nebula.com.proto:p-j-n:3.7.2 -> 3.11.1
          \--- nebula.z.a:m-i:1.256.0
               +--- nebula.z.a:m-i-c:1.256.0 (*)
               \--- nebula.z.c:p-i:4.7.102 -> 4.7.188
                    +--- nebula.z.c:p-t-z:4.7.188
                    +--- nebula.z.c:s-c:4.7.102 -> 4.7.188
                    \--- com.nebula.jax:j-e-p:1.7
                         \--- com.google.protobuf:protobuf-java:2.6.1 -> 3.11.1

testRuntimeOnly - Runtime only dependencies for source set 'test'. (n)
No dependencies

(*) - dependencies omitted (listed previously)

A web-based, searchable dependency report is available by adding the --scan option.

BUILD SUCCESSFUL in 1s
1 actionable task: 1 executed

A build scan cannot be produced as an error occurred gathering build data.
Please report this problem via https://gradle.com/help/plugin and include the following via copy/paste:

----------
Gradle version: 7.1
Plugin version: 3.6.3

org.gradle.api.InvalidUserCodeException: Variant 'runtime' doesn't belong to resolved component 'nebula.com.proto:p-j-n:3.11.1'. A variant with the same name exists but is not the same instance. Most likely you are using a variant from another component to get the dependencies of this component.
        at org.gradle.api.internal.artifacts.result.DefaultResolvedComponentResult.reportInvalidVariant(DefaultResolvedComponentResult.java:146)
        at org.gradle.api.internal.artifacts.result.DefaultResolvedComponentResult.getDependenciesForVariant(DefaultResolvedComponentResult.java:134)
        at com.gradle.scan.plugin.internal.b.f.a.n.a(SourceFile:35)
        at com.gradle.scan.plugin.internal.b.f.a.h$a.a(SourceFile:126)
        at com.gradle.scan.plugin.internal.b.f.a.h$a.a(SourceFile:150)
        at com.gradle.scan.plugin.internal.e.b.a(SourceFile:26)
        at com.gradle.scan.plugin.internal.b.f.a.h$a.a(SourceFile:126)
        at com.gradle.scan.plugin.internal.b.f.a.h$a.<init>(SourceFile:102)
        at com.gradle.scan.plugin.internal.b.f.a.h$a.b(SourceFile:116)
        at com.gradle.scan.plugin.internal.b.f.a.h$a.a(SourceFile:66)
        at com.gradle.scan.plugin.internal.b.f.a.h.a(SourceFile:47)
        at com.gradle.scan.plugin.internal.b.f.a.g.a(SourceFile:147)
        at com.gradle.scan.plugin.internal.b.f.c.a(SourceFile:137)
        at com.gradle.scan.plugin.internal.l.a.f.a(SourceFile:12)
        at com.gradle.scan.plugin.internal.l.a$c.finished(SourceFile:154)
        at com.gradle.scan.plugin.internal.l.a.a(SourceFile:65)
        at com.gradle.scan.plugin.internal.l.l.a(SourceFile:58)
        at com.gradle.scan.plugin.internal.l.c.a(SourceFile:98)
        at com.gradle.scan.plugin.internal.l.g.a(SourceFile:51)
        at com.gradle.scan.plugin.internal.q.a$a.a(SourceFile:31)
        at com.gradle.scan.plugin.internal.q.a$a.a(SourceFile:20)
        at com.gradle.scan.plugin.internal.q.a.c(SourceFile:67)
----------
```

--------

## Dependency lock state out of date

Finally, we can run

```
./gradlew dependencies --write-locks
 ```
which locks the dependencies and also shows
```
BUILD SUCCESSFUL in 906ms
1 actionable task: 1 executed

A build scan cannot be produced as an error occurred gathering build data.
Please report this problem via https://gradle.com/help/plugin and include the following via copy/paste:

----------
Gradle version: 7.1
Plugin version: 3.6.3

org.gradle.api.InvalidUserCodeException: Variant 'runtime' doesn't belong to resolved component 'nebula.com.proto:p-j-n:3.11.1'. A variant with the same name exists but is not the same instance. Most likely you are using a variant from another component to get the dependencies of this component.
```

When we use `dependencyInsight`, then we can see the lock of out date issue:

```
./gradlew dependencyInsight --dependency com.google.protobuf --configuration runtimeClasspath
 ```

```
> Task :dependencyInsight
com.google.protobuf:protobuf-java:3.11.1 FAILED
   Selection reasons:
      - By constraint : Dependency locking
   Failures:
      - Dependency lock state out of date:
          - Did not resolve 'com.google.protobuf:protobuf-java:3.11.1' which is part of the dependency lock state

com.google.protobuf:protobuf-java:3.11.1 FAILED
\--- runtimeClasspath

nebula.com.proto:p-j-n:3.11.1
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
   Selection reasons:
      - By constraint : dependency was locked to version '3.11.1'
      - By ancestor
      - By constraint : belongs to platform aligned-group:nebula.com.proto:3.11.1
      - Forced
      - Selected by rule : ✭ replacement

com.google.protobuf:protobuf-java:2.6.1 -> nebula.com.proto:p-j-n:3.11.1
\--- com.nebula.jax:j-e-p:1.7
     +--- runtimeClasspath (requested com.nebula.jax:j-e-p:{strictly 1.7})
     \--- nebula.z.c:p-i:4.7.188
          +--- runtimeClasspath (requested nebula.z.c:p-i:{strictly 4.7.188})
          \--- nebula.z.a:m-i:1.256.0 (requested nebula.z.c:p-i:4.7.102)
               +--- runtimeClasspath (requested nebula.z.a:m-i:{strictly 1.256.0})
               \--- nebula.z.b:p-j-c:0.49.0
                    +--- runtimeClasspath (requested nebula.z.b:p-j-c:{strictly 0.49.0})
                    \--- com.nebula.spring:c-m:2.3.54
                         \--- runtimeClasspath

com.google.protobuf:protobuf-java:3.7.1 -> nebula.com.proto:p-j-n:3.11.1
\--- com.nebula.gen:p:4.0.0-SNAPSHOT
     \--- runtimeClasspath

com.google.protobuf:protobuf-java:{strictly 3.11.1} -> nebula.com.proto:p-j-n:3.11.1
\--- runtimeClasspath

nebula.com.proto:p-j-u-n:3.11.1
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
   Selection reasons:
      - By constraint : dependency was locked to version '3.11.1'
      - By constraint : belongs to platform aligned-group:nebula.com.proto:3.11.1
      - Forced
      - By ancestor
      - Selected by rule : ✭ replacement

com.google.protobuf:protobuf-java-util:3.7.1 -> nebula.com.proto:p-j-u-n:3.11.1
\--- com.nebula.gen:p:4.0.0-SNAPSHOT
     \--- runtimeClasspath

(*) - dependencies omitted (listed previously)

A web-based, searchable dependency report is available by adding the --scan option.

BUILD SUCCESSFUL
```
where the most relevant part is the failure to replace a dependency
```
com.google.protobuf:protobuf-java:3.11.1 FAILED
   Selection reasons:
      - By constraint : Dependency locking
   Failures:
      - Dependency lock state out of date:
          - Did not resolve 'com.google.protobuf:protobuf-java:3.11.1' which is part of the dependency lock state
```
