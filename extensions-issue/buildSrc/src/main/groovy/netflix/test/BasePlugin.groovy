package netflix.test

import org.gradle.api.Plugin
import org.gradle.api.Project


class BasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def extension = project.extensions.create('myExtension', MyExtension, project)

    }
}
