package gradle.module.replacement.test.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class DependencyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        addCollectionsDependendency(project, "api")
    }

    private void addCollectionsDependendency(Project project, String configurationName) {
        project.dependencies.add(
                configurationName,
                "com.google.collections:google-collections:1.0"
        )
    }
}
