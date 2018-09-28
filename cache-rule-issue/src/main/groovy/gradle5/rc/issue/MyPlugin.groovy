package gradle5.rc.issue

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.artifacts.ComponentMetadataRule

class MyPlugin implements Plugin<Project> {
    static final CANDIDATE_VERSION = ~/(?i).+(-|\.)(BETA|CANDIDATE|CR|RC).*/

    @Override
    void apply(Project project) {
        project.buildscript { ScriptHandler b ->
            defineStatuses(b.dependencies)
        }
        defineStatuses(project.dependencies)
    }

    static def defineStatuses(DependencyHandler dependencies) {
        dependencies.components.all(CacheableStatusRule)
    }
}
