package test.nebula

import org.gradle.BuildAdapter
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle

class GradleBuildMetricsCollector extends BuildAdapter  implements ProjectEvaluationListener {

    @Override
    void settingsEvaluated(Settings settings) {
        throw new RuntimeException("Can run when settings evaluated")
    }

    @Override
    void projectsLoaded(Gradle gradle) {
        throw new RuntimeException("Can not run when projects loaded")
    }

    @Override
    void beforeEvaluate(Project project) {
        throw new RuntimeException("Can not run before evaluate")

    }

    @Override
    void afterEvaluate(Project project, ProjectState projectState) {
        println "WE ARE IN AFTER EVALUATE"
    }
}
