import com.netflix.nebula.interop.onExecute
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer

const val RESOLUTION_RULES_CONFIG_NAME = "resolutionRules"

open class AlignPlugin : Plugin<Project> {
    private lateinit var project: Project
    private lateinit var configurations: ConfigurationContainer
    private val ignoredConfigurationPrefixes = listOf(RESOLUTION_RULES_CONFIG_NAME)

    override fun apply(project: Project) {
        this.project = project
        configurations = project.configurations

        project.rootProject.configurations.maybeCreate(RESOLUTION_RULES_CONFIG_NAME)
        project.configurations.all { config ->
            if (ignoredConfigurationPrefixes.any { config.name.startsWith(it) }) {
                return@all
            }

            project.onExecute {
                project.rootProject.configurations.maybeCreate(RESOLUTION_RULES_CONFIG_NAME)
                val configuration = project.configurations.getByName(RESOLUTION_RULES_CONFIG_NAME)
                val copy = configuration.copyRecursive()
                val copyConfiguration = CopiedConfiguration(configuration, project, copy)
                val files = copyConfiguration.resolve()

                project.dependencies.components.all(AlignedPlatformMetadataRule::class.java)
            }
        }
    }

    class AlignedPlatformMetadataRule : ComponentMetadataRule {
        override fun execute(ctx: ComponentMetadataContext) {
            if (ctx.details.id.group.startsWith("test.nebula")) {
                ctx.details.belongsTo("test.nebula:test.nebula:${ctx.details.id.version}")
            }
        }
    }
}

class CopiedConfiguration(val source: Configuration, val project: Project, copy: Configuration) : Configuration by copy