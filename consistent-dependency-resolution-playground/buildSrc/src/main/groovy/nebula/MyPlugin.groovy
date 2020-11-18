package nebula

import nebula.plugin.responsible.FacetDefinition
import nebula.plugin.responsible.TestFacetDefinition
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceSetOutput


class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.withType(JavaBasePlugin) {
            def facet = new TestFacetDefinition('smokeTest')
            facet.parentSourceSet = 'test'
            facet.testTaskName = 'smokeTest'

            JavaPluginConvention javaConvention = project.convention.getPlugin(JavaPluginConvention)
            SourceSetContainer sourceSets = javaConvention.sourceSets
            sourceSets.matching { SourceSet sourceSet -> sourceSet.name == facet.parentSourceSet }.all { SourceSet parentSourceSet ->

                // Since we're using NamedContainerProperOrder, we're configured already.
                SourceSet sourceSet = createSourceSet(project, parentSourceSet, facet)

                Configuration parentCompile = project.configurations.getByName(parentSourceSet.compileClasspathConfigurationName)
                project.configurations.getByName(sourceSet.compileClasspathConfigurationName).extendsFrom(parentCompile)

                Configuration parentRuntime = project.configurations.getByName(parentSourceSet.runtimeClasspathConfigurationName)
                project.configurations.getByName(sourceSet.runtimeClasspathConfigurationName).extendsFrom(parentRuntime)

                Configuration annotationProcessor = project.configurations.getByName(parentSourceSet.annotationProcessorConfigurationName)
                project.configurations.getByName(sourceSet.annotationProcessorConfigurationName).extendsFrom(annotationProcessor)



            }
        }

    }

    SourceSet createSourceSet(Project project, SourceSet parentSourceSet, FacetDefinition set) {
        JavaPluginConvention javaConvention = project.convention.getPlugin(JavaPluginConvention)
        SourceSetContainer sourceSets = javaConvention.sourceSets
        sourceSets.create(set.name) { SourceSet sourceSet ->
            //our new source set needs to see compiled classes from its parent
            //the parent can be also inheriting so we need to extract all the output from previous parents
            //e.g smokeTest inherits from test which inherits from main and we need to see classes from main
            Set<Object> compileClasspath = new LinkedHashSet<Object>()
            compileClasspath.add(sourceSet.compileClasspath)
            compileClasspath.add(parentSourceSet.output)
            compileClasspath.addAll(extractAllOutputs(parentSourceSet.compileClasspath))

            //we are using from to create ConfigurableFileCollection so if we keep inhering from created facets we can
            //still extract chain of output from all parents
            sourceSet.compileClasspath = project.objects.fileCollection().from(compileClasspath as Object[])
            //runtime classpath of parent already has parent output so we don't need to explicitly add it
            Set<Object> runtimeClasspath = new LinkedHashSet<Object>()
            runtimeClasspath.add(sourceSet.runtimeClasspath)
            runtimeClasspath.addAll(extractAllOutputs(parentSourceSet.runtimeClasspath))

            sourceSet.runtimeClasspath = project.objects.fileCollection().from(runtimeClasspath as Object[])
        }
    }

    private static Set<Object> extractAllOutputs(FileCollection classpath) {
        if (classpath instanceof ConfigurableFileCollection) {
            (classpath as ConfigurableFileCollection).from.findAll {it instanceof FileCollection }. collectMany { extractAllOutputs(it as FileCollection) } as Set<Object>
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