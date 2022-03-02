package netflix.test

import org.gradle.api.Plugin
import org.gradle.api.Project


class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project == project.rootProject) {
            project.plugins.apply(BasePlugin)
            MyExtension myExtension = project.extensions.findByType(MyExtension)
            myExtension.with {
                defaultVersionStrategy = 'test'
            }
        }

    }
}