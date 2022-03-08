
package test.suites.gradle.test

import spock.lang.Specification
import spock.lang.TempDir
import org.gradle.testkit.runner.GradleRunner

class TestSuitesGradleTestPluginFunctionalTest extends Specification {
    @TempDir
    private File projectDir

    private getBuildFile() {
        new File(projectDir, "build.gradle")
    }

    private getSettingsFile() {
        new File(projectDir, "settings.gradle")
    }

    def "can run task"() {
        given:
        settingsFile << ""
        buildFile << """
        plugins {
            id 'java'
        }
        
        repositories{
            mavenCentral()
        }
        
        dependencies {
            implementation "com.google.guava:guava:19.0"
        }
        
        testing {
            suites { 
                test { 
                   useJUnitJupiter()
                }
        
                integrationTest(JvmTestSuite) { 
                    dependencies {
                        implementation project 
                    }
        
                    targets { 
                        all {
                            testTask.configure {
                                shouldRunAfter(test)
                            }
                        }
                    }
                }
            }
        }
        
        tasks.named('check') { 
            dependsOn(testing.suites.integrationTest)
        }
"""

        def test = """"\
            package netflix.nebula;
            import org.junit.jupiter.api.Test;
            import com.google.common.collect.ImmutableMap;
            import java.util.Map;
            
            public class HelloWorldTest {
                @Test 
                public void doesSomething() {
                   Map items = ImmutableMap.of("coin", 3, "glass", 4, "pencil", 1);
                   assert true;
                }
            }
            """.stripIndent()

        def testFolder = new File(projectDir, 'src/test/java/netflix/nebula')
        testFolder.mkdirs()
        new File(testFolder, 'HelloWorldTest.java').text = test


        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("build")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.output.contains("Hello from plugin 'test.suites.gradle.test.greeting'")
    }
}
