package netflix.nebula

import org.gradle.api.Plugin
import org.gradle.api.Project

class DependencyConstraintsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.afterEvaluate {
            project.rootProject.childProjects.values().each {
                if(project.name != it.name) {
                    project.dependencies.constraints.add("compile", it)
                }
            }
        }
    }
}
