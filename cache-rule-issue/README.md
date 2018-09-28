# broken-release-candidate-attribute-value

When we detect a version that it's a Release Candidate, we set the `status` to `candidate`.

This is broken starting with `5.0-20180923235913+0000` release.

Running the `MyPluginSpec` test will generate the following exception:

```
org.gradle.api.GradleException: Build aborted because of an internal error.

	at nebula.test.functional.internal.DefaultExecutionResult.rethrowFailure(DefaultExecutionResult.groovy:97)
	at nebula.test.IntegrationSpec.runTasksSuccessfully(IntegrationSpec.groovy:149)
	at gradle5.rc.issue.MyPluginSpec.specific transitive candidates or snapshots resolved(MyPluginSpec.groovy:76)
Caused by: org.gradle.internal.exceptions.LocationAwareException: A problem occurred configuring root project 'specific-transitive-candidates-or-snapshots-resolved'.
	at org.gradle.initialization.DefaultExceptionAnalyser.transform(DefaultExceptionAnalyser.java:74)
	at org.gradle.initialization.MultipleBuildFailuresExceptionAnalyser.transform(MultipleBuildFailuresExceptionAnalyser.java:49)
	at org.gradle.initialization.StackTraceSanitizingExceptionAnalyser.transform(StackTraceSanitizingExceptionAnalyser.java:30)
	at org.gradle.initialization.DefaultGradleLauncher.doBuildStages(DefaultGradleLauncher.java:164)
	at org.gradle.initialization.DefaultGradleLauncher.executeTasks(DefaultGradleLauncher.java:133)
	at org.gradle.internal.invocation.GradleBuildController$1.call(GradleBuildController.java:77)
	at org.gradle.internal.invocation.GradleBuildController$1.call(GradleBuildController.java:74)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:154)
	at org.gradle.internal.work.StopShieldingWorkerLeaseService.withLocks(StopShieldingWorkerLeaseService.java:38)
	at org.gradle.internal.invocation.GradleBuildController.doBuild(GradleBuildController.java:98)
	at org.gradle.internal.invocation.GradleBuildController.run(GradleBuildController.java:74)
	at org.gradle.tooling.internal.provider.runner.BuildModelActionRunner.run(BuildModelActionRunner.java:55)
	at org.gradle.launcher.exec.ChainingBuildActionRunner.run(ChainingBuildActionRunner.java:35)
	at org.gradle.launcher.exec.ChainingBuildActionRunner.run(ChainingBuildActionRunner.java:35)
	at org.gradle.tooling.internal.provider.ValidatingBuildActionRunner.run(ValidatingBuildActionRunner.java:32)
	at org.gradle.launcher.exec.RunAsBuildOperationBuildActionRunner$3.run(RunAsBuildOperationBuildActionRunner.java:49)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:300)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:292)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:174)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:90)
	at org.gradle.internal.operations.DelegatingBuildOperationExecutor.run(DelegatingBuildOperationExecutor.java:31)
	at org.gradle.launcher.exec.RunAsBuildOperationBuildActionRunner.run(RunAsBuildOperationBuildActionRunner.java:44)
	at org.gradle.tooling.internal.provider.SubscribableBuildActionRunner.run(SubscribableBuildActionRunner.java:51)
	at org.gradle.launcher.exec.InProcessBuildActionExecuter$1.transform(InProcessBuildActionExecuter.java:47)
	at org.gradle.launcher.exec.InProcessBuildActionExecuter$1.transform(InProcessBuildActionExecuter.java:44)
	at org.gradle.composite.internal.DefaultRootBuildState.run(DefaultRootBuildState.java:79)
	at org.gradle.launcher.exec.InProcessBuildActionExecuter.execute(InProcessBuildActionExecuter.java:44)
	at org.gradle.launcher.exec.InProcessBuildActionExecuter.execute(InProcessBuildActionExecuter.java:30)
	at org.gradle.launcher.exec.BuildTreeScopeBuildActionExecuter.execute(BuildTreeScopeBuildActionExecuter.java:39)
	at org.gradle.launcher.exec.BuildTreeScopeBuildActionExecuter.execute(BuildTreeScopeBuildActionExecuter.java:25)
	at org.gradle.tooling.internal.provider.ContinuousBuildActionExecuter.execute(ContinuousBuildActionExecuter.java:80)
	at org.gradle.tooling.internal.provider.ContinuousBuildActionExecuter.execute(ContinuousBuildActionExecuter.java:53)
	at org.gradle.tooling.internal.provider.ServicesSetupBuildActionExecuter.execute(ServicesSetupBuildActionExecuter.java:62)
	at org.gradle.tooling.internal.provider.ServicesSetupBuildActionExecuter.execute(ServicesSetupBuildActionExecuter.java:34)
	at org.gradle.tooling.internal.provider.GradleThreadBuildActionExecuter.execute(GradleThreadBuildActionExecuter.java:36)
	at org.gradle.tooling.internal.provider.GradleThreadBuildActionExecuter.execute(GradleThreadBuildActionExecuter.java:25)
	at org.gradle.tooling.internal.provider.ParallelismConfigurationBuildActionExecuter.execute(ParallelismConfigurationBuildActionExecuter.java:43)
	at org.gradle.tooling.internal.provider.ParallelismConfigurationBuildActionExecuter.execute(ParallelismConfigurationBuildActionExecuter.java:29)
	at org.gradle.tooling.internal.provider.StartParamsValidatingActionExecuter.execute(StartParamsValidatingActionExecuter.java:59)
	at org.gradle.tooling.internal.provider.StartParamsValidatingActionExecuter.execute(StartParamsValidatingActionExecuter.java:31)
	at org.gradle.tooling.internal.provider.SessionFailureReportingActionExecuter.execute(SessionFailureReportingActionExecuter.java:59)
	at org.gradle.tooling.internal.provider.SessionFailureReportingActionExecuter.execute(SessionFailureReportingActionExecuter.java:44)
	at org.gradle.tooling.internal.provider.SetupLoggingActionExecuter.execute(SetupLoggingActionExecuter.java:46)
	at org.gradle.tooling.internal.provider.SetupLoggingActionExecuter.execute(SetupLoggingActionExecuter.java:30)
	at org.gradle.tooling.internal.provider.DaemonBuildActionExecuter.execute(DaemonBuildActionExecuter.java:60)
	at org.gradle.tooling.internal.provider.DaemonBuildActionExecuter.execute(DaemonBuildActionExecuter.java:41)
	at org.gradle.tooling.internal.provider.LoggingBridgingBuildActionExecuter.execute(LoggingBridgingBuildActionExecuter.java:58)
	at org.gradle.tooling.internal.provider.LoggingBridgingBuildActionExecuter.execute(LoggingBridgingBuildActionExecuter.java:37)
	at org.gradle.tooling.internal.provider.ProviderConnection.run(ProviderConnection.java:181)
	at org.gradle.tooling.internal.provider.ProviderConnection.run(ProviderConnection.java:124)
	at org.gradle.tooling.internal.provider.DefaultConnection.getModel(DefaultConnection.java:208)
	at org.gradle.tooling.internal.consumer.connection.CancellableModelBuilderBackedModelProducer.produceModel(CancellableModelBuilderBackedModelProducer.java:53)
	at org.gradle.tooling.internal.consumer.connection.PluginClasspathInjectionSupportedCheckModelProducer.produceModel(PluginClasspathInjectionSupportedCheckModelProducer.java:41)
	at org.gradle.tooling.internal.consumer.connection.AbstractConsumerConnection.run(AbstractConsumerConnection.java:59)
	at org.gradle.tooling.internal.consumer.connection.ParameterValidatingConsumerConnection.run(ParameterValidatingConsumerConnection.java:47)
	at org.gradle.tooling.internal.consumer.DefaultBuildLauncher$1.run(DefaultBuildLauncher.java:89)
	at org.gradle.tooling.internal.consumer.DefaultBuildLauncher$1.run(DefaultBuildLauncher.java:83)
	at org.gradle.tooling.internal.consumer.connection.LazyConsumerActionExecutor.run(LazyConsumerActionExecutor.java:84)
	at org.gradle.tooling.internal.consumer.connection.CancellableConsumerActionExecutor.run(CancellableConsumerActionExecutor.java:45)
	at org.gradle.tooling.internal.consumer.connection.ProgressLoggingConsumerActionExecutor.run(ProgressLoggingConsumerActionExecutor.java:58)
	at org.gradle.tooling.internal.consumer.connection.RethrowingErrorsConsumerActionExecutor.run(RethrowingErrorsConsumerActionExecutor.java:38)
	at org.gradle.tooling.internal.consumer.async.DefaultAsyncConsumerActionExecutor$1$1.run(DefaultAsyncConsumerActionExecutor.java:55)
	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:63)
	at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:46)
	at org.gradle.internal.concurrent.ThreadFactoryImpl$ManagedThreadRunnable.run(ThreadFactoryImpl.java:55)
Caused by: org.gradle.api.ProjectConfigurationException: A problem occurred configuring root project 'specific-transitive-candidates-or-snapshots-resolved'.
	at org.gradle.configuration.project.LifecycleProjectEvaluator.wrapException(LifecycleProjectEvaluator.java:80)
	at org.gradle.configuration.project.LifecycleProjectEvaluator.addConfigurationFailure(LifecycleProjectEvaluator.java:73)
	at org.gradle.configuration.project.LifecycleProjectEvaluator.access$400(LifecycleProjectEvaluator.java:54)
	at org.gradle.configuration.project.LifecycleProjectEvaluator$EvaluateProject.run(LifecycleProjectEvaluator.java:107)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:300)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:292)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:174)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:90)
	at org.gradle.internal.operations.DelegatingBuildOperationExecutor.run(DelegatingBuildOperationExecutor.java:31)
	at org.gradle.configuration.project.LifecycleProjectEvaluator.evaluate(LifecycleProjectEvaluator.java:68)
	at org.gradle.api.internal.project.DefaultProject.evaluate(DefaultProject.java:687)
	at org.gradle.api.internal.project.DefaultProject.evaluate(DefaultProject.java:140)
	at org.gradle.execution.TaskPathProjectEvaluator.configure(TaskPathProjectEvaluator.java:35)
	at org.gradle.execution.TaskPathProjectEvaluator.configureHierarchy(TaskPathProjectEvaluator.java:60)
	at org.gradle.configuration.DefaultBuildConfigurer.configure(DefaultBuildConfigurer.java:41)
	at org.gradle.initialization.DefaultGradleLauncher$ConfigureBuild.run(DefaultGradleLauncher.java:286)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:300)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:292)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:174)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:90)
	at org.gradle.internal.operations.DelegatingBuildOperationExecutor.run(DelegatingBuildOperationExecutor.java:31)
	at org.gradle.initialization.DefaultGradleLauncher.configureBuild(DefaultGradleLauncher.java:194)
	at org.gradle.initialization.DefaultGradleLauncher.doBuildStages(DefaultGradleLauncher.java:150)
	... 61 more
Caused by: org.gradle.api.internal.artifacts.ivyservice.DefaultLenientConfiguration$ArtifactResolveException: Could not resolve all artifacts for configuration ':classpath'.
	at org.gradle.api.internal.artifacts.configurations.DefaultConfiguration.rethrowFailure(DefaultConfiguration.java:1076)
	at org.gradle.api.internal.artifacts.configurations.DefaultConfiguration.access$1700(DefaultConfiguration.java:123)
	at org.gradle.api.internal.artifacts.configurations.DefaultConfiguration$ConfigurationArtifactCollection.ensureResolved(DefaultConfiguration.java:1511)
	at org.gradle.api.internal.artifacts.configurations.DefaultConfiguration$ConfigurationArtifactCollection.getArtifacts(DefaultConfiguration.java:1483)
	at org.gradle.composite.internal.CompositeBuildClassPathInitializer.execute(CompositeBuildClassPathInitializer.java:45)
	at org.gradle.composite.internal.CompositeBuildClassPathInitializer.execute(CompositeBuildClassPathInitializer.java:32)
	at org.gradle.api.internal.initialization.DefaultScriptClassPathResolver.resolveClassPath(DefaultScriptClassPathResolver.java:37)
	at org.gradle.api.internal.initialization.DefaultScriptHandler.getScriptClassPath(DefaultScriptHandler.java:72)
	at org.gradle.plugin.use.internal.DefaultPluginRequestApplicator.defineScriptHandlerClassScope(DefaultPluginRequestApplicator.java:204)
	at org.gradle.plugin.use.internal.DefaultPluginRequestApplicator.applyPlugins(DefaultPluginRequestApplicator.java:82)
	at org.gradle.configuration.DefaultScriptPluginFactory$ScriptPluginImpl.apply(DefaultScriptPluginFactory.java:186)
	at org.gradle.configuration.BuildOperationScriptPlugin$1$1.run(BuildOperationScriptPlugin.java:69)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:300)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:292)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:174)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:90)
	at org.gradle.internal.operations.DelegatingBuildOperationExecutor.run(DelegatingBuildOperationExecutor.java:31)
	at org.gradle.configuration.BuildOperationScriptPlugin$1.execute(BuildOperationScriptPlugin.java:66)
	at org.gradle.configuration.BuildOperationScriptPlugin$1.execute(BuildOperationScriptPlugin.java:63)
	at org.gradle.configuration.internal.DefaultUserCodeApplicationContext.apply(DefaultUserCodeApplicationContext.java:48)
	at org.gradle.configuration.BuildOperationScriptPlugin.apply(BuildOperationScriptPlugin.java:63)
	at org.gradle.configuration.project.BuildScriptProcessor.execute(BuildScriptProcessor.java:41)
	at org.gradle.configuration.project.BuildScriptProcessor.execute(BuildScriptProcessor.java:26)
	at org.gradle.configuration.project.ConfigureActionsProjectEvaluator.evaluate(ConfigureActionsProjectEvaluator.java:34)
	at org.gradle.configuration.project.LifecycleProjectEvaluator$EvaluateProject.run(LifecycleProjectEvaluator.java:105)
	... 80 more
Caused by: org.gradle.internal.resolve.ModuleVersionResolveException: Could not resolve test.nebula:a:1.1.1-rc.1.
Required by:
    project :
	at org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.EdgeState.calculateTargetConfigurations(EdgeState.java:187)
	at org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.EdgeState.attachToTargetConfigurations(EdgeState.java:129)
	at org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.DependencyGraphBuilder.attachToTargetRevisionsSerially(DependencyGraphBuilder.java:317)
	at org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.DependencyGraphBuilder.resolveEdges(DependencyGraphBuilder.java:216)
	at org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.DependencyGraphBuilder.traverseGraph(DependencyGraphBuilder.java:169)
	at org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.DependencyGraphBuilder.resolve(DependencyGraphBuilder.java:130)
	at org.gradle.api.internal.artifacts.ivyservice.resolveengine.DefaultArtifactDependencyResolver.resolve(DefaultArtifactDependencyResolver.java:121)
	at org.gradle.api.internal.artifacts.ivyservice.DefaultConfigurationResolver.resolveGraph(DefaultConfigurationResolver.java:169)
	at org.gradle.api.internal.artifacts.ivyservice.ShortCircuitEmptyConfigurationResolver.resolveGraph(ShortCircuitEmptyConfigurationResolver.java:86)
	at org.gradle.api.internal.artifacts.ivyservice.ErrorHandlingConfigurationResolver.resolveGraph(ErrorHandlingConfigurationResolver.java:73)
	at org.gradle.api.internal.artifacts.configurations.DefaultConfiguration$5.run(DefaultConfiguration.java:533)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:300)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:292)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:174)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:90)
	at org.gradle.internal.operations.DelegatingBuildOperationExecutor.run(DelegatingBuildOperationExecutor.java:31)
	at org.gradle.api.internal.artifacts.configurations.DefaultConfiguration.resolveGraphIfRequired(DefaultConfiguration.java:524)
	at org.gradle.api.internal.artifacts.configurations.DefaultConfiguration.resolveToStateOrLater(DefaultConfiguration.java:509)
	at org.gradle.api.internal.artifacts.configurations.DefaultConfiguration.access$1800(DefaultConfiguration.java:123)
	at org.gradle.api.internal.artifacts.configurations.DefaultConfiguration$ConfigurationFileCollection.getSelectedArtifacts(DefaultConfiguration.java:1059)
	at org.gradle.api.internal.artifacts.configurations.DefaultConfiguration$ConfigurationFileCollection.access$3100(DefaultConfiguration.java:993)
	at org.gradle.api.internal.artifacts.configurations.DefaultConfiguration$ConfigurationArtifactCollection.ensureResolved(DefaultConfiguration.java:1505)
	... 102 more
Caused by: org.gradle.internal.component.NoMatchingConfigurationSelectionException: Unable to find a matching variant of test.nebula:a:1.1.1-rc.1:
  - Variant 'compile':
      - Found org.gradle.component.category 'library' but wasn't required.
      - Required org.gradle.status 'release' and found incompatible value 'candidate'.
      - Found org.gradle.usage 'java-api' but wasn't required.
  - Variant 'enforced-platform-compile':
      - Found org.gradle.component.category 'enforced-platform' but wasn't required.
      - Required org.gradle.status 'release' and found incompatible value 'candidate'.
      - Found org.gradle.usage 'java-api' but wasn't required.
  - Variant 'enforced-platform-runtime':
      - Found org.gradle.component.category 'enforced-platform' but wasn't required.
      - Required org.gradle.status 'release' and found incompatible value 'candidate'.
      - Found org.gradle.usage 'java-runtime' but wasn't required.
  - Variant 'platform-compile':
      - Found org.gradle.component.category 'platform' but wasn't required.
      - Required org.gradle.status 'release' and found incompatible value 'candidate'.
      - Found org.gradle.usage 'java-api' but wasn't required.
  - Variant 'platform-runtime':
      - Found org.gradle.component.category 'platform' but wasn't required.
      - Required org.gradle.status 'release' and found incompatible value 'candidate'.
      - Found org.gradle.usage 'java-runtime' but wasn't required.
  - Variant 'runtime':
      - Found org.gradle.component.category 'library' but wasn't required.
      - Required org.gradle.status 'release' and found incompatible value 'candidate'.
      - Found org.gradle.usage 'java-runtime' but wasn't required.
	at org.gradle.internal.component.model.AttributeConfigurationSelector.selectConfigurationUsingAttributeMatching(AttributeConfigurationSelector.java:46)
	at org.gradle.internal.component.model.LocalComponentDependencyMetadata.selectConfigurations(LocalComponentDependencyMetadata.java:133)
	at org.gradle.internal.component.local.model.DslOriginDependencyMetadataWrapper.selectConfigurations(DslOriginDependencyMetadataWrapper.java:60)
	at org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.EdgeState.calculateTargetConfigurations(EdgeState.java:184)
	... 123 more

```


>       - Required org.gradle.status 'release' and found incompatible value 'candidate'.

Ideally, we should be able to set the `status` to `candidate`. 