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

class RequirePreferSpec extends AbstractRequirePreferSpec {
    def setupSpec() {
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
            def firstReasonResultingVersion = requestedFrom(require, prefer)
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
        def requested1 = requestedFrom(require1, prefer1)
        def requested2 = requestedFrom(require2, prefer2)
        assertWhenAllDependenciesHaveVersionRanges(docWriter, result, requested1, requested2, finalVersion)

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
        assertWhenNotAllDependenciesHaveVersionRanges(docWriter, result, require1, require2, finalVersion, prefer1)

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
        assertWhenNotAllDependenciesHaveVersionRanges(docWriter, result, require1, require2, finalVersion, prefer1)

        where:
        prefer1 | require1 | prefer2 | require2 | finalVersion | title
        '1.6'   | '1.0'    | null    | '1.4'    | '1.4'        | 'only static dependencies are listed'
    }

    @Unroll
    def "#title1 when #title2"() {
        addSubproject(first, buildFileWithDependencyVersions(require1, prefer1))
        addSubproject(second, buildFileWithDependencyVersions(require2, prefer2))

        buildFile << simpleParentMultiProjectBuildFile(true)

        when:
        def result = runTasks(*tasks)

        and:
        DocWriter docWriter = new DocWriter(methodName, projectDir, 'dynamic')
        writeRelevantOutput(docWriter, result.output, prefer1, prefer2)

        then:
        def requested1 = requestedFrom(require1, prefer1)
        def requested2 = requestedFrom(require2, prefer2)
        assertWithDynamicAndStaticAndPreferenceVersions(docWriter, result, requested1, requested2, finalVersion)

        where:
        prefer1 | require1 | prefer2 | require2 | finalVersion | title1                         | title2
        '1.5'   | '1.+'    | null    | '2.9'    | '2.9'        | 'static version wins'          | 'static is higher than preference'
        '1.5'   | '1.+'    | null    | '1.2'    | '1.9'        | 'highest dynamic version wins' | 'preference is higher than static'
    }

    @Unroll
    def "bom version wins when #title"() {
        given:
        addSubprojectUsingAPreference(prefer)

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
        File publishedMetadata = publishedIvyOrMavenMetadata(projectDir, publishingWith, AbstractRequirePreferSpec.@first, AbstractRequirePreferSpec.@publishedVersion)

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
        File publishedMetadata = publishedIvyOrMavenMetadata(projectDir, publishingWith, first, publishedVersion)
        File publishedGradleMetadata = publishedGradleMetadata(projectDir, publishingWith, first, publishedVersion)

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

    private static def createBom(String depVersion) {
        repo.mkdirs()

        def localBom = new Pom('sample', 'bom', '1.0.0', ArtifactType.POM)
        localBom.addManagementDependency(group, dep, depVersion)
        ArtifactHelpers.setupSamplePomWith(mavenRepo, localBom, localBom.generate())
    }

    private static void assertWhenAllDependenciesHaveVersionRanges(DocWriter docWriter, BuildResult result, String requested1, String requested2, String finalVersion) {
        def firstReasonResultingVersion = "$group:$dep:$requested1 -> $finalVersion"
        docWriter.addAssertionToDoc("First dep resulting version: '$firstReasonResultingVersion'")
        assert result.output.contains(firstReasonResultingVersion)

        def secondReasonResultingVersion = "$group:$dep:$requested2 -> $finalVersion"
        docWriter.addAssertionToDoc("Second dep resulting version: '$secondReasonResultingVersion'")
        assert result.output.contains(secondReasonResultingVersion)

        docWriter.writeFooter('completed assertions')
    }

    private static void assertWhenNotAllDependenciesHaveVersionRanges(DocWriter docWriter, BuildResult result, String require1, String require2, String finalVersion, String prefer1) {
        def losingVersion = Lists.newArrayList(require1, require2)
        losingVersion.remove(finalVersion)

        def winningReasonResultingVersion = "$group:$dep:${finalVersion}"
        docWriter.addAssertionToDoc("Winning dep resulting version: '$winningReasonResultingVersion'")
        assert result.output.contains(winningReasonResultingVersion)

        def firstReasonResultingVersion = "$group:$dep:{require ${losingVersion.first()}; prefer $prefer1} -> ${finalVersion}"
        docWriter.addAssertionToDoc("Dep resulting version: '$firstReasonResultingVersion'")
        assert result.output.contains(firstReasonResultingVersion)

        if(finalVersion != require2) {
            def secondReasonResultingVersion = "$group:$dep:$require2 -> ${finalVersion}"
            docWriter.addAssertionToDoc("Other dep resulting version: '$secondReasonResultingVersion'")
            assert result.output.contains(secondReasonResultingVersion)
        }

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

    private static void assertWithDynamicAndStaticAndPreferenceVersions(DocWriter docWriter, BuildResult result, String requested1, String requested2, String finalVersion) {
        def winningReasonResultingVersion = "$group:$dep:${finalVersion}"
        docWriter.addAssertionToDoc("Winning dep resulting version: '$winningReasonResultingVersion'")
        assert result.output.contains(winningReasonResultingVersion)

        if (requested1 != finalVersion) {
            def firstReasonResultingVersion = "$group:$dep:$requested1 -> $finalVersion"
            docWriter.addAssertionToDoc("First dep resulting version: '$firstReasonResultingVersion'")
            assert result.output.contains(firstReasonResultingVersion)
        }

        if (requested2 != finalVersion) {
            def secondReasonResultingVersion = "$group:$dep:$requested2 -> $finalVersion"
            docWriter.addAssertionToDoc("Second dep resulting version: '$secondReasonResultingVersion'")
            assert result.output.contains(secondReasonResultingVersion)
        }

        docWriter.writeFooter('completed assertions')
    }

    private static String requestedFrom(require, prefer) {
        if(prefer != null) {
            return "{require $require; prefer $prefer}"
        }
        return require
    }

}
