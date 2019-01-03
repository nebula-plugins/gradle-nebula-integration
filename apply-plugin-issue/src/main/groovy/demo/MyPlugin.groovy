package demo

import nebula.plugin.publishing.maven.MavenDeveloperPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.with {
            apply MavenDeveloperPlugin
        }
    }
}