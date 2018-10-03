import nebula.test.dependencies.maven.ArtifactType
import nebula.test.dependencies.maven.Pom
import org.assertj.core.util.Lists
import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

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
class RequirePreferSpec extends TestKitSpecification {
    String dep = 'acacia'
    String first = 'blue-palo-verde'
    String second = 'coast-redwood'

    String group = 'tree'
    File repo
    def tasks = ['dependencyInsight', '--dependency', "$dep"]


    def setup() {
        repo = new File('repo')
        repo.deleteDir()
        setupDependenciesInLocalRepo()
    }

    @Unroll
    def "preference in ranges - #title"() {
        given:
        addSubproject(first, buildFileWithDependencyVersions(require1, prefer1))
        addSubproject(second, buildFileWithDependencyVersions(require2, prefer2))

        buildFile << simpleParentMultiProjectBuildFile(true)

        when:
        def result = runTasks(*tasks)

        and:
        DocWriter docWriter = new DocWriter(methodName + '-' + title.replaceAll(/\W+/, '-'), projectDir, 'misc')
        writeRelevantOutput(docWriter, result.output, prefer1, prefer2)

        then:
        assertWhenAllDependenciesHaveVersionRanges(docWriter, result, require1, require2, finalVersion)

        where:
        prefer1 | require1     | prefer2 | require2     | finalVersion | title
        '1.5'   | '[1.2, 2.0)' | null    | '[1.2, 2.0)' | '1.5'        | 'one preference in required range'
        '1.5'   | '[1.2, 2.0)' | '1.6'   | '[1.2, 2.0)' | '1.6'        | 'two preferences in required range - higher is chosen'
        '1.5'   | '[1.2, 2.0)' | null    | '[2.0, 3.0)' | '2.9'        | 'conflict resolution - higher range does not have a preference'
        '2.5'   | '[2.0, 3.0)' | null    | '[1.0, 1.2)' | '2.5'        | 'conflict resolution - higher range has a preference'
    }

    @Unroll
    def "preference in range vs static version: #title"() {
        addSubproject(first, buildFileWithDependencyVersions(require1, prefer1))
        addSubproject(second, buildFileWithDependencyVersions(require2, prefer2))

        buildFile << simpleParentMultiProjectBuildFile(true)

        when:
        def result = runTasks(*tasks)

        and:
        DocWriter docWriter = new DocWriter(methodName + '-' + title.replaceAll(/\W+/, '-'), projectDir, 'misc')
        writeRelevantOutput(docWriter, result.output, prefer1, prefer2)

        then:
        assertWhenNotAllDependenciesHaveVersionRanges(docWriter, result, require1, require2, finalVersion)

        where:
        prefer1 | require1     | prefer2 | require2 | finalVersion | title
        '1.5'   | '[1.2, 2.0)' | null    | '2.9'    | '2.9'        | 'static > preference'
        '1.5'   | '[1.4, 2.0)' | null    | '1.2'    | '1.5'        | 'preference > static'
        '1.6'   | '1.0'        | null    | '1.4'    | '1.4'        | 'preference not applied with static dependency'
    }

    @Unroll
    def "dynamic versions do not apply prefer - #title"() {
        addSubproject(first, buildFileWithDependencyVersions(require1, prefer1))
        addSubproject(second, buildFileWithDependencyVersions(require2, prefer2))

        buildFile << simpleParentMultiProjectBuildFile(true)

        when:
        def result = runTasks(*tasks)

        and:
        DocWriter docWriter = new DocWriter(methodName + '-' + title.replaceAll(/\W+/, '-'), projectDir, 'misc')
        writeRelevantOutput(docWriter, result.output, prefer1, prefer2)

        then:
        assertWithDynamicAndStaticAndPreferenceVersions(docWriter, result, require1, require2, finalVersion)

        where:
        prefer1 | require1 | prefer2 | require2 | finalVersion | title
        '1.5'   | '1.+'    | null    | '2.9'    | '2.9'        | 'static > preference'
        '1.5'   | '1.+'    | null    | '1.2'    | '1.9'        | 'preference > static'
    }

    @Unroll
    def "bom versions do not apply prefer - #title"() {
        given:
        addSubprojectUsingABomAndPreference(bomVersion, prefer)

        buildFile << simpleParentMultiProjectBuildFile(false)
        createBom(bomVersion)

        when:
        def result = runTasks(*tasks)

        and:
        DocWriter docWriter = new DocWriter(methodName + '-' + title.replaceAll(/\W+/, '-'), projectDir, 'misc')
        writeRelevantOutput(docWriter, result.output, prefer, null)

        then:
        assertWithBomAndPrefer(docWriter, result, prefer, bomVersion, finalVersion)

        where:
        prefer | bomVersion | finalVersion | title
        null   | '1.6'      | '1.6'        | 'with only bom'
        '1.2'  | '1.6'      | '1.6'        | 'bom > prefer'
        '1.8'  | '1.6'      | '1.6'        | 'prefer > bom'
    }

    private def createBom(String depVersion) {
        repo.mkdirs()

        def localBom = new Pom('sample', 'bom', '1.0.0', ArtifactType.POM)
        localBom.addManagementDependency(group, dep, depVersion)
        ArtifactHelpers.setupSamplePomWith(repo, localBom, localBom.generate())
    }

    private def simpleParentMultiProjectBuildFile(boolean shouldUseFirstProject) {
        def firstProject = ''
        if (shouldUseFirstProject) {
            firstProject = "compile project(':$first')"
        }

        """
apply plugin: 'java'

repositories {
    maven { url { '${repo.absolutePath}' } }
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

    private void setupDependenciesInLocalRepo() {
        repo.mkdirs()
        for (def major in 1..2) {
            for (def minor in 0..9) {
                setupLocalDependency(dep, "$major.$minor", Collections.emptyMap())
            }
        }
    }

    private void setupLocalDependency(String artifactName, String version, Map<String, String> dependencies) {
        def group = 'tree'
        def pom = new Pom(group, artifactName, version)
        for (Map.Entry<String, String> entry : dependencies) {
            pom.addDependency(group, entry.key, entry.value)
        }
        ArtifactHelpers.setupSamplePomWith(repo, pom, pom.generate())
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
    maven { url '${repo.absolutePath}' }
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
