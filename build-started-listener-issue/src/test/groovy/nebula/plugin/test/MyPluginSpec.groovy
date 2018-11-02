package nebula.plugin.test

import nebula.test.IntegrationSpec

class MyPluginSpec extends IntegrationSpec {

    def 'buildStarted should be triggered first'() {
        given:
        buildFile << """
            apply plugin: 'java'
            apply plugin: ${MyPlugin.name}

            repositories {
               mavenCentral()
            }

            dependencies {
               testCompile 'junit:junit:4.11'
            }
        """

        createFile('src/test/java/Test.java') << """\
            public class Test {
                @org.junit.Test
                public void iAmHeard() {
                    System.out.println("I want to be heard");
                }
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully('test')

        then:
        result.standardOutput.contains('I want to be heard')
    }
}
