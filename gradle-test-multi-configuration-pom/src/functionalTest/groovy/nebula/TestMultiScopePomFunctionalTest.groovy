package nebula

import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll

class TestMultiScopePomFunctionalTest extends Specification {
    @Unroll
    def "guava is available in #configuration"() {
        given:
        def projectDir = new File("build/functionalTest")
        projectDir.mkdirs()
        File mavenRepo = new File(getClass().getResource('/mavenRepo').toURI())
        new File(projectDir, "settings.gradle") << ""
        new File(projectDir, "build.gradle").text = """
            plugins {
                id 'java'
            }
            
            repositories {
                maven {
                    url = '${mavenRepo.absolutePath}'
                }
            }

            dependencies {
                implementation 'netflix:mymodule:1.0.0'
            }
        """

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withArguments("dI", '--dependency', 'mymodule2', '--configuration', configuration)
        runner.withProjectDir(projectDir)
        def resultRuntimeClasspath = runner.build()

        then:
        resultRuntimeClasspath.output.contains("netflix:mymodule2:1.0.0")

        where:
        configuration << ['runtimeClasspath', 'compileClasspath']
    }
}

