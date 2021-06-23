# Issue with replacement rule

Initially, I was seeing following error in the `runtimeClasspath` when the dependencies were locked:
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

While I have not reproduced that exact issue here, I did run into a binary store exception, which I believe is related, especially since it talks about the same troublesome module `nebula.com.proto:p-j-n:3.11.1` as I was seeing on an internal project with the invalid variant.

I believe the key dependency in this graph is `com.nebula.jax:j-e-p:1.7` which has the following dependency only on the runtime classpath, which is the classpath giving me the above trouble.

```
<dependency>
  <groupId>com.google.protobuf</groupId>
  <artifactId>protobuf-java</artifactId>
  <version>2.6.1</version>
  <scope>runtime</scope>
</dependency>
```

-------

To reproduce the binary store exception, we can run the following:
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
