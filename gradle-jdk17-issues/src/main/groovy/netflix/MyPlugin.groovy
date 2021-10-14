package netflix

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class MyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        //do nothing
    }
}
