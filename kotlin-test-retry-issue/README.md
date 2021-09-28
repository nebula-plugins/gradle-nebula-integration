# kotlin-test-retry-issue

Executing `./gradlew build` results in

```
> Configure project :
Kotlin Multiplatform Projects are an Alpha feature. See: https://kotlinlang.org/docs/reference/evolution/components-stability.html. To hide this message, add 'kotlin.mpp.stability.nowarn=true' to the Gradle properties.


> Task :jvmTest FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':jvmTest'.
> Unexpected test executer: org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest$Executor@1073de17

* Try:
Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Exception is:
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':jvmTest'.
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.lambda$executeIfValid$1(ExecuteActionsTaskExecuter.java:187)
        at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:268)
```

Sample build scan: [https://scans.gradle.com/s/jmzkyrqiejhd6](https://scans.gradle.com/s/jmzkyrqiejhd6)
