/**
 *
 *  Copyright 2018 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
import nebula.test.dependencies.maven.Pom

class AbstractRequirePreferSpec extends TestKitSpecification {
    static final String dep = 'acacia'
    static final String first = 'blue-palo-verde'
    static final String second = 'coast-redwood'
    static final String publishedVersion = '1.0'
    static final String group = 'tree'

    static File repo
    static File mavenRepo
    static File ivyRepo
    def tasks = ['dependencyInsight', '--dependency', "$dep"]

    def setupSpec() {
        repo = new File('repo')
        repo.mkdirs()
        mavenRepo = new File(repo, 'maven-repo')
        mavenRepo.mkdirs()
        ivyRepo = new File(repo, 'ivy-repo')
        ivyRepo.mkdirs()
    }

    static def simpleParentMultiProjectBuildFile(boolean shouldUseFirstProject) {
        def firstProject = ''
        if (shouldUseFirstProject) {
            firstProject = "compile project(':$first')"
        }

        """
apply plugin: 'java'

repositories {
    maven { url { '${mavenRepo.absolutePath}' } }
}

dependencies {
    $firstProject
    compile project(':$second')
}"""
    }

    static def buildFileWithDependencyVersions(String requireVersion, String preferVersion) {
        assert requireVersion != null

        def preferConfiguration = ''
        if (preferVersion != null) {
            preferConfiguration = "prefer '$preferVersion'"
        }

        """
plugins {
    id 'java-library'
}

group '$group'
version '1.0'

dependencies {
    api ('$group:$dep') {
        version {
            require '$requireVersion'
            $preferConfiguration
        }
    }
}

repositories {
    maven { url { '${mavenRepo.absolutePath}' } }
}
"""
    }

    def buildFilePublishingWith(String publishingWith, boolean enableGradleMetadata, String require1, String prefer1, boolean sameDependencyBlock) {
        def compConfig = ''
        if (enableGradleMetadata) {
            compConfig = """
// TODO - use public APIs when available
class TestComponent implements org.gradle.api.internal.component.SoftwareComponentInternal, ComponentWithVariants {
    String name
    Set usages = []
    Set variants = []
}

class TestUsage implements org.gradle.api.internal.component.UsageContext {
    String name
    Usage usage
    Set dependencies = []
    Set dependencyConstraints = []
    Set artifacts = []
    Set capabilities = []
    Set globalExcludes = []
    AttributeContainer attributes
}

class TestVariant implements org.gradle.api.internal.component.SoftwareComponentInternal {
    String name
    Set usages = []
}

class TestCapability implements Capability {
    String group
    String name
    String version
}

def comp = new TestComponent()
comp.usages.add(new TestUsage(
                    name: 'api',
                    usage: objects.named(Usage, 'api'),
                    dependencies: configurations.implementation.allDependencies,
                    attributes: configurations.implementation.attributes))
"""
        }

        def publicationsFrom = enableGradleMetadata ? 'comp' : 'components.java'

        if (publishingWith == 'ivy') {
            return buildFilePublishingWithIvy(compConfig, publicationsFrom, require1, prefer1, sameDependencyBlock)
        }
        return buildFilePublishingWithMaven(compConfig, publicationsFrom, require1, prefer1, sameDependencyBlock)
    }

    private def buildFilePublishingWithMaven(String compConfig, String publicationsFrom, String require1, String prefer1, boolean sameDependencyBlock) {
        def requireBlock = ''
        def requireStatement = ''
        if (require1 != null) {
            if (sameDependencyBlock) {
                requireStatement = "\n            require '$require1'"
            } else {
                requireBlock = """
    api ('$group:$dep') {
        version {
            require '${require1}'
        }
    }"""
            }
        }

        """
plugins {
    id 'java-library'
    id 'maven-publish'
}

group '$group'
version '$publishedVersion'
$compConfig

dependencies {
    api ('$group:$dep') {
        version {
            prefer '${prefer1}'$requireStatement
        }
    }
    $requireBlock
}

repositories {
    maven { url { '${mavenRepo.absolutePath}' } }
}

publishing {
    repositories {
        maven { url { '${projectDir.absolutePath + File.separator + 'maven-repo'}' } }
    }
    publications {
        maven(MavenPublication) {
            groupId = '$group'
            artifactId = '$first'
            version = '1.0'

            from ${publicationsFrom}
        }
    }
}
"""
    }

    private def buildFilePublishingWithIvy(String compConfig, String publicationsFrom, String require1, String prefer1, boolean sameDependencyBlock) {
        def requireBlock = ''
        def requireStatement = ''
        if (require1 != null) {
            if (sameDependencyBlock) {
                requireStatement = "\n            require '$require1'"
            } else {
                requireBlock = """
    api ('$group:$dep') {
        version {
            require '${require1}'
        }
    }"""
            }
        }

        """
plugins {
    id 'java-library'
    id 'ivy-publish'
}

allprojects {
    configurations { implementation }
}

group = '$group'
version = '$publishedVersion'
$compConfig

dependencies {
    api ("$group:$dep") {
        version {
            prefer '$prefer1'$requireStatement
        }
    }
    $requireBlock
}

repositories {
    maven { url { '${mavenRepo.absolutePath}' } }
}

publishing {
    repositories {
        ivy { url { '${projectDir.absolutePath + File.separator + 'ivy-repo'}' } }
    }
    publications {
        ivy(IvyPublication) {
            from ${publicationsFrom}
        }
    }
}
"""
    }

    static void setupDependenciesInLocalRepo() {
        for (def major in 1..2) {
            for (def minor in 0..9) {
                setupLocalDependency(dep, "$major.$minor", Collections.emptyMap())
            }
        }
        def mavenMetadata = new File(mavenRepo, group + File.separator + dep + File.separator + 'maven-metadata.xml')
        mavenMetadata.mkdirs()
        mavenMetadata.delete()

        def versions = ''
        for (def major in 1..2) {
            for (def minor in 0..9) {
                versions += "\n            <version>${major}.${minor}</version>"
            }
        }

        mavenMetadata << """<?xml version="1.0" encoding="UTF-8"?>
<metadata>
    <groupId>tree</groupId>
    <artifactId>blue-palo-verde</artifactId>
    <versioning>
        <release>1.0</release>
        <versions>$versions
        </versions>
        <lastUpdated>20181005164307</lastUpdated>
    </versioning>
</metadata>
"""
    }

    private static void setupLocalDependency(String artifactName, String version, Map<String, String> dependencies) {
        def group = 'tree'
        def pom = new Pom(group, artifactName, version)
        for (Map.Entry<String, String> entry : dependencies) {
            pom.addDependency(group, entry.key, entry.value)
        }
        ArtifactHelpers.setupSamplePomWith(mavenRepo, pom, pom.generate())
    }

    def writeRelevantOutput(DocWriter docWriter, String output, String prefer1, String prefer2) {
        docWriter.writeProjectFiles()
        docWriter.writeGradleVersion(project.gradle.gradleVersion)
        docWriter.writeCleanedUpBuildOutput(
                "Tasks: ${String.join(' ', tasks)}\n\n" +
                        "Scenario: ${testName.methodName}\n" +
                        "Preferred version(s): $prefer1, $prefer2\n\n" +
                        "${output}\n\n")
    }

    def addSubprojectUsingAPreference(String prefer) {
        def preferInSubproject = ''
        if (prefer != '' && prefer != null) {
            preferInSubproject = "prefer '$prefer'"
        }
        addSubproject(second, """
plugins {
    id 'java-library'
}

group '$group'
version '1.0'

dependencies {
    api platform('sample:bom:1.0.0')
    api '$group:$dep' // version from bom
    api ('$group:$dep') { // preferred version
        version {
            $preferInSubproject
        }
    }
}

repositories {
    maven { url '${mavenRepo.absolutePath}' }
}
""")
    }

    File publishedGradleMetadata(File dir, String publishingWith, String dependency, String version) {
        if (publishingWith == 'ivy') {
            return new File(ivyArtifactPath(dir, dependency, version), "${dependency}-${version}.module")
        }
        return new File(mavenArtifactPath(dir, dependency, version), "${dependency}-${version}.module")
    }

    File publishedIvyOrMavenMetadata(File dir, String publishingWith, String dependency, String version) {
        if (publishingWith == 'ivy') {
            return new File(ivyArtifactPath(dir, dependency, version), 'ivy' + '-' + version + '.xml')
        }
        return new File(mavenArtifactPath(dir, dependency, version), dependency + '-' + version + '.pom')
    }

    private static String ivyArtifactPath(File dir, String dependency, String version) {
        new File(dir, 'ivy-repo' + File.separator +
                group + File.separator +
                dependency + File.separator +
                version + File.separator)
    }

    private static String mavenArtifactPath(File dir, String dependency, String version) {
        new File(dir, 'maven-repo' + File.separator +
                group + File.separator +
                dependency + File.separator +
                version + File.separator)
    }
}
