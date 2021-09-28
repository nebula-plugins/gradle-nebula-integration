package nebula

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceSetOutput
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test

class MySourceSetPlugin implements Plugin<Project> {

    private static final String IMPLEMENTATION_CONFIG_NAME = 'implementation'
    private static final String MAIN_SOURCE_SET_NAME = 'main'
    Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.plugins.withType(JavaBasePlugin) {
            JavaPluginConvention javaConvention = project.convention.getPlugin(JavaPluginConvention)
            SourceSetContainer sourceSets = javaConvention.sourceSets
            ConfigurationContainer configurations = project.configurations
            sourceSets.matching { SourceSet sourceSet -> sourceSet.name == 'test' }.all { SourceSet parentSourceSet ->

                // sourceSets.create('integrationTest')
                SourceSet sourceSet = sourceSets.create('integrationTest')  { SourceSet sourceSet ->
                    //our new source set needs to see compiled classes from its parent
                    //the parent can be also inheriting so we need to extract all the output from previous parents
                    //e.g smokeTest inherits from test which inherits from main and we need to see classes from main
                    Set<Object> compileClasspath = new LinkedHashSet<Object>()
                    compileClasspath.add(sourceSet.compileClasspath)
                    addParentSourceSetOutputs(compileClasspath, parentSourceSet, sourceSets)

                    //we are using from to create ConfigurableFileCollection so if we keep inhering from created facets we can
                    //still extract chain of output from all parents
                    sourceSet.compileClasspath = project.objects.fileCollection().from(compileClasspath as Object[])
                    //runtime classpath of parent already has parent output so we don't need to explicitly add it
                    Set<Object> runtimeClasspath = new LinkedHashSet<Object>()
                    runtimeClasspath.add(sourceSet.runtimeClasspath)
                    addParentSourceSetOutputs(runtimeClasspath, parentSourceSet, sourceSets)

                    sourceSet.runtimeClasspath = project.objects.fileCollection().from(runtimeClasspath as Object[])
                }

                Configuration implementationConfiguration = configurations.getByName(parentSourceSet.implementationConfigurationName)
                Configuration runtimeOnlyConfiguration = configurations.getByName(parentSourceSet.runtimeOnlyConfigurationName)
                Configuration annotationProcessorConfiguration = configurations.getByName(parentSourceSet.annotationProcessorConfigurationName)
                Configuration facetImplementationConfiguration = configurations.getByName(sourceSet.implementationConfigurationName)
                Configuration facetRuntimeOnlyConfiguration = configurations.getByName(sourceSet.runtimeOnlyConfigurationName)
                Configuration facetAnnotationProcessorConfiguration = configurations.getByName(sourceSet.annotationProcessorConfigurationName)

                facetImplementationConfiguration.extendsFrom(implementationConfiguration)
                facetRuntimeOnlyConfiguration.extendsFrom(runtimeOnlyConfiguration)
                facetAnnotationProcessorConfiguration.extendsFrom(annotationProcessorConfiguration)

                project.tasks.named('build').configure(new Action<Task>() {
                    @Override
                    void execute(Task buildTask) {
                        buildTask.dependsOn(sourceSet.classesTaskName)
                    }
                })

                TaskProvider<Test> testTask = createTestTask('integrationTest', sourceSet)
                project.tasks.named('check') configure(new Action<Task>() {
                    @Override
                    void execute(Task checkTask) {
                        checkTask.dependsOn(testTask)
                    }
                })
            }
        }


    }
    private void addParentSourceSetOutputs( Set<Object> classpath, SourceSet parentSourceSet, SourceSetContainer sourceSets) {
        classpath.add(parentSourceSet.output)
        Configuration parentSourceSetImplementationConfiguration = project.configurations.findByName(parentSourceSet.implementationConfigurationName)
        if(!parentSourceSetImplementationConfiguration || !parentSourceSetImplementationConfiguration.extendsFrom) {
            return
        }

        Configuration extendsFrom = parentSourceSetImplementationConfiguration.extendsFrom.find()
        if(!extendsFrom) {
            return
        }

        if(extendsFrom.name == IMPLEMENTATION_CONFIG_NAME) {
            addParentSourceSetOutputs(classpath, sourceSets.getByName(MAIN_SOURCE_SET_NAME), sourceSets)
        } else {
            addParentSourceSetOutputs(classpath, sourceSets.getByName(extendsFrom.name.replaceAll(IMPLEMENTATION_CONFIG_NAME.capitalize(), '')), sourceSets)
        }
    }
    /**
     * Creates the integration test Gradle task and defines the output directories.
     *
     * @param sourceSet to be used for the integration test task.
     * @return the integration test task, as a Gradle Test object.
     */
    TaskProvider<Test> createTestTask(String testName, SourceSet sourceSet) {
        TaskProvider<Test> testTask = project.tasks.register(testName, Test)
        testTask.configure(new Action<Test>() {
            @Override
            void execute(Test test) {
                test.setGroup(JavaBasePlugin.VERIFICATION_GROUP)
                test.setDescription("Runs the ${sourceSet.name} tests")
                test.reports.html.setDestination(new File("${project.buildDir}/reports/${sourceSet.name}"))
                test.reports.junitXml.setDestination(new File("${project.buildDir}/${sourceSet.name}-results"))
                test.testClassesDirs = sourceSet.output.classesDirs
                test.classpath = sourceSet.runtimeClasspath
                test.shouldRunAfter(project.tasks.named('test'))
            }
        })

        testTask
    }

    /**
     * Based on the JavaPluginConvention, creates a SourceSet for the appropriate to UsableSourceSet.
     *
     * @return the new SourceSet
     */
    SourceSet createSourceSet(SourceSet parentSourceSet) {
        JavaPluginConvention javaConvention = project.convention.getPlugin(JavaPluginConvention)
        SourceSetContainer sourceSets = javaConvention.sourceSets
       // sourceSets.create('integrationTest')
        sourceSets.create('integrationTest') { SourceSet sourceSet ->
            //our new source set needs to see compiled classes from its parent
            //the parent can be also inheriting so we need to extract all the output from previous parents
            //e.g smokeTest inherits from test which inherits from main and we need to see classes from main
            Set<Object> compileClasspath = new LinkedHashSet<Object>()
            compileClasspath.add(sourceSet.compileClasspath)
            compileClasspath.add(parentSourceSet.output)
          //  compileClasspath.addAll(extractAllOutputs(parentSourceSet.compileClasspath))

            //we are using from to create ConfigurableFileCollection so if we keep inhering from created facets we can
            //still extract chain of output from all parents
            sourceSet.compileClasspath = project.objects.fileCollection().from(compileClasspath as Object[])
            //runtime classpath of parent already has parent output so we don't need to explicitly add it
            Set<Object> runtimeClasspath = new LinkedHashSet<Object>()
            runtimeClasspath.add(sourceSet.runtimeClasspath)
            runtimeClasspath.add(parentSourceSet.output)
        //    runtimeClasspath.addAll(extractAllOutputs(parentSourceSet.runtimeClasspath))

            sourceSet.runtimeClasspath = project.objects.fileCollection().from(runtimeClasspath as Object[])
        }
    }


    private Set<Object> extractAllOutputs(FileCollection classpath) {
        if (classpath instanceof ConfigurableFileCollection) {
            def from = (classpath as ConfigurableFileCollection).from
            boolean usesJvmTestPlugin = from.any { it.class.name.contains('JvmTestSuitePlugin')}
            def testing = project.getExtensions().getByType(org.gradle.testing.base.TestingExtension)
            testing.suites.withType(org.gradle.api.plugins.jvm.JvmTestSuite) {
                true
            }
            from.findAll { it instanceof FileCollection }.collectMany { extractAllOutputs(it as FileCollection) } as Set<Object>
        }
        else if (classpath instanceof UnionFileCollection) {
            (classpath as UnionFileCollection).sources.collectMany { extractAllOutputs(it) } as Set<Object>
        }
        else if (classpath instanceof SourceSetOutput) {
            [classpath] as Set<Object>
        }
        else {
            new LinkedHashSet<Object>()
        }
    }


}
