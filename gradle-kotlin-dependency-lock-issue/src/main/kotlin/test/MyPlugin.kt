package test

import org.gradle.api.Plugin
import org.gradle.api.Project

open class MyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        print("something")
    }
}
