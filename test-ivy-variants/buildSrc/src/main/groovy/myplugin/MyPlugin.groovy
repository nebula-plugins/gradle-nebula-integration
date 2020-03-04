package myplugin

import groovy.transform.CompileDynamic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.artifacts.VariantMetadata
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

class MyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.buildscript { ScriptHandler b ->
            b.dependencies.components.each {
                it
            }
            b.dependencies.components.all(IvyVariantDerivationRule)
        }
        project.dependencies.components.all(IvyVariantDerivationRule)
    }

    @CompileDynamic
    @CacheableRule
    static class IvyVariantDerivationRule implements ComponentMetadataRule {
        @Inject
        ObjectFactory getObjects() { }

        void execute(ComponentMetadataContext context) {
            context.details.maybeAddVariant('runtimeElements', 'default', new Action<VariantMetadata>() {
                @Override
                void execute(VariantMetadata variantMetadata) {
                    variantMetadata.attributes {
                        it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
                        it.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
                        it.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME))
                    }
                }
            })
            context.details.maybeAddVariant("apiElements", "compile") {
                attributes {
                    attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements, LibraryElements.JAR))
                    attribute(Category.CATEGORY_ATTRIBUTE, getObjects().named(Category, Category.LIBRARY))
                    attribute(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage, Usage.JAVA_API))
                }
            }
        }
    }
}