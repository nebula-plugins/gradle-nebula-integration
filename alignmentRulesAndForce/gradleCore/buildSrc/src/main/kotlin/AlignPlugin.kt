
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataDetails
import org.gradle.api.artifacts.ComponentMetadataRule

open class AlignPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.dependencies.components.all(AlignJackson::class.java)
    }
}

open class AlignJackson: ComponentMetadataRule {
    override fun execute(ctx: ComponentMetadataContext) {
        ctx.details.run {
            if (id.group.startsWith("com.fasterxml.jackson")) {
                belongsTo("com.fasterxml.jackson:jackson-platform:${id.version}")
            }
        }
    }
}
