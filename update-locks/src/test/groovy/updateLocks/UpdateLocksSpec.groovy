package updateLocks

import nebula.test.IntegrationSpec
import spock.lang.Issue
import spock.lang.Unroll

class UpdateLocksSpec extends IntegrationSpec {

    public static final String GUAVA_VERSION_FROM_BINTRAY_730 = '27.0.1-jre'
    public static final String GUAVA_VERSION_FROM_BINTRAY_741 = '23.1-jre'
    public static final String NEBULA_LOCKS = 'nebula locks'
    public static final String CORE_LOCKS = 'core locks'
    public static final String NEBULA_BINTRAY_VERSION_730 = "7.3.0"
    public static final String NEBULA_BINTRAY_VERSION_741 = "7.4.1"

    File dependencyLockFolder

    def setup() {
        dependencyLockFolder = new File(projectDir, 'gradle/dependency-locks')
    }

    @Unroll
    def 'expected: update locks should update all transitive dependencies when using #lockingStyle'() {
        given:
        buildFile << createBuildFile(lockingStyle)

        when:
        if(lockingStyle == CORE_LOCKS) {
            runTasks('dependencies', '--write-locks')
        } else {
            runTasks('generateLock', 'saveLock')
        }

        then:
        def lockfile = lockingStyle == CORE_LOCKS
                ? new File(dependencyLockFolder, 'compileOnly.lockfile')
                : new File(projectDir, 'dependencies.lock')
        lockfile.exists()
        lockfile.text.contains(NEBULA_BINTRAY_VERSION_730)
        lockfile.text.contains(GUAVA_VERSION_FROM_BINTRAY_730)

        when:
        buildFile.delete()
        buildFile.createNewFile()
        buildFile << updatedBuildFile(lockingStyle)

        if(lockingStyle == CORE_LOCKS) {
            runTasks('dependencies', '--update-locks', 'com.netflix.nebula:nebula-bintray-plugin')
        } else {
            runTasks('updateLock', '-PdependencyLock.updateDependencies=com.netflix.nebula:nebula-bintray-plugin:7.4.1', 'saveLock')
        }

        then:
        def updatedLockfile = lockingStyle == CORE_LOCKS
            ? new File(dependencyLockFolder, 'compileOnly.lockfile')
            : new File(projectDir, 'dependencies.lock')
        updatedLockfile.exists()
        updatedLockfile.text.contains(NEBULA_BINTRAY_VERSION_741)
        updatedLockfile.text.contains(GUAVA_VERSION_FROM_BINTRAY_741)

        where:
        lockingStyle << [NEBULA_LOCKS, CORE_LOCKS]
    }

    @Unroll
    @Issue("this issue at hand")
    def 'actual: update locks is not currently updating all transitive dependencies when using #lockingStyle'() {
        given:
        buildFile << createBuildFile(lockingStyle)

        when:
        if(lockingStyle == CORE_LOCKS) {
            runTasks('dependencies', '--write-locks')
        } else {
            runTasks('generateLock', 'saveLock')
        }

        then:
        def lockfile = lockingStyle == CORE_LOCKS
                ? new File(dependencyLockFolder, 'compileOnly.lockfile')
                : new File(projectDir, 'dependencies.lock')
        lockfile.exists()
        lockfile.text.contains(NEBULA_BINTRAY_VERSION_730)
        lockfile.text.contains(GUAVA_VERSION_FROM_BINTRAY_730)

        when:
        buildFile.delete()
        buildFile.createNewFile()
        buildFile << updatedBuildFile(lockingStyle)

        if(lockingStyle == CORE_LOCKS) {
            runTasks('dependencies', '--update-locks', 'com.netflix.nebula:nebula-bintray-plugin')
        } else {
            runTasks('updateLock', '-PdependencyLock.updateDependencies=com.netflix.nebula:nebula-bintray-plugin:7.4.1', 'saveLock')
        }

        then:
        def updatedLockfile = lockingStyle == CORE_LOCKS
                ? new File(dependencyLockFolder, 'compileOnly.lockfile')
                : new File(projectDir, 'dependencies.lock')
        updatedLockfile.exists()
        updatedLockfile.text.contains(NEBULA_BINTRAY_VERSION_741) // note the mismatch
        updatedLockfile.text.contains(GUAVA_VERSION_FROM_BINTRAY_730) // note the mismatch

        when:
        if(lockingStyle == CORE_LOCKS) {
            runTasks('dependencies', '--write-locks')
        } else {
            runTasks('generateLock', 'saveLock')
        }

        then:
        def rewrittenLockfile = lockingStyle == CORE_LOCKS
                ? new File(dependencyLockFolder, 'compileOnly.lockfile')
                : new File(projectDir, 'dependencies.lock')
        rewrittenLockfile.exists()
        rewrittenLockfile.text.contains(NEBULA_BINTRAY_VERSION_741)
        rewrittenLockfile.text.contains(GUAVA_VERSION_FROM_BINTRAY_741)

        where:
        lockingStyle << [CORE_LOCKS, NEBULA_LOCKS]
    }

    String createBuildFile(String lockingStyle) {
        if(lockingStyle == CORE_LOCKS) {
            return """\
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
        } else {
            return """\
            buildscript {
              repositories {
                maven {
                  url "https://plugins.gradle.org/m2/"
                }
              }
              dependencies {
                classpath "com.netflix.nebula:gradle-dependency-lock-plugin:8.0.0"
              }
            }
            apply plugin: 'java'
            apply plugin: 'nebula.dependency-lock'
            repositories {
                mavenCentral()
                jcenter()
            }
            dependencies {
                compileOnly 'com.netflix.nebula:nebula-bintray-plugin:7.3.0'
                testCompile 'com.netflix.nebula:nebula-bintray-plugin:7.3.0'
            }
            dependencyLock {
              includeTransitives = true
            }
            """.stripIndent()
        }
    }

    private static String updatedBuildFile(String lockingStyle) {
        if(lockingStyle == CORE_LOCKS) {
            return """\
            apply plugin: 'java'
            dependencyLocking {
                lockAllConfigurations()
            }
            repositories {
                mavenCentral()
                jcenter()
            }
            dependencies {
                compileOnly 'com.netflix.nebula:nebula-bintray-plugin:7.4.1'
                testCompile 'com.netflix.nebula:nebula-bintray-plugin:7.4.1'
            }
            """.stripIndent()
        } else {
            return """\
            buildscript {
              repositories {
                maven {
                  url "https://plugins.gradle.org/m2/"
                }
              }
              dependencies {
                classpath "com.netflix.nebula:gradle-dependency-lock-plugin:8.0.0"
              }
            }
            apply plugin: 'java'
            apply plugin: 'nebula.dependency-lock'
            repositories {
                mavenCentral()
                jcenter()
            }
            dependencies {
                compileOnly 'com.netflix.nebula:nebula-bintray-plugin:7.4.1'
                testCompile 'com.netflix.nebula:nebula-bintray-plugin:7.4.1'
            }
            dependencyLock {
              includeTransitives = true
            }
            """.stripIndent()
        }
    }
}
