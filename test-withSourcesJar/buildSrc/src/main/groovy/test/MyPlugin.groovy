package test

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension

class MyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        JavaPluginExtension javaPluginExtension = project.extensions.getByType(JavaPluginExtension)
        javaPluginExtension.withSourcesJar()
        javaPluginExtension.withJavadocJar()
    }
}
