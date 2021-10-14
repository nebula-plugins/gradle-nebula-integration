# test distribution + org.gradle.testfixtures.ProjectBuilder jdk 17 issue 

Running this project with test distribution enabled results in

```
[com.gradle.enterprise.testdistribution] Test was part of session 2 and ran on a local executor named 'localhost-executor-1' on host 'localhost'.

Could not inject synthetic classes.
org.gradle.api.GradleException: Could not inject synthetic classes.
	at app//org.gradle.initialization.DefaultLegacyTypesSupport.injectEmptyInterfacesIntoClassLoader(DefaultLegacyTypesSupport.java:91)
	at app//org.gradle.testfixtures.internal.ProjectBuilderImpl.getGlobalServices(ProjectBuilderImpl.java:181)
	at app//org.gradle.testfixtures.internal.ProjectBuilderImpl.createProject(ProjectBuilderImpl.java:112)
	at app//org.gradle.testfixtures.ProjectBuilder.build(ProjectBuilder.java:120)
	at app//netflix.PluginProjectSpec.setup(PluginProjectSpec.groovy:21)
Caused by: java.lang.RuntimeException: java.lang.IllegalAccessException: module java.base does not open java.lang to unnamed module @7825f19b
	at app//org.gradle.internal.classloader.ClassLoaderUtils$AbstractClassLoaderLookuper.invoke(ClassLoaderUtils.java:150)
	at app//org.gradle.internal.classloader.ClassLoaderUtils$LookupClassDefiner.defineClass(ClassLoaderUtils.java:206)
	at app//org.gradle.internal.classloader.ClassLoaderUtils.define(ClassLoaderUtils.java:77)
	at app//org.gradle.initialization.DefaultLegacyTypesSupport.injectEmptyInterfacesIntoClassLoader(DefaultLegacyTypesSupport.java:88)
	... 4 more
Caused by: java.lang.IllegalAccessException: module java.base does not open java.lang to unnamed module @7825f19b
	at java.base@17/java.lang.invoke.MethodHandles.privateLookupIn(MethodHandles.java:259)
	at app//org.gradle.internal.classloader.ClassLoaderUtils$AbstractClassLoaderLookuper.getLookupForClassLoader(ClassLoaderUtils.java:159)
	at app//org.gradle.internal.classloader.ClassLoaderUtils$AbstractClassLoaderLookuper.invoke(ClassLoaderUtils.java:146)
	... 7 more

```
