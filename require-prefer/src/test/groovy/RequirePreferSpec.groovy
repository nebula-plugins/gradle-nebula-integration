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

import nebula.test.dependencies.maven.ArtifactType
import nebula.test.dependencies.maven.Pom
import org.assertj.core.util.Lists
import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

class RequirePreferSpec extends TestKitSpecification {
    static final String dep = 'acacia'
    static final String first = 'blue-palo-verde'
    static final String second = 'coast-redwood'
    static final String publishedVersion = '1.0'
    static final String group = 'tree'

    static File repo
    static File mavenRepo
    def tasks = ['dependencyInsight', '--dependency', "$dep"]

    def setupSpec() {
        repo = new File('repo')
        setupDependenciesInLocalRepo()
    }

    @Unroll
    def "dependency insight #title"() {
        given:
        buildFile << buildFileWithDependencyVersions(require, prefer)

        when:
        def result = runTasks(*tasks)

        and:
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'dependency-insight')
        writeRelevantOutput(docWriter, result.output, prefer, null)

        then:
        def selectedVersion = "$group:$dep:$resolvesTo\n"
        docWriter.addAssertionToDoc("Selected version as: '$selectedVersion'")
        assert result.output.contains(selectedVersion)

        if (prefer != null) {
            def firstReasonResultingVersion = "$prefer preferred"
            docWriter.addAssertionToDoc("Indicates preferred version with '$firstReasonResultingVersion'")
            assert result.output.contains(firstReasonResultingVersion)
        }

        docWriter.writeFooter('completed assertions')

        where:
        prefer | require      | resolvesTo | title
        '1.5'  | '[1.2, 2.0)' | '1.5'      | 'should show info about prefer'
        null   | '[1.2, 2.0)' | '1.9'      | 'without prefer resolves to highest in range'
    }


    @Unroll
    def "prefer #doesWhat when #title"() {
        given:
        addSubproject(first, buildFileWithDependencyVersions(require1, prefer1))
        addSubproject(second, buildFileWithDependencyVersions(require2, prefer2))

        buildFile << simpleParentMultiProjectBuildFile(true)

        when:
        def result = runTasks(*tasks)

        and:
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'multiple-ranges')
        writeRelevantOutput(docWriter, result.output, prefer1, prefer2)

        then:
        assertWhenAllDependenciesHaveVersionRanges(docWriter, result, require1, require2, finalVersion)

        where:
        prefer1 | require1     | prefer2 | require2     | finalVersion | doesWhat       | title
        '1.5'   | '[1.2, 2.0)' | null    | '[1.2, 2.0)' | '1.5'        | 'wins'         | 'one preference is in required range'
        '1.5'   | '[1.2, 2.0)' | '1.6'   | '[1.2, 2.0)' | '1.6'        | 'wins'         | 'two preferences are in required range and the higher is chosen'
        '1.5'   | '[1.2, 2.0)' | null    | '[2.0, 3.0)' | '2.9'        | 'does not win' | 'higher range does not have a preference'
        '2.5'   | '[2.0, 3.0)' | null    | '[1.0, 1.2)' | '2.5'        | 'wins'         | 'higher range has a preference'
    }

    @Unroll
    def "#title when static version conflicts with preference in range"() {
        addSubproject(first, buildFileWithDependencyVersions(require1, prefer1))
        addSubproject(second, buildFileWithDependencyVersions(require2, prefer2))

        buildFile << simpleParentMultiProjectBuildFile(true)

        when:
        def result = runTasks(*tasks)

        and:
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'conflicts')
        writeRelevantOutput(docWriter, result.output, prefer1, prefer2)

        then:
        assertWhenNotAllDependenciesHaveVersionRanges(docWriter, result, require1, require2, finalVersion)

        where:
        prefer1 | require1     | prefer2 | require2 | finalVersion | title
        '1.5'   | '[1.2, 2.0)' | null    | '2.9'    | '2.9'        | 'higher static version wins'
        '1.5'   | '[1.4, 2.0)' | null    | '1.2'    | '1.5'        | 'higher preference version wins'
    }

    @Unroll
    def "preference is not applied when #title"() {
        addSubproject(first, buildFileWithDependencyVersions(require1, prefer1))
        addSubproject(second, buildFileWithDependencyVersions(require2, prefer2))

        buildFile << simpleParentMultiProjectBuildFile(true)

        when:
        def result = runTasks(*tasks)

        and:
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'static')
        writeRelevantOutput(docWriter, result.output, prefer1, prefer2)

        then:
        assertWhenNotAllDependenciesHaveVersionRanges(docWriter, result, require1, require2, finalVersion)

        where:
        prefer1 | require1 | prefer2 | require2 | finalVersion | title
        '1.6'   | '1.0'    | null    | '1.4'    | '1.4'        | 'only static dependencies are listed'
    }

    @Unroll
    def "highest dynamic version wins when #title"() {
        addSubproject(first, buildFileWithDependencyVersions(require1, prefer1))
        addSubproject(second, buildFileWithDependencyVersions(require2, prefer2))

        buildFile << simpleParentMultiProjectBuildFile(true)

        when:
        def result = runTasks(*tasks)

        and:
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'dynamic')
        writeRelevantOutput(docWriter, result.output, prefer1, prefer2)

        then:
        assertWithDynamicAndStaticAndPreferenceVersions(docWriter, result, require1, require2, finalVersion)

        where:
        prefer1 | require1 | prefer2 | require2 | finalVersion | title
        '1.5'   | '1.+'    | null    | '2.9'    | '2.9'        | 'static is higher than preference'
        '1.5'   | '1.+'    | null    | '1.2'    | '1.9'        | 'preference is higher than static'
    }

    @Unroll
    def "bom version wins when #title"() {
        given:
        addSubprojectUsingABomAndPreference(bomVersion, prefer)

        buildFile << simpleParentMultiProjectBuildFile(false)
        createBom(bomVersion)

        when:
        def result = runTasks(*tasks)

        and:
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'bom')
        writeRelevantOutput(docWriter, result.output, prefer, null)

        then:
        assertWithBomAndPrefer(docWriter, result, prefer, bomVersion, finalVersion)

        where:
        prefer | bomVersion | finalVersion | title
        null   | '1.6'      | '1.6'        | 'only bom'
        '1.2'  | '1.6'      | '1.6'        | 'bom is higher than prefer'
        '1.8'  | '1.6'      | '1.6'        | 'prefer is higher than bom'
    }

    @Unroll
    def "#publishingWith #title"() {
        // also noted in https://github.com/gradle/dependency-management-samples/tree/master/samples/strict-dependencies
        // and related to https://github.com/gradle/gradle/issues/6610
        given:
        buildFile << buildFilePublishingWith(publishingWith, false, require, prefer, requireAndPreferInSameDependencyBlock)
        settingsFile << "rootProject.name = '$first'"

        when:
        def insightResult = runTasks('dependencyInsight', '--dependency', dep)
        runTasks('publish')

        and:
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'publishing')
        File publishedMetadata = publishedMetadata(publishingWith)

        def outputToWrite = "Publishing $publishingWith metadata:\n\n${publishedMetadata.text}"
        writeRelevantOutput(docWriter, outputToWrite, prefer, null)

        then:
        !insightResult.output.contains('FAILED')

        docWriter.addAssertionToDoc("$preferedVersionInMetadata: Published $publishingWith metadata contains prefer version '$prefer")
        assert publishedMetadata.text.contains(prefer) == preferedVersionInMetadata

        if (require != null) {
            docWriter.addAssertionToDoc("Published $publishingWith metadata contains range '$require")
            assert publishedMetadata.text.contains(require)
        }

        docWriter.writeFooter('completed assertions')

        where:
        publishingWith | prefer | require      | requireAndPreferInSameDependencyBlock | preferedVersionInMetadata | title
// defined in the same dependency block
        'maven'        | '1.5'  | '[1.2, 2.0)' | true                                  | false                     | 'publishing when in same block contains only range'
        'maven'        | '1.5'  | null         | true                                  | true                      | 'publishing when in same block contains prefer when require is blank'
        'ivy'          | '1.5'  | '[1.2, 2.0)' | true                                  | false                     | 'publishing when in same block contains only range'
        'ivy'          | '1.5'  | null         | true                                  | true                      | 'publishing when in same block contains prefer when require is blank'
// not in the same dependency block
        'maven'        | '1.5'  | '[1.2, 2.0)' | false                                 | true                      | 'publishing when in different blocks contains prefer and range'
        'maven'        | '1.5'  | null         | false                                 | true                      | 'publishing when in different blocks contains only prefer'
        'ivy'          | '1.5'  | '[1.2, 2.0)' | false                                 | true                      | 'publishing when in different blocks contains prefer and range'
        'ivy'          | '1.5'  | null         | false                                 | true                      | 'publishing when in different blocks contains only prefer'
    }

    @Unroll
    def "#publishingWith publishing with gradle metadata #title"() {
        // also noted in https://github.com/gradle/dependency-management-samples/tree/master/samples/strict-dependencies
        // and related to https://github.com/gradle/gradle/issues/6610
        given:
        buildFile << buildFilePublishingWith(publishingWith, true, require, prefer, requireAndPreferInSameDependencyBlock)
        settingsFile << "rootProject.name = '$first'"

        settingsFile << """
            enableFeaturePreview('GRADLE_METADATA')
            enableFeaturePreview('STABLE_PUBLISHING')
            """.stripIndent()

        when:
        def insightResult = runTasks('dependencyInsight', '--dependency', dep)
        runTasks('publish')

        and:
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'publishing-gradle-metadata')
        File publishedMetadata = publishedMetadata(publishingWith)
        File publishedGradleMetadata = publishedGradleMetadata(publishingWith)

        def outputToWrite = "Publishing $publishingWith metadata:\n\n" +
                "${publishedMetadata.text}\n\n" +
                "Publishing Gradle metadata snippet:\n\n" +
                "      dependencies ${publishedGradleMetadata.text.split('dependencies')[1]}"
        writeRelevantOutput(docWriter, outputToWrite, prefer, null)

        then:
        !insightResult.output.contains('FAILED')

        docWriter.addAssertionToDoc("$preferedVersionInMetadata: Published $publishingWith metadata contains prefer version '$prefer")
        assert publishedMetadata.text.contains(prefer) == preferedVersionInMetadata

        docWriter.addAssertionToDoc("$preferedVersionInMetadata: Published Gradle metadata contains prefer version '$prefer")
        assert publishedGradleMetadata.text.contains(prefer) == preferedVersionInGradleMetadata

        if (require != null) {
            docWriter.addAssertionToDoc("Published $publishingWith metadata contains range '$require")
            assert publishedMetadata.text.contains(require)

            docWriter.addAssertionToDoc("Published Gradle metadata contains range '$require")
            assert publishedGradleMetadata.text.contains(require)
        }

        docWriter.writeFooter('completed assertions')

        where:
        publishingWith | prefer | require      | requireAndPreferInSameDependencyBlock | preferedVersionInMetadata | preferedVersionInGradleMetadata | title
// defined in the same dependency block
        'maven'        | '1.5'  | '[1.2, 2.0)' | true                                  | false                     | false                           | 'when in same block contains only range'
        'maven'        | '1.5'  | null         | true                                  | true                      | true                            | 'when in same block contains prefer when require is blank'
        'ivy'          | '1.5'  | '[1.2, 2.0)' | true                                  | false                     | false                           | 'when in same block contains only range'
        'ivy'          | '1.5'  | null         | true                                  | true                      | true                            | 'when in same block contains prefer when require is blank'
// not in the same dependency block
        'maven'        | '1.5'  | '[1.2, 2.0)' | false                                 | true                      | true                            | 'when in different blocks contains prefer and range'
        'maven'        | '1.5'  | null         | false                                 | true                      | true                            | 'when in different blocks contains only prefer'
        'ivy'          | '1.5'  | '[1.2, 2.0)' | false                                 | true                      | true                            | 'when in different blocks contains prefer and range'
        'ivy'          | '1.5'  | null         | false                                 | true                      | true                            | 'when in different blocks contains only prefer'
    }

    @Unroll
    def "consuming published dependencies from #publishingWith with prefer"() {
        given:
        buildFile << buildFilePublishingWith(publishingWith, false, require, prefer, requireAndPreferInSameDependencyBlock)
        settingsFile << "rootProject.name = '$first'"

        when:
        def insightResultForProducer = runTasks('dependencyInsight', '--dependency', dep)
        runTasks('publish')

        then:
        !insightResultForProducer.output.contains('FAILED')

        when:
        buildFile.delete()
        buildFile.createNewFile()
        buildFile <<
                """
plugins {
    id 'java-library'
}

group '$group'
version '1.0'

dependencies {
    api '$group:$first:$publishedVersion'
}

repositories {
    maven { url '${mavenRepo.absolutePath}' }
    maven { url { 'maven-repo' } }
    ivy { url { 'ivy-repo' } }
}
"""
        settingsFile.delete()
        settingsFile.createNewFile()
        settingsFile << "rootProject.name = '$second'"

        def insightResultForConsumer = runTasks('dependencyInsight', '--dependency', dep)
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'consuming')

        def outputToWrite = "Dependency insight:\n\n${insightResultForConsumer.output}"
        writeRelevantOutput(docWriter, outputToWrite, prefer, null)

        File publishedMetadata = publishedMetadata(publishingWith)

        then:
        !insightResultForConsumer.output.contains('FAILED')

        docWriter.addAssertionToDoc("Published first order dependency $publishingWith metadata contains prefer version '$prefer'")
        assert publishedMetadata.text.contains(prefer)

        docWriter.addAssertionToDoc("Transitive dependency version resolves to preferred version in first order dependency: '$prefer'")
        assert insightResultForConsumer.output.contains(prefer)

        docWriter.writeFooter('completed assertions')

        where:
        publishingWith | prefer | require      | requireAndPreferInSameDependencyBlock
        'maven'        | '1.5'  | '[1.2, 2.0)' | false
        'ivy'          | '1.5'  | '[1.2, 2.0)' | false
    }

    private static def createBom(String depVersion) {
        repo.mkdirs()

        def localBom = new Pom('sample', 'bom', '1.0.0', ArtifactType.POM)
        localBom.addManagementDependency(group, dep, depVersion)
        ArtifactHelpers.setupSamplePomWith(mavenRepo, localBom, localBom.generate())
    }

    private static def simpleParentMultiProjectBuildFile(boolean shouldUseFirstProject) {
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

    private static def buildFileWithDependencyVersions(String requireVersion, String preferVersion) {
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

    private def buildFilePublishingWith(String publishingWith, boolean enableGradleMetadata, String require1, String prefer1, boolean sameDependencyBlock) {
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
        maven { url '${projectDir.absolutePath + File.separator + 'maven-repo'}' }
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
        ivy { url '${projectDir.absolutePath + File.separator + 'ivy-repo'}' }
    }
    publications {
        ivy(IvyPublication) {
            from ${publicationsFrom}
        }
    }
}
"""
    }

    private static void setupDependenciesInLocalRepo() {
        repo.mkdirs()
        mavenRepo = new File(repo, 'maven-repo')
        mavenRepo.mkdirs()

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

    private def writeRelevantOutput(DocWriter docWriter, String output, String prefer1, String prefer2) {
        docWriter.writeProjectFiles()
        docWriter.writeGradleVersion(project.gradle.gradleVersion)
        docWriter.writeCleanedUpBuildOutput(
                "Tasks: ${String.join(' ', tasks)}\n\n" +
                        "Scenario: ${testName.methodName}\n" +
                        "Preferred version(s): $prefer1, $prefer2\n\n" +
                        "${output}\n\n")
    }

    private def addSubprojectUsingABomAndPreference(String bomVersion, String prefer) {
        def preferInSubproject = ''
        if (prefer != '') {
            preferInSubproject = "prefer '$bomVersion'"
        }
        addSubproject(second, """
plugins {
    id 'java-library'
}

group '$group'
version '1.0'

dependencies {
    api platform('sample:bom:1.0.0')
    api '$group:$dep'
    api ('$group:$dep') {
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

    private File publishedGradleMetadata(String publishingWith) {
        if (publishingWith == 'ivy') {
            return new File(ivyArtifactPath(), "${first}-${publishedVersion}.module")
        }
        return new File(mavenArtifactPath(), "${first}-${publishedVersion}.module")
    }

    private File publishedMetadata(String publishingWith) {
        if (publishingWith == 'ivy') {
            return new File(ivyArtifactPath(), 'ivy' + '-' + publishedVersion + '.xml')
        }
        return new File(mavenArtifactPath(), first + '-' + publishedVersion + '.pom')
    }

    private String ivyArtifactPath() {
        new File(projectDir, 'ivy-repo' + File.separator +
                group + File.separator +
                first + File.separator +
                publishedVersion + File.separator)
    }

    private String mavenArtifactPath() {
        new File(projectDir, 'maven-repo' + File.separator +
                group + File.separator +
                first + File.separator +
                publishedVersion + File.separator)
    }

    private static void assertWhenAllDependenciesHaveVersionRanges(DocWriter docWriter, BuildResult result, String require1, String require2, String finalVersion) {
        def firstReasonResultingVersion = "$group:$dep:$require1 -> $finalVersion"
        docWriter.addAssertionToDoc("First dep resulting version: '$firstReasonResultingVersion'")
        assert result.output.contains(firstReasonResultingVersion)

        def secondReasonResultingVersion = "$group:$dep:$require2 -> $finalVersion"
        docWriter.addAssertionToDoc("Second dep resulting version: '$secondReasonResultingVersion'")
        assert result.output.contains(secondReasonResultingVersion)

        docWriter.writeFooter('completed assertions')
    }

    private static void assertWhenNotAllDependenciesHaveVersionRanges(DocWriter docWriter, BuildResult result, String require1, String require2, String finalVersion) {
        def losingVersion = Lists.newArrayList(require1, require2)
        losingVersion.remove(finalVersion)

        def winningReasonResultingVersion = "$group:$dep:${finalVersion}"
        docWriter.addAssertionToDoc("Winning dep resulting version: '$winningReasonResultingVersion'")
        assert result.output.contains(winningReasonResultingVersion)

        def losingReasonResultingVersion = "$group:$dep:${losingVersion.first()} -> ${finalVersion}"
        docWriter.addAssertionToDoc("Losing dep resulting version: '$losingReasonResultingVersion'")
        assert result.output.contains(losingReasonResultingVersion)

        docWriter.writeFooter('completed assertions')
    }

    private static void assertWithBomAndPrefer(DocWriter docWriter, BuildResult result, String prefer, String bomVersion, String finalVersion) {
        def winningReasonResultingVersion = "$group:$dep:${finalVersion}"
        docWriter.addAssertionToDoc("Winning dep resulting version: '$winningReasonResultingVersion'")
        assert result.output.contains(winningReasonResultingVersion)

        def losingReasonResultingVersion = "$group:$dep -> $finalVersion"
        docWriter.addAssertionToDoc("Losing dep resulting version: '$losingReasonResultingVersion'")
        assert result.output.contains(losingReasonResultingVersion)

        docWriter.addAssertionToDoc("Ensure 'bomVersion == finalVersion': ${bomVersion == finalVersion}")
        assert bomVersion == finalVersion

        def bomIsApplied = '--- sample:bom:1.0.0'
        docWriter.addAssertionToDoc("Contains '$bomIsApplied'")
        assert result.output.contains(bomIsApplied)

        docWriter.writeFooter('completed assertions')
    }

    private static void assertWithDynamicAndStaticAndPreferenceVersions(DocWriter docWriter, BuildResult result, String require1, String require2, String finalVersion) {
        def winningReasonResultingVersion = "$group:$dep:${finalVersion}"
        docWriter.addAssertionToDoc("Winning dep resulting version: '$winningReasonResultingVersion'")
        assert result.output.contains(winningReasonResultingVersion)

        if (require1 != finalVersion) {
            def firstReasonResultingVersion = "$group:$dep:$require1 -> $finalVersion"
            docWriter.addAssertionToDoc("First dep resulting version: '$firstReasonResultingVersion'")
            assert result.output.contains(firstReasonResultingVersion)
        }

        if (require2 != finalVersion) {
            def secondReasonResultingVersion = "$group:$dep:$require2 -> $finalVersion"
            docWriter.addAssertionToDoc("Second dep resulting version: '$secondReasonResultingVersion'")
            assert result.output.contains(secondReasonResultingVersion)
        }

        docWriter.writeFooter('completed assertions')
    }

}
