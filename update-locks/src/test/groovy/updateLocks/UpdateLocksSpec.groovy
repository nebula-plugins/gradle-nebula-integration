package updateLocks

import nebula.test.IntegrationSpec
import spock.lang.Issue

class UpdateLocksSpec extends IntegrationSpec {

    public static final String GUAVA_FROM_BINTRAY_730 = 'com.google.guava:guava:27.0.1-jre'
    public static final String GUAVA_FROM_BINTRAY_741 = 'com.google.guava:guava:23.1-jre'

    File dependencyLockFolder

    def setup() {
        buildFile << """\
            apply plugin: 'java'
            dependencyLocking {
                lockAllConfigurations()
            }
            repositories {
                mavenCentral()
                jcenter()
            }
            dependencies {
                compileOnly 'com.netflix.nebula:nebula-bintray-plugin:7.3.0'
                testCompile 'com.netflix.nebula:nebula-bintray-plugin:7.3.0'
            }
            """.stripIndent()
        dependencyLockFolder = new File(projectDir, 'gradle/dependency-locks')
    }

    def 'expected: update locks should update all transitive dependencies'() {
        when:
        runTasks('dependencies', '--write-locks')

        then:
        def compileOnlyLockfile = new File(dependencyLockFolder, 'compileOnly.lockfile')
        compileOnlyLockfile.exists()
        compileOnlyLockfile.text.contains(GUAVA_FROM_BINTRAY_730)

        when:
        buildFile.delete()
        buildFile.createNewFile()
        buildFile << updatedBuildFile()

        runTasks('dependencies', '--update-locks', 'com.netflix.nebula:nebula-bintray-plugin')

        then:
        def updatedCompileOnlyLockfile = new File(dependencyLockFolder, 'compileOnly.lockfile')
        updatedCompileOnlyLockfile.exists()
        updatedCompileOnlyLockfile.text.contains(GUAVA_FROM_BINTRAY_741)
    }

    @Issue("this issue at hand")
    def 'actual: update locks is not currently updating all transitive dependencies'() {
        when:
        runTasks('dependencies', '--write-locks')

        then:
        def compileOnlyLockfile = new File(dependencyLockFolder, 'compileOnly.lockfile')
        compileOnlyLockfile.exists()
        compileOnlyLockfile.text.contains(GUAVA_FROM_BINTRAY_730)

        when:
        buildFile.delete()
        buildFile.createNewFile()
        buildFile << updatedBuildFile()

        runTasks('dependencies', '--update-locks', 'com.netflix.nebula:nebula-bintray-plugin')

        then:
        def updatedCompileOnlyLockfile = new File(dependencyLockFolder, 'compileOnly.lockfile')
        updatedCompileOnlyLockfile.exists()
        updatedCompileOnlyLockfile.text.contains(GUAVA_FROM_BINTRAY_730)
    }

    private static String updatedBuildFile() {
        """\
            apply plugin: 'java'
            dependencyLocking {
                lockAllConfigurations()
            }
            dependencies {
                compileOnly 'com.netflix.nebula:nebula-bintray-plugin:7.4.1'
                testCompile 'com.netflix.nebula:nebula-bintray-plugin:7.4.1'
            }
            """.stripIndent()
    }
}
