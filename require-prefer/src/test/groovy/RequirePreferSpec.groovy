import nebula.test.dependencies.maven.Pom
import org.assertj.core.util.Lists
import org.gradle.util.VersionNumber
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

    def setup() {
        repo = new File('repo')
        repo.deleteDir()
        setupDependenciesInLocalRepo()
    }

    @Unroll
    def "#title"() {
        given:
        addSubproject(first, buildFileWithDependencyVersions(firstRequireVersion, preferFirstVersion))
        addSubproject(second, buildFileWithDependencyVersions(secondRequireVersion, preferSecondVersion))

        buildFile << simpleParentMultiProjectBuildFile()

        when:
        def tasks = ['dependencyInsight', '--dependency', "$dep"]
        def result = runTasks(*tasks)

        DocWriter docWriter = new DocWriter(title, projectDir, 'misc')

        then:
        docWriter.writeProjectFiles()
        docWriter.writeCleanedUpBuildOutput(
                "Tasks: ${String.join(' ', tasks)}\n\n${result.output}\n\n")

        def versionRangeIndicators = ['[', ']', '(', ')']
        def firstHasDynamicDep = versionRangeIndicators.any { firstRequireVersion.contains(it) }
        def secondHasDynamicDep = versionRangeIndicators.any { secondRequireVersion.contains(it) }

        if (!firstHasDynamicDep && !secondHasDynamicDep) {
            def winningVersion = returnHigherOf(firstRequireVersion, secondRequireVersion)
            def losingVersion = Lists.newArrayList(firstRequireVersion, secondRequireVersion)
            losingVersion.remove(winningVersion)

            def winningReasonResultingVersion = "$group:$dep:$winningVersion"
            docWriter.addAssertionToDoc("Winning dep resulting version: '$winningReasonResultingVersion'")
            assert result.output.contains(winningReasonResultingVersion)

            def losingReasonResultingVersion = "$group:$dep:${losingVersion.first()} -> $winningVersion"
            docWriter.addAssertionToDoc("Losing dep resulting version: '$losingReasonResultingVersion'")
            assert result.output.contains(losingReasonResultingVersion)


        } else {
            def firstReasonResultingVersion = "$group:$dep:$firstRequireVersion -> $resultingVersion"
            docWriter.addAssertionToDoc("First dep resulting version: '$firstReasonResultingVersion'")
            assert result.output.contains(firstReasonResultingVersion)

            def secondReasonResultingVersion = "$group:$dep:$secondRequireVersion -> $resultingVersion"
            docWriter.addAssertionToDoc("Second dep resulting version: '$secondReasonResultingVersion'")
            assert result.output.contains(secondReasonResultingVersion)
        }

        docWriter.writeFooter('completed assertions')

        where:
        preferFirstVersion | preferSecondVersion | firstRequireVersion | secondRequireVersion | resultingVersion | title
        '1.5'              | null                | '[1.2, 2.0)'        | '[1.2, 2.0)'         | '1.5'            | 'one-preference-in-required-range'
        '1.5'              | '1.6'               | '[1.2, 2.0)'        | '[1.2, 2.0)'         | '1.6'            | 'two-preferences-in-required-range'
        '1.5'              | null                | '[1.2, 2.0)'        | '[2.0, 3.0)'         | '2.9'            | 'conflict-resolution-when-ranges-do-not-overlap---preference-is-not-used'
        '1.5'              | null                | '[1.4, 2.0)'        | '[1.0, 1.2)'         | '1.5'            | 'conflict-resolution-when-ranges-do-not-overlap---preference-is-used'
        '1.5'              | null                | '1.0'               | '2.0'                | '2.0'            | 'preference-not-used-with-static-dependency'
    }

    private def simpleParentMultiProjectBuildFile() {
        """
apply plugin: 'java'

repositories {
    maven { url { '${repo.absolutePath}' } }
}

dependencies {
    compile project(':$first')
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

    def returnLowerOf(String v1, String v2) {
        def compareTo = VersionNumber.parse(v1).compareTo(VersionNumber.parse(v2))
        if (compareTo < 0) {
            return v1
        }
        return v2
    }

    def returnHigherOf(String v1, String v2) {
        def compareTo = VersionNumber.parse(v1).compareTo(VersionNumber.parse(v2))
        if (compareTo < 0) {
            return v2
        }
        return v1
    }
}
