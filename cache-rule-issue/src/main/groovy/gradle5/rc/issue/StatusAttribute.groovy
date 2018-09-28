package gradle5.rc.issue

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.internal.project.ProjectInternal

class StatusAttribute {

    static void configureStatusAttribute(Project project) {
        project.ext.release = {
            withStatus('release')
        }

        project.ext.candidate = {
            withStatus('candidate')
        }

        project.buildscript {
            configureDefaultStatusAttribute(it.configurations)
            configureStatusOverrideForDirectDependencies(it.configurations)
            configureStatusOverrideForTransitiveDependencies(it.dependencies)
            it.configurations.each {
                it
            }
        }
    }

    static void configureDefaultStatusAttribute(configurations) {
        configurations.all {
            attributes(withStatus('release'))
        }
    }

    static Action<AttributeContainer> withStatus(String requestedStatus) {
        return new Action<AttributeContainer>() {
            void execute(AttributeContainer attributes) {
                attributes.attribute(ProjectInternal.STATUS_ATTRIBUTE, requestedStatus)
            }
        }
    }


    static void configureStatusOverrideForDirectDependencies(configurations) {
        configurations.all {
            withDependencies { dependencies ->
                dependencies.each { dependency ->
                    setLowerStatusAttributeBasedOnVersion(dependency, dependency.version)
                }
            }
        }
    }

    static void configureStatusOverrideForTransitiveDependencies(dependencies) {
        dependencies.components.all { componentMetadata ->
            allVariants {
                withDependencies { transitiveDependencies ->
                    transitiveDependencies.each { dependency ->
                        setLowerStatusAttributeBasedOnVersion(dependency, dependency.versionConstraint.preferredVersion)
                        setLowerStatusAttributeBasedOnVersion(dependency, dependency.versionConstraint.requiredVersion)
                    }
                }
            }
        }
    }

    private static void setLowerStatusAttributeBasedOnVersion(dependency, version) {
        if (isCandidate(version))
            dependency.attributes(withStatus('candidate'))
    }

    private static boolean isCandidate(version) {
        version == "latest.candidate" || version =~ MyPlugin.CANDIDATE_VERSION
    }
}