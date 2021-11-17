# replacement-issue-gradle_7_3

When running `./gradlew generateLock saveLock`, we see:

```
* What went wrong:
Execution failed for task ':server:generateLock'.
> Could not resolve all dependencies for configuration ':server:runtimeClasspath'.
   > Problems reading data from Binary store in /Users/rperezalcolea/.gradle/.tmp/gradle4057093498228885854.bin offset 1934 exists? true

```

Looking at the debugger, we can see that 

```
Caused by: java.lang.IllegalStateException: Unexpected parent dependency id 11. Seen ids: [17, 2, 7, 8, 14]
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.oldresult.TransientConfigurationResultsBuilder.deserialize(TransientConfigurationResultsBuilder.java:171)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.oldresult.TransientConfigurationResultsBuilder.lambda$load$5(TransientConfigurationResultsBuilder.java:117)
        at org.gradle.api.internal.artifacts.ivyservice.resolveengine.store.DefaultBinaryStore$SimpleBinaryData.read(DefaultBinaryStore.java:130)
        ... 129 more
```

which relates to

```
this = {DefaultResolvedConfigurationBuilder@11455} 
parent = {NodeState@11453} "netflix:b-standalone:1.0.0(runtime)"
child = {NodeState@11454} "netflix:b-standalone-original:1.0.0(runtime)"
artifactsId = 3
```

