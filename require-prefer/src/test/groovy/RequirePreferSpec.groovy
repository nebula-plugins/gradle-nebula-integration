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

    String group = 'tree'
    static File repo
    static File mavenRepo
    def tasks = ['dependencyInsight', '--dependency', "$dep"]

    def setupSpec() {
        repo = new File('repo')
        repo.deleteDir()
        setupDependenciesInLocalRepo()
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
    def "#title with ivy"() {
        // also noted in https://github.com/gradle/dependency-management-samples/tree/master/samples/strict-dependencies
        given:
        buildFile << buildFilePublishingToIvy(require, prefer)

        when:
        runTasks('publish')

        and:
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'publishing')
        def publishedPom = new File(repo, 'ivy' + File.separator +
                group + File.separator +
                first + File.separator +
                publishedVersion + File.separator +
                'ivy' + '-' + publishedVersion + '.xml')
        writeRelevantOutput(docWriter, "Publishing to 'repo' with metadata:\n\n${publishedPom.text}", prefer, null)

        then:
        docWriter.addAssertionToDoc("Published metadata does not contain prefer version '$prefer")
        !publishedPom.text.contains(prefer)

        docWriter.writeFooter('completed assertions')

        where:
        prefer | require      | preferInMetadata | title
        '1.5'  | '[1.2, 2.0)' | false            | 'prefer does not live through publishing'
    }

    @Unroll
    def "#title with maven"() {
        // also noted in https://github.com/gradle/dependency-management-samples/tree/master/samples/strict-dependencies
        given:
        buildFile << buildFilePublishingToMaven(require, prefer)

        when:
        runTasks('publish')

        and:
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'publishing')
        def publishedPom = new File(mavenRepo, group + File.separator +
                first + File.separator +
                publishedVersion + File.separator +
                first + '-' + publishedVersion + '.pom')

        writeRelevantOutput(docWriter, "Publishing to 'repo' with metadata:\n\n${publishedPom.text}", prefer, null)

        then:
        docWriter.addAssertionToDoc("Published metadata does not contain prefer version '$prefer")
        !publishedPom.text.contains(prefer)

        docWriter.writeFooter('completed assertions')

        where:
        prefer | require      | preferInMetadata | title
        '1.5'  | '[1.2, 2.0)' | false            | 'prefer does not live through publishing'
    }

    private def createBom(String depVersion) {
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

    private def buildFileWithDependencyVersions(String requireVersion, String preferVersion) {
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
    jcenter()
}
"""
    }

    private def buildFilePublishingToMaven(String require1, String prefer1) {
        """
plugins {
    id 'java-library'
    id 'maven-publish'
}

group '$group'
version '1.0'

dependencies {
    api ('$group:$dep') {
        version {
            require '${require1}'
            prefer '${prefer1}'
        }
    }
}

repositories {
    jcenter()
}

publishing {
    repositories {
        maven { url '${mavenRepo.absolutePath}' }
    }
    publications {
        ivy(MavenPublication) {
            groupId = '$group'
            artifactId = '$first'
            version = '1.0'

            from components.java
        }
    }
}
"""
    }

    private def buildFilePublishingToIvy(String require1, String prefer1) {
        """
plugins {
    id 'java-library'
    id 'ivy-publish'
}

group '$group'
version '1.0'

dependencies {
    api ('$group:$dep') {
        version {
            require '${require1}'
            prefer '${prefer1}'
        }
    }
}

repositories {
    jcenter()
}

publishing {
    repositories {
        ivy { url '${repo.absolutePath + File.separator + 'ivy'}' }
    }
    publications {
        ivy(IvyPublication) {
            organisation = '$group'
            module = '${first}'
            revision = '1.0'
            descriptor.status = 'snapshot'
            descriptor.branch = 'master'

            from components.java
        }
    }
}
"""
    }


    private static void setupDependenciesInLocalRepo() {
        repo.mkdirs()
        mavenRepo = new File(repo, 'maven')
        mavenRepo.mkdirs()

        for (def major in 1..2) {
            for (def minor in 0..9) {
                setupLocalDependency(dep, "$major.$minor", Collections.emptyMap())
            }
        }
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
    jcenter()
    maven { url '${mavenRepo.absolutePath}' }
}
""")
    }

    private void assertWhenAllDependenciesHaveVersionRanges(DocWriter docWriter, BuildResult result, String require1, String require2, String finalVersion) {
        def firstReasonResultingVersion = "$group:$dep:$require1 -> $finalVersion"
        docWriter.addAssertionToDoc("First dep resulting version: '$firstReasonResultingVersion'")
        assert result.output.contains(firstReasonResultingVersion)

        def secondReasonResultingVersion = "$group:$dep:$require2 -> $finalVersion"
        docWriter.addAssertionToDoc("Second dep resulting version: '$secondReasonResultingVersion'")
        assert result.output.contains(secondReasonResultingVersion)

        docWriter.writeFooter('completed assertions')
    }

    private void assertWhenNotAllDependenciesHaveVersionRanges(DocWriter docWriter, BuildResult result, String require1, String require2, String finalVersion) {
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

    private void assertWithBomAndPrefer(DocWriter docWriter, BuildResult result, String prefer, String bomVersion, String finalVersion) {
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

    private void assertWithDynamicAndStaticAndPreferenceVersions(DocWriter docWriter, BuildResult result, String require1, String require2, String finalVersion) {
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
