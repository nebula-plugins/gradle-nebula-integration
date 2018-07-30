Excludes with IMPROVED_POM_SUPPORT
==================================

A dependency that is excluded ends up brought in anyway when a bom recommends a version of it.

Adding the same excludes to the bom results in the excludes being applied.


Without IMPROVED_POM_SUPPORT
----------------------------

```
$ ./gradlew -DimprovedPom=false -q dependencies --configuration compileClasspath

------------------------------------------------------------
Root project
------------------------------------------------------------

compileClasspath - Compile classpath for source set 'main'.
+--- io.grpc:grpc-protobuf:1.10.0
|    +--- io.grpc:grpc-core:1.10.0
|    |    +--- io.grpc:grpc-context:1.10.0
|    |    +--- com.google.code.gson:gson:2.7
|    |    +--- com.google.guava:guava:19.0
|    |    +--- com.google.errorprone:error_prone_annotations:2.1.2
|    |    +--- com.google.code.findbugs:jsr305:3.0.0
|    |    +--- io.opencensus:opencensus-api:0.11.0
|    |    |    \--- com.google.guava:guava:19.0
|    |    \--- io.opencensus:opencensus-contrib-grpc-metrics:0.11.0
|    |         \--- io.opencensus:opencensus-api:0.11.0 (*)
|    +--- com.google.protobuf:protobuf-java:3.5.1
|    +--- com.google.guava:guava:19.0
|    +--- com.google.protobuf:protobuf-java-util:3.5.1
|    |    +--- com.google.protobuf:protobuf-java:3.5.1
|    |    +--- com.google.guava:guava:19.0
|    |    \--- com.google.code.gson:gson:2.7
|    +--- com.google.api.grpc:proto-google-common-protos:1.0.0
|    \--- io.grpc:grpc-protobuf-lite:1.10.0
|         +--- io.grpc:grpc-core:1.10.0 (*)
|         \--- com.google.guava:guava:19.0
\--- nebulatest:nebulabom:1.0.0

(*) - dependencies omitted (listed previously)

A web-based, searchable dependency report is available by adding the --scan option.

```

With IMPROVED_POM_SUPPORT
-------------------------

```
$ ./gradlew -DimprovedPom=true dependencies --configuration compileClasspath

------------------------------------------------------------
Root project
------------------------------------------------------------

compileClasspath - Compile classpath for source set 'main'.
+--- io.grpc:grpc-protobuf:1.10.0
|    +--- io.grpc:grpc-core:1.10.0
|    |    +--- io.grpc:grpc-context:1.10.0
|    |    +--- com.google.code.gson:gson:2.7
|    |    +--- com.google.guava:guava:19.0
|    |    |    +--- com.google.code.findbugs:jsr305:1.3.9 -> 3.0.0
|    |    |    \--- com.google.errorprone:error_prone_annotations:2.0.2 -> 2.1.2
|    |    +--- com.google.errorprone:error_prone_annotations:2.1.2
|    |    +--- com.google.code.findbugs:jsr305:3.0.0
|    |    +--- io.opencensus:opencensus-api:0.11.0
|    |    |    \--- com.google.guava:guava:19.0 (*)
|    |    \--- io.opencensus:opencensus-contrib-grpc-metrics:0.11.0
|    |         \--- io.opencensus:opencensus-api:0.11.0 (*)
|    +--- com.google.protobuf:protobuf-java:3.5.1
|    +--- com.google.guava:guava:19.0 (*)
|    +--- com.google.protobuf:protobuf-java-util:3.5.1
|    |    +--- com.google.protobuf:protobuf-java:3.5.1
|    |    +--- com.google.guava:guava:19.0 (*)
|    |    \--- com.google.code.gson:gson:2.7
|    +--- com.google.api.grpc:proto-google-common-protos:1.0.0
|    \--- io.grpc:grpc-protobuf-lite:1.10.0
|         +--- io.grpc:grpc-core:1.10.0 (*)
|         +--- com.google.protobuf:protobuf-lite:3.0.1                  <--- new, unexpected since original declaration excluded
|         \--- com.google.guava:guava:19.0 (*)
\--- nebulatest:nebulabom:1.0.0
     \--- io.grpc:grpc-protobuf-lite:1.10.0 (*)

(*) - dependencies omitted (listed previously)

A web-based, searchable dependency report is available by adding the --scan option.

```
