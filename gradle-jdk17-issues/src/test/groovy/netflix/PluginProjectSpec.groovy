package netflix

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

abstract class PluginProjectSpec extends Specification {
    abstract String getPluginName()

    File ourProjectDir
    String canonicalName
    Project project

    def setup() {
        ourProjectDir = new File("build/nebulatest/${this.class.canonicalName}/somethingsomething").absoluteFile
        if (ourProjectDir.exists()) {
            ourProjectDir.deleteDir()
        }
        ourProjectDir.mkdirs()
        canonicalName = "somethingsomething"
        project = ProjectBuilder.builder().withName(canonicalName).withProjectDir(ourProjectDir).build()
    }

    def 'apply does not throw exceptions'() {
        when:
        project.apply plugin: pluginName

        then:
        noExceptionThrown()
    }
}
