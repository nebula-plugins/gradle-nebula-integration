import com.google.common.collect.ImmutableMap
import nebula.test.dependencies.maven.Pom

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
class SecondOrderContributorsSpec extends TestKitSpecification {
    String mercury = 'mercury'
    String venus = 'venus'
    String earth = 'earth'
    String mars = 'mars'
    String jupiter = 'jupiter'
    File repo

    def setup() {
        repo = new File(projectDir, 'repo')
        setupDependenciesInLocalRepo()
    }

    def "second order contributors"() {
        given:
        buildFile << """
apply plugin: 'java'

repositories {
    maven { url { 'repo' } }
}

dependencies {
    compile 'planet:$earth:3.0.0'
    compile 'planet:$mars:4.0.0'
    compile 'planet:$jupiter:5.0.0'
}"""

        when:
        def venusDependencyInsightTasks = ['dependencyInsight', '--dependency', "$venus"]
        def mercuryDependencyInsightTasks = ['dependencyInsight', '--dependency', "$mercury"]

        def venusResult = runTasks(*venusDependencyInsightTasks)
        def mercuryResult = runTasks(*mercuryDependencyInsightTasks)

        DocWriter docWriter = new DocWriter('second-order-contributor', projectDir, 'misc')

        then:
        docWriter.writeProjectFiles()
        docWriter.writeCleanedUpBuildOutput(
                "Mercury\nTasks: ${String.join(' ', mercuryDependencyInsightTasks)}\n\n${mercuryResult.output}\n\n",
                "Venus\nTasks: ${String.join(' ', venusDependencyInsightTasks)}\n\n${venusResult.output}\n\n")

        def expectedSecondOrderConflictStatement = '\\--- planet:venus:2.0.1 (conflict resolution between versions'
        docWriter.addAssertionToDoc("[mercury dependencyInsight] contains '$expectedSecondOrderConflictStatement'")
        assert mercuryResult.output.contains(expectedSecondOrderConflictStatement)

        docWriter.writeFooter('completed assertions')
    }

    private void setupDependenciesInLocalRepo() {
        repo.mkdirs()
        setupLocalDependency(mercury, '1.0.0', Collections.emptyMap())
        setupLocalDependency(mercury, '1.0.1', Collections.emptyMap())
        setupLocalDependency(mercury, '1.0.2', Collections.emptyMap())
        setupLocalDependency(venus, '2.0.0', ImmutableMap.of(mercury, '1.0.0'))
        setupLocalDependency(venus, '2.0.1', ImmutableMap.of(mercury, '1.0.1'))
        setupLocalDependency(earth, '3.0.0', ImmutableMap.of(venus, '2.0.0'))
        setupLocalDependency(mars, '4.0.0', ImmutableMap.of(venus, '2.0.1'))
        setupLocalDependency(jupiter, '5.0.0', ImmutableMap.of(mercury, '1.0.2'))
    }

    private void setupLocalDependency(String artifactName, String version, Map<String, String> dependencies) {
        def group = 'planet'
        def pom = new Pom(group, artifactName, version)
        for (Map.Entry<String, String> entry : dependencies) {
            pom.addDependency(group, entry.key, entry.value)
        }
        ArtifactHelpers.setupSamplePomWith(repo, pom, pom.generate())
    }
}
