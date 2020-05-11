##  Core alignment example with dropped dependencies

Here is an example dependency graph. The graph has been highly truncated for illustration.

This is using Gradle 6.3

### Update

**This example is fixed in Gradle 6.4-rc-1**

### Issue seen

Dependencies are dropped when there are exclusions for transitive dependencies, and the dependencies are also brought in from another source.

### Dependency setup

When each dependency is given its own configuration, it's easy to see what will be brought in:

```
./gradlew dependencies -DuseNebulaAlignment=true
```
and
```
./gradlew dependencies -DuseNebulaAlignment=false
```
both show the following:
```
configA
\--- apricot:apricot:2.2.2.RELEASE
     \--- com.berry.ribbon:ribbon:2.3.0
          \--- io.reactivex:rxcherry:0.4.9

configB
\--- berry:raspberry-impl:1.89.0

configC
\--- com.berry.foo:blueberry-governator:2.9.2
     \--- com.berry.foo:blueberry-core:2.9.2
          \--- io.reactivex:rxcherry-contexts:0.4.7
               \--- io.reactivex:rxcherry:0.4.7
                    \--- io.cherry:cherry-transport-native-epoll:4.0.25.Final
                         \--- io.cherry:cherry-common:4.0.25.Final

configD
\--- berry.bar.metrics:metrics-core:0.17.0
     \--- com.berry.ribbon:ribbon-loadbalancer:2.4.4

configE
\--- berry:raspberry-impl:1.87.4
```

### Runtime configuration

Once these configurations are merged together, however, there is a difference between the modes of alignment:

Runtime classpath for Nebula alignment
```
./gradlew dependencies --configuration runtimeClasspath -DuseNebulaAlignment=true

> Configure project :
Using Nebula resolution rules

> Task :dependencies

------------------------------------------------------------
Root project
------------------------------------------------------------

runtimeClasspath - Runtime classpath of source set 'main'.
+--- apricot:apricot:2.2.2.RELEASE
|    \--- com.berry.ribbon:ribbon:2.3.0 -> 2.4.4
|         \--- io.reactivex:rxcherry:0.4.9
|              \--- io.cherry:cherry-transport-native-epoll:4.0.27.Final
|                   \--- io.cherry:cherry-common:4.0.27.Final
+--- berry:raspberry-impl:1.89.0
+--- com.berry.foo:blueberry-governator:2.9.2
|    \--- com.berry.foo:blueberry-core:2.9.2
|         \--- io.reactivex:rxcherry-contexts:0.4.7
|              \--- io.reactivex:rxcherry:0.4.7 -> 0.4.9 (*)
+--- berry.bar.metrics:metrics-core:0.17.0
|    \--- com.berry.ribbon:ribbon-loadbalancer:2.4.4
\--- berry:raspberry-impl:1.87.4 -> 1.89.0
```

Runtime classpath for core Gradle alignment
```
./gradlew dependencies --configuration runtimeClasspath -DuseNebulaAlignment=false

> Configure project :
Using core Gradle alignment

> Task :dependencies

------------------------------------------------------------
Root project
------------------------------------------------------------

runtimeClasspath - Runtime classpath of source set 'main'.
+--- apricot:apricot:2.2.2.RELEASE
|    \--- com.berry.ribbon:ribbon:2.3.0 -> 2.4.4
|         \--- io.reactivex:rxcherry:0.4.9
+--- berry:raspberry-impl:1.89.0
+--- com.berry.foo:blueberry-governator:2.9.2
|    \--- com.berry.foo:blueberry-core:2.9.2
|         \--- io.reactivex:rxcherry-contexts:0.4.7
|              \--- io.reactivex:rxcherry:0.4.7 -> 0.4.9
+--- berry.bar.metrics:metrics-core:0.17.0
|    \--- com.berry.ribbon:ribbon-loadbalancer:2.4.4
\--- berry:raspberry-impl:1.87.4 -> 1.89.0
```

What is the difference? These two dependencies are not in the runtime classpath when using core Gradle alignment:

```
io.cherry:cherry-transport-native-epoll:4.0.27.Final
io.cherry:cherry-common:4.0.27.Final
```

### Let's verify: is alignment taking place?

We can see that alignment is taking place by looking at the dependency insight for `com.berry.ribbon`:

```
./gradlew dependencyInsight --dependency com.berry.ribbon --configuration runtimeClasspath -DuseNebulaAlignment=true

> Configure project :
Using Nebula resolution rules

> Task :dependencyInsight
com.berry.ribbon:ribbon:2.4.4
   Selection reasons:
      - Selected by rule : aligned to 2.4.4 by local-rules aligning group 'com.berry.ribbon'
                with reasons: nebula.resolution-rules uses: local-rules.json
```
and
```
./gradlew dependencyInsight --dependency com.berry.ribbon --configuration runtimeClasspath -DuseNebulaAlignment=false

> Configure project :
Using core Gradle alignment

> Task :dependencyInsight
com.berry.ribbon:ribbon:2.4.4
   Selection reasons:
      - By constraint : belongs to platform com.berry.ribbon:com.berry.ribbon:2.4.4
      - By conflict resolution : between versions 2.4.4 and 2.3.0
```

When we look at `io.cherry`, however, we see very different results:

```
./gradlew dependencyInsight --dependency io.cherry --configuration runtimeClasspath -DuseNebulaAlignment=true

> Configure project :
Using Nebula resolution rules

> Task :dependencyInsight
io.cherry:cherry-common:4.0.27.Final
\--- io.cherry:cherry-transport-native-epoll:4.0.27.Final
     \--- io.reactivex:rxcherry:0.4.9
          +--- com.berry.ribbon:ribbon:2.4.4
          |    \--- apricot:apricot:2.2.2.RELEASE (requested com.berry.ribbon:ribbon:2.3.0)
          |         \--- runtimeClasspath
          \--- io.reactivex:rxcherry-contexts:0.4.7 (requested io.reactivex:rxcherry:0.4.7)
               \--- com.berry.foo:blueberry-core:2.9.2
                    \--- com.berry.foo:blueberry-governator:2.9.2
                         \--- runtimeClasspath
...

io.cherry:cherry-transport-native-epoll:4.0.27.Final
\--- io.reactivex:rxcherry:0.4.9
     +--- com.berry.ribbon:ribbon:2.4.4
     |    \--- apricot:apricot:2.2.2.RELEASE (requested com.berry.ribbon:ribbon:2.3.0)
     |         \--- runtimeClasspath
     \--- io.reactivex:rxcherry-contexts:0.4.7 (requested io.reactivex:rxcherry:0.4.7)
          \--- com.berry.foo:blueberry-core:2.9.2
               \--- com.berry.foo:blueberry-governator:2.9.2
                    \--- runtimeClasspath
```
and
```
./gradlew dependencyInsight --dependency io.cherry --configuration runtimeClasspath -DuseNebulaAlignment=false

> Configure project :
Using core Gradle alignment

> Task :dependencyInsight
No dependencies matching given input were found in configuration ':runtimeClasspath'
```

### What may be causing this?

There is an exclusion in `apricot:apricot:2.2.2.RELEASE`. Here is a snippet of the pom file:

```
  <groupId>apricot</groupId>
  <artifactId>apricot</artifactId>
  <version>2.2.2.RELEASE</version>
  <dependencies>
    <dependency>
      <groupId>com.berry.ribbon</groupId>
      <artifactId>ribbon</artifactId>
      <version>2.3.0</version>
      <scope>compile</scope>
      <exclusions>
        <exclusion>
          <artifactId>cherry-codec-http</artifactId>
          <groupId>io.cherry</groupId>
        </exclusion>
        <exclusion>
          <artifactId>cherry-transport-native-epoll</artifactId>
          <groupId>io.cherry</groupId>
        </exclusion>
      </exclusions>
    </dependency>
  </dependencies>
```

Because this exclusion is in place for `apricot:apricot`, the dependencies `io.cherry:cherry-codec-http` and `io.cherry:cherry-transport-native-epoll` (at version `4.0.27.Final`) are not brought in by `apricot:apricot`. This can be seen clearly in the dependencies for `configA`, which is only `apricot:apricot`

In `configC`, we add `com.berry.foo:blueberry-governator` which also brings in `io.cherry:cherry-codec-http` and `io.cherry:cherry-transport-native-epoll` (at a different version: `4.0.25.Final`)

When the configurations are merged, then Nebula alignment uses `io.cherry:cherry-codec-http` and `io.cherry:cherry-transport-native-epoll` at version `4.0.27.Final` while core Gradle alignment appears to hold onto the exclusion.

### Expected result

1. When a dependency (`apricot:apricot`) has an exclusion for transitive dependencies (`io.cherry:cherry-codec-http` and `io.cherry:cherry-transport-native-epoll`), then this exclusion should be honored.

2. When a dependency (`com.berry.foo:blueberry-governator`) brings in the same transitive dependencies (`io.cherry:cherry-codec-http` and `io.cherry:cherry-transport-native-epoll`), then they should indeed enter the dependency graph.

3. The dependency graph can then show contributors from only the non-excluding contributors (`com.berry.foo:blueberry-governator`), similar to:

```
# Speculative results:
./gradlew dependencyInsight --dependency io.cherry --configuration runtimeClasspath

> Task :dependencyInsight
io.cherry:cherry-common:4.0.27.Final
\--- io.cherry:cherry-transport-native-epoll:4.0.27.Final
     \--- io.reactivex:rxcherry:0.4.9
          \--- io.reactivex:rxcherry-contexts:0.4.7 (requested io.reactivex:rxcherry:0.4.7)
               \--- com.berry.foo:blueberry-core:2.9.2
                    \--- com.berry.foo:blueberry-governator:2.9.2
                         \--- runtimeClasspath
...

io.cherry:cherry-transport-native-epoll:4.0.27.Final
\--- io.reactivex:rxcherry:0.4.9
     \--- io.reactivex:rxcherry-contexts:0.4.7 (requested io.reactivex:rxcherry:0.4.7)
          \--- com.berry.foo:blueberry-core:2.9.2
               \--- com.berry.foo:blueberry-governator:2.9.2
                    \--- runtimeClasspath
```

and folks can see where `io.reactivex:rxcherry` came from via

```
./gradlew dependencyInsight --dependency rxcherry --configuration runtimeClasspath

io.reactivex:rxcherry:0.4.9
\--- com.berry.ribbon:ribbon:2.4.4
     \--- apricot:apricot:2.2.2.RELEASE (requested com.berry.ribbon:ribbon:2.3.0)
          \--- runtimeClasspath

io.reactivex:rxcherry:0.4.7 -> 0.4.9
\--- io.reactivex:rxcherry-contexts:0.4.7
     \--- com.berry.foo:blueberry-core:2.9.2
          \--- com.berry.foo:blueberry-governator:2.9.2
               \--- runtimeClasspath
```
