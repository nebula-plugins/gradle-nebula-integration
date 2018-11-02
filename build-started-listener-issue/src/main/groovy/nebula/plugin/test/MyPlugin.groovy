package nebula.plugin.test

import org.gradle.api.Plugin
import org.gradle.api.Project

class MyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        final MyBuildListener myListener = new MyBuildListener()
        project.gradle.addListener(myListener)
    }
}
