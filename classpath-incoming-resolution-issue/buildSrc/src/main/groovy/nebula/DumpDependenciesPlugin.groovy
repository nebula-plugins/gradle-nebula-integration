package nebula

import org.gradle.api.Plugin
import org.gradle.api.Project

class DumpDependenciesPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.tasks.register("dumpDependencies", DumpDependenciesTask)
    }
}
