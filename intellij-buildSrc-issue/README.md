Importing this on IntelliJ, results in:

```
Exception during working with external system: java.lang.AssertionError
	at org.jetbrains.plugins.gradle.service.project.CommonGradleProjectResolverExtension.createModule(CommonGradleProjectResolverExtension.java:122)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.kotlin.idea.configuration.KotlinMPPGradleProjectResolver.createModule(KotlinMPPGradleProjectResolver.kt:68)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension.createModule(AbstractProjectResolverExtension.java:80)
	at org.jetbrains.plugins.gradle.service.project.TracedProjectResolverExtension.createModule(TracedProjectResolverExtension.java:37)
	at org.jetbrains.plugins.gradle.service.project.GradleProjectResolver.convertData(GradleProjectResolver.java:380)
	at org.jetbrains.plugins.gradle.service.project.GradleProjectResolver.doResolveProjectInfo(GradleProjectResolver.java:308)
	at org.jetbrains.plugins.gradle.service.project.GradleProjectResolver$ProjectConnectionDataNodeFunction.fun(GradleProjectResolver.java:775)
	at org.jetbrains.plugins.gradle.service.project.GradleProjectResolver$ProjectConnectionDataNodeFunction.fun(GradleProjectResolver.java:758)
	at org.jetbrains.plugins.gradle.service.execution.GradleExecutionHelper.lambda$execute$0(GradleExecutionHelper.java:129)
	at org.jetbrains.plugins.gradle.service.execution.GradleExecutionHelper.maybeFixSystemProperties(GradleExecutionHelper.java:160)
	at org.jetbrains.plugins.gradle.service.execution.GradleExecutionHelper.lambda$execute$1(GradleExecutionHelper.java:129)
	at org.jetbrains.plugins.gradle.GradleConnectorService$Companion.withGradleConnection(GradleConnectorService.kt:181)
	at org.jetbrains.plugins.gradle.GradleConnectorService.withGradleConnection(GradleConnectorService.kt)
	at org.jetbrains.plugins.gradle.service.execution.GradleExecutionHelper.execute(GradleExecutionHelper.java:121)
	at org.jetbrains.plugins.gradle.service.project.GradleBuildSrcProjectsResolver.handleBuildSrcProject(GradleBuildSrcProjectsResolver.java:237)
	at org.jetbrains.plugins.gradle.service.project.GradleBuildSrcProjectsResolver.lambda$discoverAndAppendTo$0(GradleBuildSrcProjectsResolver.java:164)
	at java.base/java.util.stream.Streams$StreamBuilderImpl.forEachRemaining(Streams.java:411)
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:658)
	at org.jetbrains.plugins.gradle.service.project.GradleBuildSrcProjectsResolver.discoverAndAppendTo(GradleBuildSrcProjectsResolver.java:122)
	at org.jetbrains.plugins.gradle.service.project.GradleProjectResolver.resolveProjectInfo(GradleProjectResolver.java:155)
	at org.jetbrains.plugins.gradle.service.project.GradleProjectResolver.resolveProjectInfo(GradleProjectResolver.java:70)
	at com.intellij.openapi.externalSystem.service.remote.RemoteExternalSystemProjectResolverImpl.lambda$resolveProjectInfo$0(RemoteExternalSystemProjectResolverImpl.java:37)
	at com.intellij.openapi.externalSystem.service.remote.AbstractRemoteExternalSystemService.execute(AbstractRemoteExternalSystemService.java:43)
	at com.intellij.openapi.externalSystem.service.remote.RemoteExternalSystemProjectResolverImpl.resolveProjectInfo(RemoteExternalSystemProjectResolverImpl.java:36)
	at com.intellij.openapi.externalSystem.service.remote.wrapper.ExternalSystemProjectResolverWrapper.resolveProjectInfo(ExternalSystemProjectResolverWrapper.java:48)
	at com.intellij.openapi.externalSystem.service.internal.ExternalSystemResolveProjectTask.doExecute(ExternalSystemResolveProjectTask.java:115)
	at com.intellij.openapi.externalSystem.service.internal.AbstractExternalSystemTask.execute(AbstractExternalSystemTask.java:151)
	at com.intellij.openapi.externalSystem.service.internal.AbstractExternalSystemTask.execute(AbstractExternalSystemTask.java:135)
	at com.intellij.openapi.externalSystem.util.ExternalSystemUtil$2.executeImpl(ExternalSystemUtil.java:565)
	at com.intellij.openapi.externalSystem.util.ExternalSystemUtil$2.lambda$execute$0(ExternalSystemUtil.java:396)
	at com.intellij.openapi.project.DumbServiceHeavyActivities.suspendIndexingAndRun(DumbServiceHeavyActivities.java:21)
	at com.intellij.openapi.project.DumbServiceImpl.suspendIndexingAndRun(DumbServiceImpl.java:187)
	at com.intellij.openapi.externalSystem.util.ExternalSystemUtil$2.execute(ExternalSystemUtil.java:396)
	at com.intellij.openapi.externalSystem.util.ExternalSystemUtil$4.run(ExternalSystemUtil.java:670)
	at com.intellij.openapi.progress.impl.CoreProgressManager.startTask(CoreProgressManager.java:450)
	at com.intellij.openapi.progress.impl.ProgressManagerImpl.startTask(ProgressManagerImpl.java:117)
	at com.intellij.openapi.progress.impl.CoreProgressManager.lambda$runProcessWithProgressAsync$5(CoreProgressManager.java:510)
	at com.intellij.openapi.progress.impl.ProgressRunner.lambda$submit$3(ProgressRunner.java:243)
	at com.intellij.openapi.progress.impl.CoreProgressManager.lambda$runProcess$2(CoreProgressManager.java:183)
	at com.intellij.openapi.progress.impl.CoreProgressManager.registerIndicatorAndRun(CoreProgressManager.java:705)
	at com.intellij.openapi.progress.impl.CoreProgressManager.executeProcessUnderProgress(CoreProgressManager.java:647)
	at com.intellij.openapi.progress.impl.ProgressManagerImpl.executeProcessUnderProgress(ProgressManagerImpl.java:63)
	at com.intellij.openapi.progress.impl.CoreProgressManager.runProcess(CoreProgressManager.java:170)
	at com.intellij.openapi.progress.impl.ProgressRunner.lambda$submit$4(ProgressRunner.java:243)
	at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1700)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
	at java.base/java.util.concurrent.Executors$PrivilegedThreadFactory$1$1.run(Executors.java:668)
	at java.base/java.util.concurrent.Executors$PrivilegedThreadFactory$1$1.run(Executors.java:665)
	at java.base/java.security.AccessController.doPrivileged(Native Method)
	at java.base/java.util.concurrent.Executors$PrivilegedThreadFactory$1.run(Executors.java:665)
	at java.base/java.lang.Thread.run(Thread.java:829)

```

# IntelliJ info

```
IntelliJ IDEA 2021.2 (Ultimate Edition)
Build #IU-212.4746.92, built on July 27, 2021

Runtime version: 11.0.11+9-b1504.13 x86_64
VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
macOS 11.4
GC: G1 Young Generation, G1 Old Generation
Memory: 4096M
Cores: 16
Non-Bundled Plugins: org.intellij.scala (2021.2.17), org.asciidoctor.intellij.asciidoc (0.33.18)
Kotlin: 212-1.5.10-release-IJ4746.92
```
