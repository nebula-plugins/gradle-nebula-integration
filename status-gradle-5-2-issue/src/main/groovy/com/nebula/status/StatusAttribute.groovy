package com.nebula.status

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataDetails
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.CompatibilityCheckDetails
import org.gradle.api.internal.project.ProjectInternal

class StatusAttribute {

    static void configureStatusAttribute(Project project) {
        project.ext.release = {
            withStatus('release')
        }

        project.ext.candidate = {
            withStatus('candidate')
        }

        project.ext.integration = {
            withStatus('integration')
        }

        project.ext.snapshot = {
            withStatus('snapshot')
        }

        project.buildscript {
            configureDefaultStatusAttribute(it.configurations)
            configureStatusOverrideForDirectDependencies(it.configurations)
            configureStatusOverrideForTransitiveDependencies(it.dependencies)
            configureStatusAttributeCompatibility(it.dependencies)
        }
    }

    static void configureDefaultStatusAttribute(configurations) {
        configurations.all {
            attributes(withStatus('release'))
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
        dependencies.components.all(TransitiveDependenciesStatusRule)
    }

    private static void setLowerStatusAttributeBasedOnVersion(dependency, version) {
        if (isCandidate(version))
            dependency.attributes(withStatus('candidate'))
        if (isSnapshot(version))
            dependency.attributes(withStatus('snapshot'))
    }

    private static boolean isSnapshot(version) {
        version == "latest.snapshot" || version =~ StatusSchemePlugin.SNAPSHOT_VERSION
    }

    private static boolean isCandidate(version) {
        version == "latest.candidate" || version =~ StatusSchemePlugin.CANDIDATE_VERSION
    }

    static void configureStatusAttributeCompatibility(dependencies) {
        dependencies.attributesSchema {
            attribute(ProjectInternal.STATUS_ATTRIBUTE) {
                compatibilityRules.add(StatusCompatibilityRule)
            }
        }
    }

    static Action<AttributeContainer> withStatus(String requestedStatus) {
        return new Action<AttributeContainer>() {
            void execute(AttributeContainer attributes) {
                attributes.attribute(ProjectInternal.STATUS_ATTRIBUTE, requestedStatus)
            }
        }
    }

    static class StatusCompatibilityRule implements AttributeCompatibilityRule<String> {
        void execute(CompatibilityCheckDetails<String> details) {
            // Requested 'integration' status is compatible with anything
            if (details.consumerValue == "snapshot") {
                details.compatible()
            }
            if (details.consumerValue == "integration" && (details.producerValue == "integration" || details.producerValue == "candidate" || details.producerValue == "release")) {
                details.compatible()
            }
            if (details.consumerValue == "candidate" && (details.producerValue == "candidate" || details.producerValue == "release")) {
                details.compatible()
            }
        }
    }

    static class TransitiveDependenciesStatusRule implements ComponentMetadataRule {
        @Override
        void execute(ComponentMetadataContext componentMetadataContext) {
            ComponentMetadataDetails details = componentMetadataContext.details
            details.allVariants {
                withDependencies { transitiveDependencies ->
                    transitiveDependencies.each { dependency ->
                        setLowerStatusAttributeBasedOnVersion(dependency, dependency.versionConstraint.requiredVersion)
                    }
                }
            }
        }
    }
}
