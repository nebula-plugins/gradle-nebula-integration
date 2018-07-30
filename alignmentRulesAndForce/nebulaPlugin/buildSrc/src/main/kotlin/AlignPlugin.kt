
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * A module set lists all modules that, by default, belong together (typically lists all modules of a group)
 */
private data class ModuleSet(val modules: List<String>, val reason: String)

open class AlignPlugin: Plugin<Project> {
    
    private val alignJacksonModuleSet = ModuleSet(
            modules = listOf("com.fasterxml.jackson.core:jackson-annotations",
                    "com.fasterxml.jackson.core:jackson-core",
                    "com.fasterxml.jackson.core:jackson-databind",
                    "com.fasterxml.jackson.dataformat:jackson-dataformat-avro",
                    "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor",
                    "com.fasterxml.jackson.dataformat:jackson-dataformat-csv",
                    "com.fasterxml.jackson.dataformat:jackson-dataformat-properties",
                    "com.fasterxml.jackson.dataformat:jackson-dataformat-protobuf",
                    "com.fasterxml.jackson.dataformat:jackson-dataformat-smile",
                    "com.fasterxml.jackson.dataformat:jackson-dataformat-xml",
                    "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml",
                    "com.fasterxml.jackson.dataformat:jackson-dataformats-binary",
                    "com.fasterxml.jackson.dataformat:jackson-dataformats-text",
                    "com.fasterxml.jackson.datatype:jackson-datatype-guava",
                    "com.fasterxml.jackson.datatype:jackson-datatype-hibernate-parent",
                    "com.fasterxml.jackson.datatype:jackson-datatype-hibernate3",
                    "com.fasterxml.jackson.datatype:jackson-datatype-hibernate4",
                    "com.fasterxml.jackson.datatype:jackson-datatype-hibernate5",
                    "com.fasterxml.jackson.datatype:jackson-datatype-hppc",
                    "com.fasterxml.jackson.datatype:jackson-datatype-jaxrs",
                    "com.fasterxml.jackson.datatype:jackson-datatype-jdk8",
                    "com.fasterxml.jackson.datatype:jackson-datatype-joda",
                    "com.fasterxml.jackson.datatype:jackson-datatype-json-org",
                    "com.fasterxml.jackson.datatype:jackson-datatype-jsr310",
                    "com.fasterxml.jackson.datatype:jackson-datatype-jsr353",
                    "com.fasterxml.jackson.datatype:jackson-datatype-pcollections",
                    "com.fasterxml.jackson.datatype:jackson-datatypes-collections",
                    "com.fasterxml.jackson.jaxrs:jackson-jaxrs-base",
                    "com.fasterxml.jackson.jaxrs:jackson-jaxrs-cbor-provider",
                    "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider",
                    "com.fasterxml.jackson.jaxrs:jackson-jaxrs-providers",
                    "com.fasterxml.jackson.jaxrs:jackson-jaxrs-smile-provider",
                    "com.fasterxml.jackson.jaxrs:jackson-jaxrs-xml-provider",
                    "com.fasterxml.jackson.jaxrs:jackson-jaxrs-yaml-provider",
                    "com.fasterxml.jackson.jr:jackson-jr-all",
                    "com.fasterxml.jackson.jr:jackson-jr-objects",
                    "com.fasterxml.jackson.jr:jackson-jr-parent",
                    "com.fasterxml.jackson.jr:jackson-jr-retrofit2",
                    "com.fasterxml.jackson.jr:jackson-jr-stree",
                    "com.fasterxml.jackson.module:jackson-module-afterburner",
                    "com.fasterxml.jackson.module:jackson-module-guice",
                    "com.fasterxml.jackson.module:jackson-module-jaxb-annotations",
                    "com.fasterxml.jackson.module:jackson-module-jsonSchema",
                    "com.fasterxml.jackson.module:jackson-module-kotlin",
                    "com.fasterxml.jackson.module:jackson-module-mrbean",
                    "com.fasterxml.jackson.module:jackson-module-osgi",
                    "com.fasterxml.jackson.module:jackson-module-parameter-names",
                    "com.fasterxml.jackson.module:jackson-module-paranamer",
                    "com.fasterxml.jackson.module:jackson-modules-base",
                    "com.fasterxml.jackson.module:jackson-modules-java8"),
            reason = "Jackson has to be the same"
    )

    override fun apply(project: Project) {
        project.dependencies.align(alignJacksonModuleSet)
    }
}

/**
 * The alignment pattern:
 * Each known version of a module in a modules set defines a constraint to
 * all other modules of the set with the same version.
 */
private fun DependencyHandler.align(moduleSet: ModuleSet) {
    moduleSet.modules.forEach { moduleName ->
        this.components.withModule(moduleName) {
            val version = id.version
            allVariants {
                withDependencyConstraints {
                    moduleSet.modules.filter { moduleName != it }.forEach {
                        add("$it:(,$version]") {
                            because(moduleSet.reason)
                        }
                    }
                }
            }
        }
    }
}
