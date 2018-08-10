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


import com.google.common.collect.ImmutableMap
import spock.lang.Unroll

class VerifyInsightAlignment extends AbstractVerifyInsight {
    static def slf4jApi = 'slf4j-api'
    static def slf4jSimple = 'slf4j-simple'
    static def slf4jApiDependency = 'org.slf4j:slf4j-api'
    static def slf4jSimpleDependency = 'org.slf4j:slf4j-simple'
    static def slf4jApiStatic = '1.6.0'
    static def slf4jSimpleStatic = '1.7.20'
    static def slf4jApiForce = '1.5.0'
    static def slf4jSimpleForce = '1.5.5'
    static def slf4jUseLocks = true
    static def slf4jApiRec = '1.7.25'

    private String asDirectOutput
    private String asTransitiveOutput
    private DependencyHelper dhAsDirect
    private DependencyHelper dhAsTransitive
    private DocWriter w

    private String moduleAsDirect
    private Coordinate expected1

    private String moduleAsTransitive
    private Coordinate expected2

    public static
    def lookupRequestedModuleIdentifier = ImmutableMap.of(
            slf4jApi, slf4jApiDependency,
            slf4jSimple, slf4jSimpleDependency)

    @Unroll
    def "#asDirect: #title"() {
        given:
        createSimpleBuildFile(recAsDirect)
        createSettingsFile()

        dhAsDirect = new DependencyHelper()
        dhAsDirect.requestedModuleIdentifier = asDirect
        dhAsDirect.staticVersion = staticAsDirect
        dhAsDirect.recommendedVersion = recAsDirect
        dhAsDirect.lookupRequestedModuleIdentifier = lookupRequestedModuleIdentifier
        dhAsDirect.useLocks = useLocks
        dhAsDirect.forceVersion = forceAsDirect
        def version1 = dhAsDirect.findRequestedVersion() // static, dynamic, or recommended

        dhAsTransitive = new DependencyHelper()
        dhAsTransitive.requestedModuleIdentifier = asTransitive
        dhAsTransitive.staticVersion = staticAsTransitive
        dhAsTransitive.recommendedVersion = null
        dhAsTransitive.lookupRequestedModuleIdentifier = lookupRequestedModuleIdentifier
        dhAsTransitive.useLocks = useLocks
        dhAsTransitive.forceVersion = forceAsTransitive
        def version2 = dhAsTransitive.findRequestedVersion() // static, dynamic, or recommended

        buildFile << 'dependencies {\n'

        if (recAsDirect != null) {
            buildFile << '    compile \'sample:bom:1.0.0\'\n'
        }

        buildFile << "    compile '${lookupRequestedModuleIdentifier[asDirect]}${version1}'\n"
        buildFile << "    compile '${lookupRequestedModuleIdentifier[asTransitive]}${version2}'\n"

        buildFile << '}\n'.stripIndent()

        // add configuration
        createForceConfigurationIfNeeded(asDirect, forceAsDirect, lookupRequestedModuleIdentifier)
        createForceConfigurationIfNeeded(asTransitive, forceAsTransitive, lookupRequestedModuleIdentifier)
        createAlignmentConfiguration(asDirect, lookupRequestedModuleIdentifier)

        // add additional files
        createBomIfNeeded(recAsDirect)
        createLocksIfNeeded(useLocks)

        createJavaSourceFile(projectDir, createMainFile())
        def asDirectTasks = tasksFor(asDirect)
        if (useLocks) {
            asDirectTasks << '--write-locks'
        }

        def asTransitiveTasks = tasksFor(asTransitive)
        if (useLocks) {
            asTransitiveTasks << '--write-locks'
        }

        when:
        def asDirectResult = runTasks(*asDirectTasks)
        def asTransitiveResult = runTasks(*asTransitiveTasks)
        w = new DocWriter(title, projectDir, grouping)

        then:
        w.writeCleanedUpBuildOutput("=== For the dependency under test: ${asDirect} as a direct dependency ===\n" +
                "Tasks: ${asDirectTasks.join(' ')}\n\n" +
                asDirectResult.output,
                "\n\n=== For the dependency under test: ${asTransitive} bringing in ${asDirect} as a transitive dependency ===\n" +
                        "Tasks: ${asTransitiveTasks.join(' ')}\n\n" +
                        asTransitiveResult.output + '\n')
        w.writeProjectFiles()

        when:
        moduleAsDirect = dhAsDirect.lookupRequestedModuleIdentifier[dhAsDirect.requestedModuleIdentifier]
        expected1 = dhAsDirect.results()
        def requestedVersion1 = dhAsDirect.findRequestedVersion()
        asDirectOutput = asDirectResult.output

        moduleAsTransitive = dhAsTransitive.lookupRequestedModuleIdentifier[dhAsTransitive.requestedModuleIdentifier]
        expected2 = dhAsTransitive.results()
        def requestedVersion2 = dhAsTransitive.findRequestedVersion()
        asTransitiveOutput = asTransitiveResult.output

        then:
        assert expected1.version != null || expected1.moduleIdentifierWithVersion != null
        assert expected2.version != null || expected2.moduleIdentifierWithVersion != null

        runAssertions()

        w.writeFooter('completed assertions')

        where:
        asDirect | asTransitive | staticAsDirect | staticAsTransitive | recAsDirect | forceAsDirect | forceAsTransitive | useLocks      | grouping    | title
//        alignment - static
        slf4jApi | slf4jSimple  | slf4jApiStatic | slf4jSimpleStatic  | null        | null          | null              | null          | 'alignment' | 'alignment-static'
        slf4jApi | slf4jSimple  | slf4jApiStatic | slf4jSimpleStatic  | null        | slf4jApiForce | null              | null          | 'alignment' | 'alignment-static-direct-force'
        slf4jApi | slf4jSimple  | slf4jApiStatic | slf4jSimpleStatic  | null        | slf4jApiForce | null              | slf4jUseLocks | 'alignment' | 'alignment-static-direct-force-lock'
        slf4jApi | slf4jSimple  | slf4jApiStatic | slf4jSimpleStatic  | null        | null          | slf4jSimpleForce  | null          | 'alignment' | 'alignment-static-transitive-force'
        slf4jApi | slf4jSimple  | slf4jApiStatic | slf4jSimpleStatic  | null        | null          | slf4jSimpleForce  | slf4jUseLocks | 'alignment' | 'alignment-static-transitive-force-lock'
        slf4jApi | slf4jSimple  | slf4jApiStatic | slf4jSimpleStatic  | null        | slf4jApiForce | slf4jSimpleForce  | null          | 'alignment' | 'alignment-static-both-force'
        slf4jApi | slf4jSimple  | slf4jApiStatic | slf4jSimpleStatic  | null        | slf4jApiForce | slf4jSimpleForce  | slf4jUseLocks | 'alignment' | 'alignment-static-both-force-lock'
        slf4jApi | slf4jSimple  | slf4jApiStatic | slf4jSimpleStatic  | null        | null          | null              | slf4jUseLocks | 'alignment' | 'alignment-static-lock'
////        alignment - with recommendation
        slf4jApi | slf4jSimple  | null           | slf4jSimpleStatic  | slf4jApiRec | null          | null              | null          | 'alignment' | 'alignment-rec'
        slf4jApi | slf4jSimple  | null           | slf4jSimpleStatic  | slf4jApiRec | slf4jApiForce | null              | null          | 'alignment' | 'alignment-rec-direct-force'
        slf4jApi | slf4jSimple  | null           | slf4jSimpleStatic  | slf4jApiRec | slf4jApiForce | null              | slf4jUseLocks | 'alignment' | 'alignment-rec-direct-force-lock'
        slf4jApi | slf4jSimple  | null           | slf4jSimpleStatic  | slf4jApiRec | null          | slf4jSimpleForce  | null          | 'alignment' | 'alignment-rec-transitive-force'
        slf4jApi | slf4jSimple  | null           | slf4jSimpleStatic  | slf4jApiRec | null          | slf4jSimpleForce  | slf4jUseLocks | 'alignment' | 'alignment-rec-transitive-force-lock'
        slf4jApi | slf4jSimple  | null           | slf4jSimpleStatic  | slf4jApiRec | slf4jApiForce | slf4jSimpleForce  | null          | 'alignment' | 'alignment-rec-both-force'
        slf4jApi | slf4jSimple  | null           | slf4jSimpleStatic  | slf4jApiRec | slf4jApiForce | slf4jSimpleForce  | slf4jUseLocks | 'alignment' | 'alignment-rec-both-force-lock'
        slf4jApi | slf4jSimple  | null           | slf4jSimpleStatic  | slf4jApiRec | null          | null              | slf4jUseLocks | 'alignment' | 'alignment-rec-lock'
    }

    void runAssertions() {
        assertionsForAll()
        assertionsOnLocks()
        if (assertionsOnForces()) {
            assertionsOnRecommendationsWithForces()
            assertionsOnStaticDefinitionsWithForces()
            return // stop assertions here
        }
        assertionsOnRecommendations()
        assertionsOnStaticDefinitions()
    }

    private void assertionsForAll() {
        if (!(dhAsDirect.forceVersion != null && dhAsTransitive.forceVersion != null)) {
            def headerSearchCriteria1 = dhAsDirect.useLocks ? "> Task :dependencyInsight\n.*\n${moduleAsDirect}:.*" : "> Task :dependencyInsight\n${moduleAsDirect}:.*"
            def headerAsDirect = asDirectOutput.find(headerSearchCriteria1)
            def resolvedVersionAsDirect = headerAsDirect.split(':').last()

            def headerSearchCriteria2 = dhAsDirect.useLocks ? "> Task :dependencyInsight\n.*\n${moduleAsTransitive}:.*" : "> Task :dependencyInsight\n${moduleAsTransitive}:.*"
            def headerAsTransitive = asTransitiveOutput.find(headerSearchCriteria2)
            def resolvedVersionAsTransitive = headerAsTransitive.split(':').last()

            w.addAssertionToDoc("resolved versions should be the same: '$moduleAsDirect:$resolvedVersionAsDirect' and '$moduleAsTransitive:$resolvedVersionAsTransitive' [while both are not forced]")
            assert resolvedVersionAsDirect == resolvedVersionAsTransitive
        }

        def depGroupAndArtifact = moduleAsDirect.split(':')
        def group = depGroupAndArtifact[0]
        def platform = group.split('\\.').last() + '-platform'
        def expectedPlatformConstraint = "By constraint : belongs to platform $group:$platform:"

        w.addAssertionToDoc("$moduleAsDirect output contains '$expectedPlatformConstraint' [platform constraint]")
        assert asDirectOutput.contains(expectedPlatformConstraint)

        w.addAssertionToDoc("$moduleAsTransitive output contains '$expectedPlatformConstraint' [platform constraint]")
        assert asTransitiveOutput.contains(expectedPlatformConstraint)

        def expectedDependencyComesFrom = "--- $moduleAsTransitive:"
        w.addAssertionToDoc("$moduleAsDirect output contains '$expectedDependencyComesFrom' [dependency is brought in by...]")
        assert asDirectOutput.contains(expectedDependencyComesFrom)
    }

    private void assertionsOnLocks() {
        if (dhAsDirect.useLocks != null) {
            def expectedOutput = 'Persisted dependency lock state'
            w.addAssertionToDoc("$moduleAsDirect output contains '$expectedOutput' [locked & build matches lock version]")
            assert asDirectOutput.contains(expectedOutput)
        }
        if (dhAsTransitive.useLocks != null) {
            def expectedOutput = 'Persisted dependency lock state'
            w.addAssertionToDoc("$moduleAsTransitive output contains '$expectedOutput' [locked & build matches lock version]")
            assert asTransitiveOutput.contains(expectedOutput)
        }
    }

    private boolean assertionsOnForces() {
        def runningForceAssertions = false

        def expectedForceOutput = 'forced'
        if (dhAsDirect.forceVersion == null && dhAsTransitive.forceVersion != null) {
            w.addAssertionToDoc("$moduleAsDirect does not contain '$expectedForceOutput'")
            assert !asDirectOutput.toLowerCase().contains(expectedForceOutput)

            w.addAssertionToDoc("$moduleAsTransitive output contains '$expectedForceOutput'")
            assert asTransitiveOutput.toLowerCase().contains(expectedForceOutput)

            runningForceAssertions = true
        } else if (dhAsDirect.forceVersion != null && dhAsTransitive.forceVersion == null) {
            w.addAssertionToDoc("$moduleAsDirect contains '$expectedForceOutput'")
            assert asDirectOutput.toLowerCase().contains(expectedForceOutput)

            w.addAssertionToDoc("$moduleAsTransitive output does not contain '$expectedForceOutput'")
            assert !asTransitiveOutput.toLowerCase().contains(expectedForceOutput)

            runningForceAssertions = true
        } else if (dhAsDirect.forceVersion != null && dhAsTransitive.forceVersion != null) {
            w.addAssertionToDoc("$moduleAsDirect output contains '$expectedForceOutput'")
            assert asDirectOutput.toLowerCase().contains(expectedForceOutput)

            w.addAssertionToDoc("$moduleAsTransitive output contains '$expectedForceOutput'")
            assert asTransitiveOutput.toLowerCase().contains(expectedForceOutput)

            runningForceAssertions = true
        }

        runningForceAssertions
    }

    private void assertionsOnRecommendations() {
        if (dhAsDirect.recommendedVersion != null) {
            assertBomIsInUse()

            // asDirect result
            def expectedOutput1 = "${moduleAsDirect} -> ${expected1.version}"
            w.addAssertionToDoc("$moduleAsDirect contains '$expectedOutput1' [recommended]")
            assert asDirectOutput.contains(expectedOutput1)

            def endResultRegex1
            if (dhAsDirect.useLocks) {
                endResultRegex1 = "Task.*\n.*\n.*${moduleAsDirect}:${expected1.version}"
            } else {
                endResultRegex1 = "Task.*\n.*${moduleAsDirect}:${expected1.version}"
            }
            w.addAssertionToDoc("$moduleAsDirect output contains '$endResultRegex1' [recommended end result]")
            assert asDirectOutput.findAll(endResultRegex1).size() > 0

            // asTransitive result
            def expectedOutput2 = "${moduleAsTransitive}:${expected2.version} -> ${expected1.version}"
            w.addAssertionToDoc("$moduleAsTransitive contains '$expectedOutput2' [recommended]")
            assert asTransitiveOutput.contains(expectedOutput2)

            def endResultRegex2
            if (dhAsTransitive.useLocks) {
                endResultRegex2 = "Task.*\n.*\n.*${moduleAsTransitive}:${expected1.version}"
            } else {
                endResultRegex2 = "Task.*\n.*${moduleAsTransitive}:${expected1.version}"
            }
            w.addAssertionToDoc("$moduleAsTransitive output contains '$endResultRegex2' [recommended end result]")
            assert asTransitiveOutput.findAll(endResultRegex2).size() > 0
        }
    }

    private void assertionsOnRecommendationsWithForces() {
        if (dhAsDirect.recommendedVersion != null) {
            assertBomIsInUse()

            // asDirect result
            def expectedOutput1 = "${moduleAsDirect} -> ${expected1.version}"
            w.addAssertionToDoc("$moduleAsDirect contains '$expectedOutput1' [recommended version with force(s) output]")
            assert asDirectOutput.contains(expectedOutput1)

            def endResultRegex1
            if (dhAsDirect.useLocks) {
                endResultRegex1 = "Task.*\n.*\n.*"
            } else {
                endResultRegex1 = "Task.*\n.*"
            }
            endResultRegex1 += "${moduleAsDirect}:${expected1.version}"

            w.addAssertionToDoc("$moduleAsDirect output contains '$endResultRegex1' [recommended end result with force(s)]")
            assert asDirectOutput.findAll(endResultRegex1).size() > 0

            // asTransitive result
            def expectedOutput2
            if (dhAsTransitive.forceVersion != null) {
                expectedOutput2 = "${moduleAsTransitive}${dhAsTransitive.findRequestedVersion()} -> ${expected2.version}"
            } else {
                expectedOutput2 = "${moduleAsTransitive}:${expected2.version} -> ${expected1.version}"
            }
            w.addAssertionToDoc("$moduleAsTransitive contains '$expectedOutput2' [recommended]")
            assert asTransitiveOutput.contains(expectedOutput2)

            def endResultRegex2
            if (dhAsTransitive.useLocks) {
                endResultRegex2 = "Task.*\n.*\n.*"
            } else {
                endResultRegex2 = "Task.*\n.*"
            }
            if (dhAsTransitive.forceVersion != null) {
                endResultRegex2 += "${moduleAsTransitive}:${expected2.version}"
            } else {
                endResultRegex2 += "${moduleAsTransitive}:${expected1.version}"
            }

            w.addAssertionToDoc("$moduleAsTransitive output contains '$endResultRegex2' [recommended end result]")
            assert asTransitiveOutput.findAll(endResultRegex2).size() > 0
        }
    }

    private void assertionsOnStaticDefinitions() {
        if (dhAsDirect.staticVersion != null) {
            def reasonOutput1 = "$moduleAsDirect${dhAsDirect.findRequestedVersion()} -> ${expected2.version}"
            w.addAssertionToDoc("$moduleAsDirect contains '$reasonOutput1' [static version reason]")
            assert asDirectOutput.contains(reasonOutput1)
        }
    }

    def assertionsOnStaticDefinitionsWithForces() {
        if (dhAsDirect.staticVersion != null) {
            def reasonOutput1
            if (dhAsDirect.forceVersion != null) {
                reasonOutput1 = "$moduleAsDirect${dhAsDirect.findRequestedVersion()} -> ${expected1.version}"
            } else {
                reasonOutput1 = "$moduleAsDirect${dhAsDirect.findRequestedVersion()} -> ${expected2.version}"
            }
            w.addAssertionToDoc("$moduleAsDirect contains '$reasonOutput1' [static version with force(s) output]")
            assert asDirectOutput.contains(reasonOutput1)


            def reasonOutput2
            if (dhAsTransitive.forceVersion != null) {
                reasonOutput2 = "$moduleAsTransitive${dhAsTransitive.findRequestedVersion()} -> ${expected2.version}"
            } else {
                reasonOutput2 = "$moduleAsTransitive${dhAsTransitive.findRequestedVersion()} -> ${expected1.version}"
            }
            w.addAssertionToDoc("$moduleAsTransitive contains '$reasonOutput2' [static version with force(s) output]")
            assert asTransitiveOutput.contains(reasonOutput2)
        }
    }

    private void assertBomIsInUse() {
        def bomDependencyConstraint = '\\--- sample:bom:1.0.0'
        w.addAssertionToDoc("$moduleAsDirect output contains '$bomDependencyConstraint' [bom dependency constraint - recommended]")
        assert asDirectOutput.contains(bomDependencyConstraint)
    }
}
