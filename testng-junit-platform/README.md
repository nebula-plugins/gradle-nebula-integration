# testng junit platform integration

`ParallelParameterizationTest` results in:

```
null
java.lang.NullPointerException
        at org.gradle.api.internal.tasks.testing.results.StateTrackingTestResultProcessor.started(StateTrackingTestResultProcessor.java:45)
        at org.gradle.api.internal.tasks.testing.results.AttachParentTestResultProcessor.started(AttachParentTestResultProcessor.java:38)
        at jdk.internal.reflect.GeneratedMethodAccessor20.invoke(Unknown Source)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:566)
        at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:36)
        at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:24)
        at org.gradle.internal.dispatch.FailureHandlingDispatch.dispatch(FailureHandlingDispatch.java:30)
        at org.gradle.internal.dispatch.AsyncDispatch.dispatchMessages(AsyncDispatch.java:87)
        at org.gradle.internal.dispatch.AsyncDispatch.access$000(AsyncDispatch.java:36)
        at org.gradle.internal.dispatch.AsyncDispatch$1.run(AsyncDispatch.java:71)
        at org.gradle.internal.concurrent.InterruptibleRunnable.run(InterruptibleRunnable.java:42)
        at org.gradle.internal.operations.CurrentBuildOperationPreservingRunnable.run(CurrentBuildOperationPreservingRunnable.java:42)
        at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
        at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:48)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
        at org.gradle.internal.concurrent.ThreadFactoryImpl$ManagedThreadRunnable.run(ThreadFactoryImpl.java:56)
        at java.base/java.lang.Thread.run(Thread.java:829)

Gradle Test Run :test > Gradle Test Executor 2 > UnknownClass.TestNG > UnknownClass.executionError FAILED
    org.junit.platform.commons.JUnitException at EngineExecutionOrchestrator.java:114
        Caused by: java.lang.NullPointerException at ExecutionListener.java:169
```

[Build Scan](https://scans.gradle.com/s/ooupoqm2p2ggu)  
