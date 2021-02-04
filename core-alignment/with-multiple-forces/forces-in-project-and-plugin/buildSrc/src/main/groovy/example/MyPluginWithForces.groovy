package example

import org.gradle.api.Plugin
import org.gradle.api.Project

class MyPluginWithForces implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.configurations.all {
            resolutionStrategy {
                force 'com.google.inject:guice:4.1.0'
                force 'com.google.inject.extensions:guice-assistedinject:4.1.0'
            }
        }
    }
}
