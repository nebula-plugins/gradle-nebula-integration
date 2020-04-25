package myplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

class MyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.dependencies.components.all(IvyVariantDerivationRule)
    }

    @CacheableRule
    static class IvyVariantDerivationRule implements ComponentMetadataRule {

        @Inject
        ObjectFactory getObjects() { }

        void execute(ComponentMetadataContext context) {
            //DO nothing
        }
    }
}