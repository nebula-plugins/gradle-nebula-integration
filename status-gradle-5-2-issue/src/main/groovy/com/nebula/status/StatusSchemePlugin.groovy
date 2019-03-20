package com.nebula.status

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ComponentMetadataDetails
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.initialization.dsl.ScriptHandler

@CompileStatic
class StatusSchemePlugin implements Plugin<Project> {
    static final SNAPSHOT_VERSION = ~/(?i).+(-|\.)(ALPHA|SNAPSHOT|PR|DEV).*/
    static final CANDIDATE_VERSION = ~/(?i).+(-|\.)(BETA|CANDIDATE|CR|RC).*/
    static final List<String> DEFAULT_STATUS_SCHEME = ['snapshot', 'integration', 'candidate', 'release']

    @Override
    void apply(Project project) {
        project.buildscript { ScriptHandler b ->
            defineStatuses(b.dependencies)
        }
        defineStatuses(project.dependencies)
    }

    static defineStatuses(DependencyHandler dependencies) {
        dependencies.components.all(StatusSchemeRule)

    }

    static defineStatusesWithScheme(DependencyHandler dependencies) {
        dependencies.components.all { ComponentMetadataDetails details ->
            StatusSchemeRule.defineStatuses(details, DEFAULT_STATUS_SCHEME)
        }
    }
}