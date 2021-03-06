package myplugin

import groovy.transform.CompileDynamic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.artifacts.MutableVariantFilesMetadata
import org.gradle.api.artifacts.VariantMetadata
import org.gradle.api.artifacts.ivy.IvyModuleDescriptor
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.capabilities.MutableCapabilitiesMetadata
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.component.external.model.ivy.IvyModuleResolveMetadata

import javax.inject.Inject

class MyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.dependencies.components.all(IvyVariantDerivationRule)
    }

    @CompileDynamic
    @CacheableRule
    static class IvyVariantDerivationRule implements ComponentMetadataRule {
        @Inject
        ObjectFactory getObjects() { }

        void execute(ComponentMetadataContext context) {
            if(context.getDescriptor(IvyModuleDescriptor) == null) {
                return
            }

            boolean shouldRegisterIvyVariants = hasCompileArtifacts(context)
            if(!shouldRegisterIvyVariants) {
                return
            }

            context.details.maybeAddVariant('runtimeElements', 'default', new Action<VariantMetadata>() {
                @Override
                void execute(VariantMetadata variantMetadata) {
                    variantMetadata.attributes {
                        it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
                        it.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
                        it.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME))
                    }
                    variantMetadata.withFiles(new Action<MutableVariantFilesMetadata>() {
                        @Override
                        void execute(MutableVariantFilesMetadata mutableVariantFilesMetadata) {
                            mutableVariantFilesMetadata
                        }
                    })
                }
            })

             context.details.maybeAddVariant('apiElements', 'compile', new Action<VariantMetadata>() {
                @Override
                void execute(VariantMetadata variantMetadata) {
                    variantMetadata.attributes {
                        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements, LibraryElements.JAR))
                        attribute(Category.CATEGORY_ATTRIBUTE, getObjects().named(Category, Category.LIBRARY))
                        attribute(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage, Usage.JAVA_API))
                    }
                    variantMetadata.withCapabilities(new Action<MutableCapabilitiesMetadata>() {
                        @Override
                        void execute(MutableCapabilitiesMetadata mutableCapabilitiesMetadata) {
                            mutableCapabilitiesMetadata
                        }
                    })
                    variantMetadata.withFiles(new Action<MutableVariantFilesMetadata>() {
                        @Override
                        void execute(MutableVariantFilesMetadata mutableVariantFilesMetadata) {
                            mutableVariantFilesMetadata
                        }
                    })
                }
            })

        }

        private static boolean hasCompileArtifacts(ComponentMetadataContext context) {
            IvyModuleResolveMetadata ivyModuleResolveMetadata = context.metadata
            return ivyModuleResolveMetadata.artifactDefinitions.any {
                it.configurations.contains('compile')
            }
        }
    }
}