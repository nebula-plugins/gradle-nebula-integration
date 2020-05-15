# my-submodule

`./gradlew :my-submodule:dependencies --configuration integTestRuntimeClasspath -s`

results in

```
* What went wrong:
Execution failed for task ':my-submodule:dependencies'.
> Failed to notify dependency resolution listener.
   > Could not resolve all dependencies for configuration ':my-submodule:integTestRuntimeClasspathCopy'.
      > Problems reading data from Binary store in /private/var/folders/hb/c0ghc68d7vn26h4ny84kzv0r0000gn/T/gradle4483235546235009376.bin offset 12425 exists? true
   > Could not resolve all dependencies for configuration ':my-submodule:integTestRuntimeClasspathCopy'.
      > Problems reading data from Binary store in /private/var/folders/hb/c0ghc68d7vn26h4ny84kzv0r0000gn/T/gradle4483235546235009376.bin offset 12425 exists? true

Caused by: java.lang.IllegalStateException: Corrupt serialized resolution result. Cannot find selected module (149) for constraint platform-runtime -> org.hamcrest:hamcrest-core:2.2
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.DefaultResolutionResultBuilder.visitOutgoingEdges(DefaultResolutionResultBuilder.java:82)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.StreamingResolutionResultBuilder$RootFactory.deserialize(StreamingResolutionResultBuilder.java:237)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.store.DefaultBinaryStore$SimpleBinaryData.read(DefaultBinaryStore.java:130)
        ... 200 more

```


# my-submodule-2

`./gradlew :my-submodule-2:dI --dependency oauth2-oidc-sdk --configuration testCompileClasspath -s`

results in

```
* What went wrong:
Execution failed for task ':my-submodule-2:dependencyInsight'.
> Failed to notify dependency resolution listener.
   > Could not resolve all dependencies for configuration ':my-submodule-2:testCompileClasspathCopy'.
      > Problems reading data from Binary store in /private/var/folders/hb/c0ghc68d7vn26h4ny84kzv0r0000gn/T/gradle6195706002220021701.bin offset 12425 exists? true
   > Could not resolve all dependencies for configuration ':my-submodule-2:testCompileClasspathCopy'.
      > Problems reading data from Binary store in /private/var/folders/hb/c0ghc68d7vn26h4ny84kzv0r0000gn/T/gradle6195706002220021701.bin offset 12425 exists? true

Caused by: java.lang.IllegalStateException: Corrupt serialized resolution result. Cannot find selected module (152) for compile -> com.nimbusds:oauth2-oidc-sdk:7.1.1
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.DefaultResolutionResultBuilder.visitOutgoingEdges(DefaultResolutionResultBuilder.java:82)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.StreamingResolutionResultBuilder$RootFactory.deserialize(StreamingResolutionResultBuilder.java:237)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.store.DefaultBinaryStore$SimpleBinaryData.read(DefaultBinaryStore.java:130)
        ... 197 more

```


# my-submodule-3

`./gradlew :my-submodule-3:dI --dependency spring-expression --configuration testCompileClasspath -s`

results in

```
Execution failed for task ':my-submodule-3:dependencyInsight'.
> Failed to notify dependency resolution listener.
   > Could not resolve all dependencies for configuration ':my-submodule-3:testCompileClasspathCopy'.
      > Problems reading data from Binary store in /private/var/folders/hb/c0ghc68d7vn26h4ny84kzv0r0000gn/T/gradle1707861726770070894.bin offset 12425 exists? true
   > Could not resolve all dependencies for configuration ':my-submodule-3:testCompileClasspathCopy'.
      > Problems reading data from Binary store in /private/var/folders/hb/c0ghc68d7vn26h4ny84kzv0r0000gn/T/gradle1707861726770070894.bin offset 12425 exists? true

Caused by: java.lang.IllegalStateException: Corrupt serialized resolution result. Cannot find selected module (625) for constraint platform-compile -> org.springframework:spring-expression:5.2.5.RELEASE
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.DefaultResolutionResultBuilder.visitOutgoingEdges(DefaultResolutionResultBuilder.java:82)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.StreamingResolutionResultBuilder$RootFactory.deserialize(StreamingResolutionResultBuilder.java:237)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.store.DefaultBinaryStore$SimpleBinaryData.read(DefaultBinaryStore.java:130)
        ... 196 more


```
