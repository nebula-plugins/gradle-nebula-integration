# Core alignment with substitutions and a direct dependency

When a project brings in in the following dependencies that are part of an aligned group, so they should resolve to the same version, such as:

```groovy
com.google.inject:guice
com.google.inject.extensions:guice-multibindings
com.google.inject.extensions:guice-grapher
com.google.inject.extensions:guice-assistedinject
```

and there is a substitution rule for each of these that downgrades the version, that might read in `dependencyInsight` like:

```sh
substitution from 'com.google.inject:guice:[4.2.0,)' to 'com.google.inject:guice:4.1.0'
substitution from 'com.google.inject.extensions:guice-multibindings:[4.2.0,)' to 'com.google.inject.extensions:guice-multibindings:4.1.0'
substitution from 'com.google.inject.extensions:guice-grapher:[4.2.0,)' to 'com.google.inject.extensions:guice-grapher:4.1.0'
substitution from 'com.google.inject.extensions:guice-assistedinject:[4.2.0,)' to 'com.google.inject.extensions:guice-assistedinject:4.1.0'
```

and you have a direct dependency with an open dynamic version such as `com.google.inject:guice:4.+` in your project

then Nebula alignment will align all dependencies to the latest version available, as the substituted version goes into conflict resolution with that latest brought-in version. Here is the graph with only the guice-related dependencies shown:

```sh
+--- example:brings-guice:1.0.0
|    +--- com.google.inject:guice:4.1.0 -> 4.2.3
|    +--- com.google.inject.extensions:guice-multibindings:4.1.0 -> 4.2.3
|    |    \--- com.google.inject:guice:4.2.3 (*)
|    \--- com.google.inject.extensions:guice-grapher:4.1.0 -> 4.2.3
|         +--- com.google.inject.extensions:guice-assistedinject:4.2.3
|         |    \--- com.google.inject:guice:4.2.3 (*)
|         \--- com.google.inject:guice:4.2.3 (*)
\--- com.google.inject:guice:4.+ -> 4.2.3 (*)
```

Notice how all versions above use v4.2.3 which makes sense as they should all be aligned.

In contrast, Gradle core alignment will use the latest version available for the specified module and related modules that are part of its direct dependency graph, but will use the substituted version for others.

```
./gradlew dependencies --configuration compileClasspath
```

```sh
+--- example:brings-guice:1.0.0
|    +--- com.google.inject:guice:4.1.0 -> 4.2.3
|    |    +--- javax.inject:javax.inject:1
|    |    +--- aopalliance:aopalliance:1.0
|    |    \--- com.google.guava:guava:27.1-jre
|    |         +--- com.google.guava:failureaccess:1.0.1
|    |         +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |         +--- com.google.code.findbugs:jsr305:3.0.2
|    |         +--- org.checkerframework:checker-qual:2.5.2
|    |         +--- com.google.errorprone:error_prone_annotations:2.2.0
|    |         +--- com.google.j2objc:j2objc-annotations:1.1
|    |         \--- org.codehaus.mojo:animal-sniffer-annotations:1.17
|    +--- com.google.inject.extensions:guice-multibindings:4.1.0 -> 4.2.3
|    |    \--- com.google.inject:guice:4.2.3 (*)
|    \--- com.google.inject.extensions:guice-grapher:4.1.0 -> 4.2.3
|         +--- com.google.inject.extensions:guice-assistedinject:4.2.3 -> 4.1.0
|         |    \--- com.google.inject:guice:4.1.0 -> 4.2.3 (*)
|         \--- com.google.inject:guice:4.2.3 (*)
\--- com.google.inject:guice:4.+ -> 4.2.3 (*)
```

Notice how one dependency differs from the others: `guice-assistedinject:4.2.3 -> 4.1.0` out of:

```
com.google.inject:guice:4.1.0 -> 4.2.3
com.google.inject.extensions:guice-multibindings:4.1.0 -> 4.2.3
com.google.inject.extensions:guice-grapher:4.1.0 -> 4.2.3
com.google.inject.extensions:guice-assistedinject:4.2.3 -> 4.1.0
```

When looking at the [`dependencyInsight` results](dependencyInsight.out), we can see that many versions go into conflict resolution to choose v4.2.3 but only `guice-assistedinject` does not contain the reason `By conflict resolution : between versions`.
