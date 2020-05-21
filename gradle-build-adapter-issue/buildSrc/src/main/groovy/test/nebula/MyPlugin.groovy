package test.nebula

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

class MyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        final GradleBuildMetricsCollector gradleCollector = new GradleBuildMetricsCollector()
        final Gradle gradle = project.rootProject.gradle
        gradle.addListener(gradleCollector)
    }
}
