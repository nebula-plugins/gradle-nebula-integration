package nebula.plugin.test

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

class MyBuildListener implements BuildListener, ProjectEvaluationListener, TaskExecutionListener, DependencyResolutionListener {

    private isBuildStarted = false

    @Override
    void buildStarted(Gradle gradle) {
        isBuildStarted = true
    }

    @Override
    void settingsEvaluated(Settings settings) {
        if(!isBuildStarted) {
            throw new RuntimeException("buildStarted not triggered")
        }
    }

    @Override
    void projectsLoaded(Gradle gradle) {
        if(!isBuildStarted) {
            throw new RuntimeException("buildStarted not triggered")
        }
    }

    @Override
    void projectsEvaluated(Gradle gradle) {
        if(!isBuildStarted) {
            throw new RuntimeException("buildStarted not triggered")
        }
    }

    @Override
    void buildFinished(BuildResult buildResult) {
        if(!isBuildStarted) {
            throw new RuntimeException("buildStarted not triggered")
        }
    }

    @Override
    void beforeEvaluate(Project project) {
        if(!isBuildStarted) {
            throw new RuntimeException("buildStarted not triggered")
        }
    }

    @Override
    void afterEvaluate(Project project, ProjectState projectState) {
        if(!isBuildStarted) {
            throw new RuntimeException("buildStarted not triggered")
        }
    }

    @Override
    void beforeResolve(ResolvableDependencies resolvableDependencies) {
        if(!isBuildStarted) {
            throw new RuntimeException("buildStarted not triggered")
        }
    }

    @Override
    void afterResolve(ResolvableDependencies resolvableDependencies) {
        if(!isBuildStarted) {
            throw new RuntimeException("buildStarted not triggered")
        }
    }

    @Override
    void beforeExecute(Task task) {
        if(!isBuildStarted) {
            throw new RuntimeException("buildStarted not triggered")
        }
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        if(!isBuildStarted) {
            throw new RuntimeException("buildStarted not triggered")
        }
    }

}
