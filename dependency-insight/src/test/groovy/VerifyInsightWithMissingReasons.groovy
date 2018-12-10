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

import spock.lang.Unroll

class VerifyInsightWithMissingReasons extends AbstractVerifyInsight {
    static def guava = 'guava'
    static def spymemcached = 'spymemcached'
    static def mockito = 'mockito'

    @Unroll
    def "#title"() {
        given:
        createSettingsFile()

        buildFile << """
apply plugin: 'java'

repositories {
    jcenter()
}

configurations.all {
    resolutionStrategy {
        ${if (ruleWithReason) { "eachDependency { it.useVersion('20.0'); it.because('RULE 2') }" } else { '' } }
        ${if (ruleWithoutReason) { "eachDependency { it.useVersion('19.0') }" } else { '' } }
    }
}
            
dependencies {
    compile 'com.google.guava:guava:18.+'
}
"""
        def tasks = tasksFor(dep)

        when:
        def result = runTasks(*tasks)
        DocWriter w = new DocWriter("$title", projectDir, grouping)

        then:
        w.writeGradleVersion(project.gradle.gradleVersion)
        w.writeCleanedUpBuildOutput('=== For the dependency under test ===\n' +
                "Tasks: ${tasks.join(' ')}\n\n" +
                result.output)
        w.writeProjectFiles()

        def output = result.output

        if (ruleWithReason) {
            def selectedByRule = '- Selected by rule : RULE 2'
            w.addAssertionToDoc("contains '$selectedByRule'")
            assert output.contains(selectedByRule)
        }

        if (ruleWithoutReason) {
            if (ruleWithReason) {
                def selectionReasons = "Selection reasons:"
                w.addAssertionToDoc("contains '$selectionReasons ' heading")
                assert output.contains(selectionReasons)

                def selectedByRuleWithNoReason = '- Selected by rule\n'
                w.addAssertionToDoc("contains '$selectedByRuleWithNoReason ' - with no reason")
                assert output.contains(selectedByRuleWithNoReason)

            } else {
                def selectionReasons = 'Selection reasons:'
                w.addAssertionToDoc("does not contain '$selectionReasons ' heading")
                assert !output.contains(selectionReasons)

                def onlyNoReasonSelection = '(selected by rule)'
                w.addAssertionToDoc("contains '$onlyNoReasonSelection ' - with no reason")
                assert output.contains(onlyNoReasonSelection)
            }
        }

        w.writeFooter('completed assertions')

        where:
        dep   | ruleWithReason | ruleWithoutReason | grouping          | title
        guava | true           | true              | 'missing-reasons' | 'use-version-with-and-without-a-reason'
        guava | false          | true              | 'missing-reasons' | 'use-version-without-a-reason'
        guava | true           | false             | 'missing-reasons' | 'use-version-with-a-reason'
    }

    @Unroll
    def "from gradle/gradle#6826 - #title"() {
        given:
        createSettingsFile()

        buildFile << """
apply plugin: 'java'

repositories {
    mavenCentral()
}

configurations.all {
    resolutionStrategy {
        eachDependency {DependencyResolveDetails details ->
            if (details.requested.group == 'net.spy') {
                details.useVersion '2.11.7'
                ${if(detailReasonProvided) {"details.because '2.11.7 is a good one'"} else { '' } }
            }
        }
    }
    resolutionStrategy.dependencySubstitution {
       substitute(module('net.spy:spymemcached'))
         ${if(substituteReasonProvided) {".because('not really 2.11.6 is a good one')"} else { '' } }
         .with(module('net.spy:spymemcached:2.11.6'))
    }
}

dependencies {
    compile 'net.spy:spymemcached:2.11.5'
}"""
        def tasks = tasksFor(dep)

        when:
        def result = runTasks(*tasks)
        DocWriter w = new DocWriter("$title", projectDir, grouping)

        then:
        w.writeGradleVersion(project.gradle.gradleVersion)
        w.writeCleanedUpBuildOutput('=== For the dependency under test ===\n' +
                "Tasks: ${tasks.join(' ')}\n\n" +
                result.output)
        w.writeProjectFiles()

        def output = result.output


        def selectionReasons = "Selection reasons:"
        w.addAssertionToDoc("contains '$selectionReasons ' heading")
        assert output.contains(selectionReasons)

        if(detailReasonProvided) {
            def selectedByRule = '- Selected by rule : 2.11.7 is a good one'
            w.addAssertionToDoc("contains '$selectedByRule'")
            assert output.contains(selectedByRule)
        }

        if(substituteReasonProvided) {
            def selectedByRuleWithNoReason = '- Selected by rule : not really 2.11.6 is a good one'
            w.addAssertionToDoc("contains '$selectedByRuleWithNoReason ' - with no reason")
            assert output.contains(selectedByRuleWithNoReason)
        }

        if(substituteReasonProvided && !detailReasonProvided || detailReasonProvided && !substituteReasonProvided) {
            def noReason = '- Selected by rule\n'
            w.addAssertionToDoc("contains '${noReason.replace('\n', '\\n')}' exactly once")
            assert output.findAll(noReason).size() == 1
        }

        if(!substituteReasonProvided && !detailReasonProvided) {
            def noReason = '- Selected by rule\n'
            w.addAssertionToDoc("contains '${noReason.replace('\n', '\\n')}' exactly twice")
            assert output.findAll(noReason).size() == 2
        }

        w.writeFooter('completed assertions')

        where:
        dep          | detailReasonProvided | substituteReasonProvided | grouping                     | title
        spymemcached | false                | false                    | 'missing-reasons-with-subst' | 'details-and-subst-without-reason'
        spymemcached | true                 | true                     | 'missing-reasons-with-subst' | 'details-and-subst-with-reason'
        spymemcached | true                 | false                    | 'missing-reasons-with-subst' | 'details-with-reason-subst-without'
        spymemcached | false                | true                     | 'missing-reasons-with-subst' | 'subst-with-reason-details-without'
    }

    @Unroll
    def "reasons - #title"() {
        given:
        createSettingsFile()

        buildFile << """
apply plugin: 'java'

repositories {
    jcenter()
}

dependencies {
    compile 'org.mockito:mockito-all:1.8.0'
}

// resolution strategy adapted from https://github.com/gradle/gradle/blob/master/subprojects/diagnostics/src/integTest/groovy/org/gradle/api/tasks/diagnostics/DependencyInsightReportTaskIntegrationTest.groovy#L732-L743
configurations.all {
    resolutionStrategy {
        eachDependency { 
            if (it.requested.group == 'org.mockito') {
                it.useVersion('1.9.0')${if(showReasons) {"; it.because('RULE 1')"} else { '' } }
            }
        }
        eachDependency { 
            if (it.requested.group == 'org.mockito') {
                it.useVersion('1.8.5')${if(showReasons) {"; it.because('RULE 2')"} else { '' } } 
            }
        }
        dependencySubstitution {
            substitute module('org.mockito:mockito-all') ${if(showReasons) {"because 'SUBSTITUTION 1' "} else { '' } }with module('org.mockito:mockito-all:1.9.5')
            substitute module('org.mockito:mockito-all') ${if(showReasons) {"because 'SUBSTITUTION 2' "} else { '' } }with module('org.mockito:mockito-core:1.10.5')
            all {
                if (it.requested.group == 'org.mockito') {
                    it.useTarget('org.mockito:mockito-core:1.8.3'${if(showReasons) {", 'SUBSTITUTION 3'"} else { '' } })
                }
            }
        }
        force('org.mockito:mockito-all:1.8.4')
    }
}
"""
        def tasks = tasksFor(dep)

        when:
        def result = runTasks(*tasks)
        DocWriter w = new DocWriter("$title", projectDir, grouping)

        then:
        w.writeGradleVersion(project.gradle.gradleVersion)
        w.writeCleanedUpBuildOutput('=== For the dependency under test ===\n' +
                "Tasks: ${tasks.join(' ')}\n\n" +
                result.output)
        w.writeProjectFiles()

        def output = result.output

        def selectionReasons = "Selection reasons:"
        w.addAssertionToDoc("contains '$selectionReasons ' heading")
        assert output.contains(selectionReasons)

        if(showReasons) {
            def reasons = [
                "- Forced",
                "- Selected by rule : RULE 1",
                "- Selected by rule : RULE 2",
                "- Selected by rule : SUBSTITUTION 1",
                "- Selected by rule : SUBSTITUTION 2",
                "- Selected by rule : SUBSTITUTION 3"]
            reasons.each {
                w.addAssertionToDoc("contains '$it'")
                assert output.contains(it)
            }
        } else {
            def reasons = [
                "useVersion('1.9.0')",
                "useVersion('1.8.5')",
                "substitute module('org.mockito:mockito-all') with module('org.mockito:mockito-all:1.9.5')",
                "substitute module('org.mockito:mockito-all') with module('org.mockito:mockito-core:1.10.5')",
                "useTarget('org.mockito:mockito-core:1.8.3')",
                "forced"
            ]
            w.addAssertionToDoc("would be nice to contain each of:\n${reasons.stream().map{ it -> "    - $it\n"}.collect().join('') }")

            reasons.each {
                assert output.contains(it)
            }
        }

        w.writeFooter('completed assertions')

        where:
        dep     | showReasons | grouping          | title
        mockito | false       | 'missing-reasons' | 'complex-would-love-reasons-here'
        mockito | true        | 'missing-reasons' | 'complex-have-reasons-here'
    }
}
