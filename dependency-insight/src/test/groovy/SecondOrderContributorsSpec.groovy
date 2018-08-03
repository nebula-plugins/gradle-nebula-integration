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

    }

    private void setupDependenciesInLocalRepo() {
        repo.mkdirs()

        def mercury100 = new Pom('planet', mercury, '1.0.0')
        ArtifactHelpers.setupSamplePomWith(repo, mercury100, mercury100.generate())

        def mercury101 = new Pom('planet', mercury, '1.0.1')
        ArtifactHelpers.setupSamplePomWith(repo, mercury101, mercury101.generate())

        def mercury102 = new Pom('planet', mercury, '1.0.2')
        ArtifactHelpers.setupSamplePomWith(repo, mercury102, mercury102.generate())


        def venus200 = new Pom('planet', venus, '2.0.0')
        venus200.addDependency('planet', mercury, '1.0.0')
        ArtifactHelpers.setupSamplePomWith(repo, venus200, venus200.generate())

        def venus201 = new Pom('planet', venus, '2.0.1')
        venus201.addDependency('planet', mercury, '1.0.1')
        ArtifactHelpers.setupSamplePomWith(repo, venus201, venus201.generate())


        def earth300 = new Pom('planet', earth, '3.0.0')
        earth300.addDependency('planet', venus, '2.0.0')
        ArtifactHelpers.setupSamplePomWith(repo, earth300, earth300.generate())

        def mars400 = new Pom('planet', mars, '4.0.0')
        mars400.addDependency('planet', venus, '2.0.1')
        ArtifactHelpers.setupSamplePomWith(repo, mars400, mars400.generate())

        def jupiter500 = new Pom('planet', jupiter, '5.0.0')
        jupiter500.addDependency('planet', mercury, '1.0.2')
        ArtifactHelpers.setupSamplePomWith(repo, jupiter500, jupiter500.generate())
    }
}
