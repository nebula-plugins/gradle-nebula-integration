
package com.nebula.status

import com.google.common.base.Function
import com.google.common.base.Predicate
import com.google.common.collect.FluentIterable
import nebula.test.IntegrationSpec
import nebula.test.dependencies.DependencyGraphBuilder
import nebula.test.dependencies.GradleDependencyGenerator
import nebula.test.dependencies.ModuleBuilder
import nebula.test.functional.GradleRunner
import org.gradle.internal.classloader.ClasspathUtil
import org.gradle.internal.classpath.ClassPath
import org.gradle.util.TextUtil

class StatusSpec extends IntegrationSpec {
    def setup() {
        def initScript = new File(projectDir, "init.gradle")
        initScript.text = """
            import static com.nebula.status.StatusAttribute.configureStatusAttribute
            import static com.nebula.status.StatusSchemePlugin.defineStatusesWithScheme
            
            initscript {
                dependencies {
                   ${buildDependencies()}
                }
            }
            
            allprojects {
                configureStatusAttribute(it)
                buildscript {
                    defineStatusesWithScheme(dependencies, ['snapshot', 'integration', 'candidate', 'release'])
                }
            } 
        """
        addInitScript(initScript)
    }

    def "first order dependency with candidate status is also transitive dependency with release status "() {
        given:
        def graph = new DependencyGraphBuilder()
                .addModule('test.nebula:a:1.1.0')
                .addModule('test.nebula:a:1.1.1-rc.1')
                .addModule(new ModuleBuilder('test.nebula:b:1.1.0')
                .addDependency('test.nebula:a:1.1.0').build())
                .build()
        def mavenrepo = new GradleDependencyGenerator(graph, "${projectDir}/testrepogen")
        mavenrepo.generateTestMavenRepo()

        buildFile << """  
            buildscript {               
                repositories {
                    ${mavenrepo.mavenRepositoryBlock}
                }
                dependencies {
                    classpath('test.nebula:a:1.1.1-rc.1')
                    classpath('test.nebula:b:1.1.0')
                }
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("buildEnvironment")

        then:
        result.standardOutput.contains("test.nebula:a:1.1.0 -> 1.1.1-rc.1")
    }

    static String buildDependencies() {
        ClassLoader classLoader = StatusSpec.class.getClassLoader()
        def classpathFilter = GradleRunner.CLASSPATH_DEFAULT
        getClasspathAsFiles(classLoader, classpathFilter).collect {
            String.format("      classpath files('%s')\n", TextUtil.escapeString(it.getAbsolutePath()))
        }.join('\n')
    }

    private static List<File> getClasspathAsFiles(ClassLoader classLoader, Predicate<URL> classpathFilter) {
        List<URL> classpathUrls = getClasspathUrls(classLoader)
        return FluentIterable.from(classpathUrls).filter(classpathFilter).transform(new Function<URL, File>() {
            @Override
            File apply(URL url) {
                return new File(url.toURI())
            }
        }).toList()
    }

    private static List<URL> getClasspathUrls(ClassLoader classLoader) {
        Object cp = ClasspathUtil.getClasspath(classLoader)
        if (cp instanceof List<URL>) {
            return (List<URL>) cp
        }
        if (cp instanceof ClassPath) { // introduced by gradle/gradle@0ab8bc2
            return ((ClassPath) cp).asURLs
        }
        throw new IllegalStateException("Unable to extract classpath urls from type ${cp.class.canonicalName}")
    }
}