package netflix

import org.gradle.api.GradleException
import org.gradle.api.plugins.JavaPlugin

class MySpec extends PluginProjectSpec {
    String pluginName = 'my.plugin'

    def 'Plugin is applied with a single project'() {
        when:
        project.plugins.apply(MyPlugin)
        project.plugins.apply(JavaPlugin)

        then:
        notThrown(GradleException)
    }
    def 'Plugin is applied with a single project 2'() {
        when:
        project.plugins.apply(MyPlugin)
        project.plugins.apply(JavaPlugin)

        then:
        notThrown(GradleException)
    }
    def 'Plugin is applied with a single project 3'() {
        when:
        project.plugins.apply(MyPlugin)
        project.plugins.apply(JavaPlugin)

        then:
        notThrown(GradleException)
    }
    def 'Plugin is applied with a single project 4'() {
        when:
        project.plugins.apply(MyPlugin)
        project.plugins.apply(JavaPlugin)

        then:
        notThrown(GradleException)
    }
    def 'Plugin is applied with a single project 5'() {
        when:
        project.plugins.apply(MyPlugin)
        project.plugins.apply(JavaPlugin)

        then:
        notThrown(GradleException)
    }
}
