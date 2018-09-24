package gradle5.rc.issue

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ComponentMetadataDetails
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.initialization.dsl.ScriptHandler

class MyPlugin implements Plugin<Project> {
    static final CANDIDATE_VERSION = ~/(?i).+(-|\.)(BETA|CANDIDATE|CR|RC).*/
    static final List<String> DEFAULT_STATUS_SCHEME = ['candidate', 'release']

    @Override
    void apply(Project project) {
        project.buildscript { ScriptHandler b ->
            defineStatuses(b.dependencies, DEFAULT_STATUS_SCHEME)
        }
        defineStatuses(project.dependencies, DEFAULT_STATUS_SCHEME)
    }

    static def defineStatuses(DependencyHandler dependencies, List<String> statusScheme) {
        dependencies.components.all { ComponentMetadataDetails details ->
            def version = details.id.version
            if (version =~ CANDIDATE_VERSION) {
                details.status = 'candidate'
            }

            if (details.status == null) {
                details.status = 'release'
            }
            details.statusScheme = statusScheme
        }
    }
}
