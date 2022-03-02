package netflix.test

import org.gradle.api.Project


class MyExtension {
    protected final Project project
    def defaultVersionStrategy

    MyExtension(Project project) {
        this.project = project
        def sharedVersion = new DelayedVersion()
        project.rootProject.allprojects { Project p ->
            p.version = sharedVersion
        }
    }

    private class DelayedVersion implements Serializable {
        def inferredVersion

        private void infer() {
            defaultVersionStrategy
            println defaultVersionStrategy
        }

        @Override
        String toString() {
            if (!inferredVersion) {
                infer()
                inferredVersion = '1.0.0'
            }
            return inferredVersion
        }
    }

}