package nebula

import org.gradle.api.BuildCancelledException;
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.ConsoleRenderer;

 class DumpDependenciesTask extends DefaultTask  {
    @OutputDirectory
    def dependenciesDir = new File(project.buildDir.toString() + File.separator + 'dependencies-dump')

    @TaskAction
    def dumpBuildScriptDependenciesTask() {
        def rawConfigurations = 'classpath'
        def configurations = project.getBuildscript().getConfigurations()
        dumpDependenciesFor(rawConfigurations, configurations, dependenciesDir)
    }

    private  dumpDependenciesFor(String rawConfigurations, ConfigurationContainer configurations, File dependenciesDir) {
        dependenciesDir.deleteDir()
        dependenciesDir.mkdirs()
        def configurationsToDump = rawConfigurations.split(',').collect { it.trim() }
        configurationsToDump.each { configurationName ->
            def configuration = configurations.find { it.name == configurationName }
            if (configuration) {
                def resolvedDependencies = configuration.incoming.resolutionResult.getAllDependencies()
                def output = new File(dependenciesDir.toString() + '/' + project.name + '-' + configuration.name + '.txt')
                output << resolvedDependencies.size()
                logger.lifecycle("Dependencies for '${configuration.name}' are listed at ${new ConsoleRenderer().asClickableFileUrl(output)}")
            }
        }
    }
}
