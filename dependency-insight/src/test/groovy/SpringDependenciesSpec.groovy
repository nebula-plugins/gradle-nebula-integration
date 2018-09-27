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
class SpringDependenciesSpec extends TestKitSpecification {
    File repo
    def tasks

    def setup() {
        repo = new File(projectDir, 'repo')
        tasks = ['dependencyInsight', '--dependency', "cassandra-driver-core"]
    }

    @Unroll
    def "spring-boot-dependencies v#springBootVersionFamily should provide a reason"() {
        given:
        buildFile << createBuildFileWithSpringBootGradlePluginV(springBootVersion)

        when:

        def result = runTasks(*tasks)

        DocWriter docWriter = new DocWriter("spring-boot-dependencies-v${springBootVersionFamily}", projectDir, 'misc')

        then:
        docWriter.writeProjectFiles()
        docWriter.writeCleanedUpBuildOutput(
                "Tasks: ${String.join(' ', tasks)}\n\n${result.output}\n\n")

        if(selectedBySpringRule) {
            def versionOutput = "com.datastax.cassandra:cassandra-driver-core:$cassandraDriverVersion (selected by rule)"
            docWriter.addAssertionToDoc("Contains '$versionOutput'")
            assert(result.output.contains(versionOutput))

            def selectionReasons = 'Selection reasons:'
            docWriter.addAssertionToDoc("Contains '$selectionReasons'")
            assert(result.output.contains(selectionReasons))
        } else {
            def output = "com.datastax.cassandra:cassandra-driver-core:$cassandraDriverVersion\n"
            docWriter.addAssertionToDoc("Contains '$output'")
            assert(result.output.contains(output))
        }

        docWriter.writeFooter('completed assertions')

        where:
        springBootVersionFamily | springBootVersion | cassandraDriverVersion | selectedBySpringRule
        '1.x.x' | '1.4.7' | '2.1.9' | true
        '2.x.x'                 | '2.0.5' | '3.3.2' | false
    }

    private static String createBuildFileWithSpringBootGradlePluginV(String version) {
        """
buildscript {
    repositories {
        jcenter()
    }
    dependencies { 
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${version}.RELEASE") 
    }
}
apply plugin: 'org.springframework.boot'
apply plugin: 'java'

repositories {
    jcenter()
}

dependencies {
    compile 'com.netflix.astyanax:astyanax-cql:3.10.1'
}
"""
    }
}
