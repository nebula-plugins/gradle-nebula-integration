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

class VerifyInsight extends AbstractVerifyInsight {
    static def guava = 'guava'
    static def guavaDependency = 'com.google.guava:guava'
    static def guavaStatic = '18.0'
    static def guavaDynamic = '18.+'
    static def guavaForce = '14.0.1'
    static def guavaLock = true
    static def guavaRec = '19.0'

    static def guavaReplaceFrom = new Coordinate('com.google.collections:google-collections', '1.0')

    static def guavaDynamicForRejection = '16.+'
    static def guavaReject = '16.0.1'
    static def guavaResolveRejection = '16.0'

    static def mockito = 'mockito'
    static def mockitoDependency = 'org.mockito:mockito-all'
    static def mockitoStatic = '1.8.0'
    static def mockitoDynamic = '1.8.+'
    static def mockitoForce = '1.10.17'
    static def mockitoLock = true
    static def mockitoRec = '1.9.5'
    static def mockitoSubTo = 'org.mockito:mockito-core:1.10.19'

    static def netty = 'netty'
    static def nettyDependency = 'io.netty:netty-all'
    static def nettyStatic = '4.1.20.FINAL'
    static def nettyDynamic = '4.1.+'
    static def nettyForce = '4.1.10.FINAL'
    static def nettyLock = true
    static def nettyRec = '4.1.22.FINAL'
    static def nettySubTo = 'io.netty:netty-common:4.1.23.Final'

    public static
    def lookupRequestedModuleIdentifier = ImmutableMap.of(guava, guavaDependency, mockito, mockitoDependency, netty, nettyDependency)
    public static
    def lookupDynamicResolveVersion = ImmutableMap.of(guava, guavaStatic, mockito, mockitoStatic, netty, nettyStatic)

    @Unroll
    def "#title"() {
        given:
        createSimpleBuildFile(recVersion)
        createSettingsFile()

        def dependencyHelper = new DependencyHelper()
        dependencyHelper.staticVersion = staticVersion
        dependencyHelper.dynamicVersion = dynamicVersion
        dependencyHelper.recommendedVersion = recVersion
        dependencyHelper.forceVersion = forceVersion
        dependencyHelper.useLocks = useLocks
        dependencyHelper.versionForDynamicToResolveTo = lookupDynamicResolveVersion[dep]

        dependencyHelper.requestedModuleIdentifier = dep
        dependencyHelper.replaceFrom = replaceFrom
        dependencyHelper.substituteWith = substitute
        dependencyHelper.exclude = exclude
        dependencyHelper.resolveRejectionTo = resolveRejectionTo
        dependencyHelper.lookupRequestedModuleIdentifier = lookupRequestedModuleIdentifier

        def version = dependencyHelper.findRequestedVersion() // static, dynamic, or recommended

        buildFile << 'dependencies {\n'

        if (recVersion != null) {
            buildFile << '    compile \'sample:bom:1.0.0\'\n'
        }

        buildFile << "    compile '${lookupRequestedModuleIdentifier[dep]}${version}'\n"

        if (replaceFrom != null) {
            buildFile << "    compile '$replaceFrom'\n"
        }
        buildFile << '}\n'.stripIndent()

        // add configuration
        createForceConfigurationIfNeeded(dep, forceVersion, lookupRequestedModuleIdentifier)
        createReplacementConfigurationIfNeeded(dep, replaceFrom, lookupRequestedModuleIdentifier)
        createSubstitutionConfigurationIfNeeded(dep, substitute, lookupRequestedModuleIdentifier)
        createExclusionConfigurationIfNeeded(dep, exclude, lookupRequestedModuleIdentifier)
        def rejectedVersion = resolveRejectionTo != null ? guavaReject : null
        createRejectionConfigurationIfNeeded(dep, rejectedVersion, version.replace(':', ''), lookupRequestedModuleIdentifier)

        // add additional files
        createBomIfNeeded(recVersion)
        createLocksIfNeeded(useLocks)

        createJavaSourceFile(projectDir, createMainFile())
        def tasks = tasksFor(dep)

        if (useLocks) {
            tasks << '--write-locks'
        }

        when:
        def result = runTasks(*tasks)

        then:
        DocWriter w = new DocWriter(title, projectDir, grouping)
        w.writeCleanedUpBuildOutput('=== For the dependency under test ===\n' +
                "Tasks: ${tasks.join(' ')}\n\n" +
                result.output)
        w.writeProjectFiles()

        verifyInsightOutput(result.output, dependencyHelper, dep, w)
        w.writeFooter('completed assertions')

        where:
        dep     | staticVersion | dynamicVersion           | recVersion | forceVersion | useLocks    | replaceFrom      | substitute   | exclude | resolveRejectionTo    | grouping      | title
//        static
        guava   | guavaStatic   | null                     | null       | null         | null        | null             | null         | null    | null                  | 'basic'       | 'static'
        guava   | guavaStatic   | null                     | null       | guavaForce   | null        | null             | null         | null    | null                  | 'basic'       | 'static-force'
        guava   | guavaStatic   | null                     | null       | guavaForce   | guavaLock   | null             | null         | null    | null                  | 'basic'       | 'static-force-lock'
        guava   | guavaStatic   | null                     | null       | null         | guavaLock   | null             | null         | null    | null                  | 'basic'       | 'static-lock'
//         dynamic
        guava   | null          | guavaDynamic             | null       | null         | null        | null             | null         | null    | null                  | 'basic'       | 'dynamic'
        guava   | null          | guavaDynamic             | null       | guavaForce   | null        | null             | null         | null    | null                  | 'basic'       | 'dynamic-force'
        guava   | null          | guavaDynamic             | null       | guavaForce   | guavaLock   | null             | null         | null    | null                  | 'basic'       | 'dynamic-force-lock'
        guava   | null          | guavaDynamic             | null       | null         | guavaLock   | null             | null         | null    | null                  | 'basic'       | 'dynamic-lock'
//         recommendation
        guava   | null          | null                     | guavaRec   | null         | null        | null             | null         | null    | null                  | 'basic'       | 'rec'
        guava   | null          | null                     | guavaRec   | guavaForce   | null        | null             | null         | null    | null                  | 'basic'       | 'rec-force'
        guava   | null          | null                     | guavaRec   | guavaForce   | guavaLock   | null             | null         | null    | null                  | 'basic'       | 'rec-force-lock'
        guava   | null          | null                     | guavaRec   | null         | guavaLock   | null             | null         | null    | null                  | 'basic'       | 'rec-lock'
//        replacement - static
        guava   | guavaStatic   | null                     | null       | null         | null        | guavaReplaceFrom | null         | null    | null                  | 'replacement' | 'replacement-static'
        guava   | guavaStatic   | null                     | null       | guavaForce   | null        | guavaReplaceFrom | null         | null    | null                  | 'replacement' | 'replacement-static-force'
        guava   | guavaStatic   | null                     | null       | guavaForce   | guavaLock   | guavaReplaceFrom | null         | null    | null                  | 'replacement' | 'replacement-static-force-lock'
        guava   | guavaStatic   | null                     | null       | null         | guavaLock   | guavaReplaceFrom | null         | null    | null                  | 'replacement' | 'replacement-static-lock'
//        replacement - dynamic
        guava   | null          | guavaDynamic             | null       | null         | null        | guavaReplaceFrom | null         | null    | null                  | 'replacement' | 'replacement-dynamic'
        guava   | null          | guavaDynamic             | null       | guavaForce   | null        | guavaReplaceFrom | null         | null    | null                  | 'replacement' | 'replacement-dynamic-force'
        guava   | null          | guavaDynamic             | null       | guavaForce   | guavaLock   | guavaReplaceFrom | null         | null    | null                  | 'replacement' | 'replacement-dynamic-force-lock'
        guava   | null          | guavaDynamic             | null       | null         | guavaLock   | guavaReplaceFrom | null         | null    | null                  | 'replacement' | 'replacement-dynamic-lock'
//        replacement - with recommendation
        guava   | null          | null                     | guavaRec   | null         | null        | null             | null         | null    | null                  | 'replacement' | 'replacement-rec'
        guava   | null          | null                     | guavaRec   | guavaForce   | null        | null             | null         | null    | null                  | 'replacement' | 'replacement-rec-force'
        guava   | null          | null                     | guavaRec   | guavaForce   | guavaLock   | null             | null         | null    | null                  | 'replacement' | 'replacement-rec-force-lock'
        guava   | null          | null                     | guavaRec   | null         | guavaLock   | null             | null         | null    | null                  | 'replacement' | 'replacement-rec-lock'
//        rejection - static
        guava   | guavaStatic   | null                     | null       | null         | null        | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-static'
        guava   | guavaStatic   | null                     | null       | guavaForce   | null        | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-static-force'
        guava   | guavaStatic   | null                     | null       | guavaForce   | guavaLock   | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-static-force-lock'
        guava   | guavaStatic   | null                     | null       | null         | guavaLock   | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-static-lock'
//        rejection - dynamic
        guava   | null          | guavaDynamicForRejection | null       | null         | null        | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-dynamic'
        guava   | null          | guavaDynamicForRejection | null       | guavaForce   | null        | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-dynamic-force'
        guava   | null          | guavaDynamicForRejection | null       | guavaForce   | guavaLock   | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-dynamic-force-lock'
        guava   | null          | guavaDynamicForRejection | null       | null         | guavaLock   | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-dynamic-lock'
//        rejection - with recommendation
        guava   | null          | null                     | guavaRec   | null         | null        | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-rec'
        guava   | null          | null                     | guavaRec   | guavaForce   | null        | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-rec-force'
        guava   | null          | null                     | guavaRec   | guavaForce   | guavaLock   | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-rec-force-lock'
        guava   | null          | null                     | guavaRec   | null         | guavaLock   | null             | null         | null    | guavaResolveRejection | 'reject'      | 'reject-rec-lock'
//        substitution - static
        mockito | mockitoStatic | null                     | null       | null         | null        | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-static'
        mockito | mockitoStatic | null                     | null       | mockitoForce | null        | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-static-force'
        mockito | mockitoStatic | null                     | null       | mockitoForce | mockitoLock | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-static-force-lock'
        mockito | mockitoStatic | null                     | null       | null         | mockitoLock | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-static-lock'
//        substitution - dynamic
        mockito | null          | mockitoDynamic           | null       | null         | null        | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-dynamic'
        mockito | null          | mockitoDynamic           | null       | mockitoForce | null        | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-dynamic-force'
        mockito | null          | mockitoDynamic           | null       | mockitoForce | mockitoLock | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-dynamic-force-lock'
        mockito | null          | mockitoDynamic           | null       | null         | mockitoLock | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-dynamic-lock'
//        substitution - with recommendation
        mockito | null          | null                     | mockitoRec | null         | null        | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-rec'
        mockito | null          | null                     | mockitoRec | mockitoForce | null        | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-rec-force'
        mockito | null          | null                     | mockitoRec | mockitoForce | mockitoLock | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-rec-force-lock'
        mockito | null          | null                     | mockitoRec | null         | mockitoLock | null             | mockitoSubTo | null    | null                  | 'substitute'  | 'substitute-rec-lock'
//        exclude - static
        netty   | nettyStatic   | null                     | null       | null         | null        | null             | null         | true    | null                  | 'exclude'     | 'exclude-static'
        netty   | nettyStatic   | null                     | null       | nettyForce   | null        | null             | null         | true    | null                  | 'exclude'     | 'exclude-static-force'
        netty   | nettyStatic   | null                     | null       | nettyForce   | nettyLock   | null             | null         | true    | null                  | 'exclude'     | 'exclude-static-force-lock'
        netty   | nettyStatic   | null                     | null       | null         | nettyLock   | null             | null         | true    | null                  | 'exclude'     | 'exclude-static-lock'
//        exclude - dynamic
        netty   | null          | nettyDynamic             | null       | null         | null        | null             | null         | true    | null                  | 'exclude'     | 'exclude-dynamic'
        netty   | null          | nettyDynamic             | null       | nettyForce   | null        | null             | null         | true    | null                  | 'exclude'     | 'exclude-dynamic-force'
        netty   | null          | nettyDynamic             | null       | nettyForce   | nettyLock   | null             | null         | true    | null                  | 'exclude'     | 'exclude-dynamic-force-lock'
        netty   | null          | nettyDynamic             | null       | null         | nettyLock   | null             | null         | true    | null                  | 'exclude'     | 'exclude-dynamic-lock'
//        exclude - with recommendation
        netty   | null          | null                     | nettyRec   | null         | null        | null             | null         | true    | null                  | 'exclude'     | 'exclude-rec'
        netty   | null          | null                     | nettyRec   | nettyForce   | null        | null             | null         | true    | null                  | 'exclude'     | 'exclude-rec-force'
        netty   | null          | null                     | nettyRec   | nettyForce   | nettyLock   | null             | null         | true    | null                  | 'exclude'     | 'exclude-rec-force-lock'
        netty   | null          | null                     | nettyRec   | null         | nettyLock   | null             | null         | true    | null                  | 'exclude'     | 'exclude-rec-lock'
//        exclude - static & with substitution
        netty   | nettyStatic   | null                     | null       | null         | null        | null             | nettySubTo   | true    | null                  | 'exclude'     | 'exclude-substitute-static'
        netty   | nettyStatic   | null                     | null       | nettyForce   | null        | null             | nettySubTo   | true    | null                  | 'exclude'     | 'exclude-substitute-static-force'
        netty   | nettyStatic   | null                     | null       | nettyForce   | nettyLock   | null             | nettySubTo   | true    | null                  | 'exclude'     | 'exclude-substitute-static-force-lock'
        netty   | nettyStatic   | null                     | null       | null         | nettyLock   | null             | nettySubTo   | true    | null                  | 'exclude'     | 'exclude-substitute-static-lock'
    }

    private static void verifyInsightOutput(String output, DependencyHelper dh, String dep, DocWriter w) {
        def expected = dh.results()
        def requestedVersion = dh.findRequestedVersion()

        assert expected.version != null || expected.moduleIdentifierWithVersion != null

        if (dh.exclude != null) {
            def expectedOutput = 'No dependencies matching given input were found in configuration'
            w.addAssertionToDoc("contains '$expectedOutput' [exclude]")
            assert output.contains(expectedOutput)

            // TODO: would prefer the below. See https://github.com/nebula-plugins/gradle-nebula-integration/issues/6
//            if (dh.substituteWith != null) {
//                def notFound = '✭ substitution'
//                w.addAssertionToDoc("does not contain '$notFound' [exclude > substitute]")
//                assert !output.contains(notFound)
//            }
//
//            def expectedReason = 'Selected by rule : ✭ exclusion'
//            w.addAssertionToDoc("contains '$expectedReason' [custom substitute reason]")
//            assert output.contains(expectedReason)

            return // if exclude occurs, stop checking here
        }

        if (dh.resolveRejectionTo) {
            if (dh.dynamicVersion != null) {
                def expectedFinalVersion = "${lookupRequestedModuleIdentifier[dep]}:${dh.dynamicVersion} -> ${expected.version}"
                w.addAssertionToDoc("contains '$expectedFinalVersion' [substitute & static]")
                assert output.contains(expectedFinalVersion)
            }
        }

        if (dh.substituteWith != null) {
            def expectedReason = 'Selected by rule : ✭ substitution'
            w.addAssertionToDoc("contains '$expectedReason' [custom substitute reason]")
            assert output.contains(expectedReason)

            if (dh.staticVersion != null) {
                def expectedFinalVersion = "${lookupRequestedModuleIdentifier[dep]}:${dh.staticVersion} -> ${expected}"
                w.addAssertionToDoc("contains '$expectedFinalVersion' [substitute & static]")
                assert output.contains(expectedFinalVersion)

            } else if (dh.recommendedVersion != null) {
                def expectedFinalVersion = "${lookupRequestedModuleIdentifier[dep]} -> ${expected}"
                w.addAssertionToDoc("contains '$expectedFinalVersion' [substitute & recommended]")
                assert output.contains(expectedFinalVersion)

                def expectedOutput = 'By conflict resolution : between versions '
                w.addAssertionToDoc("contains '$expectedOutput' [substitute & recommended]")
                assert output.contains(expectedOutput)

                def bomDependencyConstraint = '\\--- sample:bom:1.0.0'
                w.addAssertionToDoc("contains '$bomDependencyConstraint' [bom dependency constraint - recommended]")
                assert output.contains(bomDependencyConstraint)

            } else {
                def expectedFinalVersion = "${lookupRequestedModuleIdentifier[dep]}:${dh.dynamicVersion} -> ${expected}"
                w.addAssertionToDoc("contains '$expectedFinalVersion' [substitute & dynamic]")
                assert output.contains(expectedFinalVersion)
            }

            if (dh.useLocks) {
                def expectedOutput = 'Persisted dependency lock state'
                w.addAssertionToDoc("contains '$expectedOutput' [locked & build matches lock version]")
                assert output.contains(expectedOutput)
            }

            def forceIsAppliedToSubstitutedModule = expected.toString().contains("${lookupRequestedModuleIdentifier[dep]}")
            if (dh.forceVersion != null && forceIsAppliedToSubstitutedModule) {
                // TODO: create this example
                w.addAssertionToDoc("contains 'forced/Forced'")
                assert output.toLowerCase().contains('forced')
            }

            if (dh.useLocks) {
                def endResultRegex = "Task.*\n.*\n.*$expected"
                w.addAssertionToDoc("contains '$endResultRegex' [substitute end result]")
                assert output.findAll(endResultRegex).size() > 0
            } else {
                def endResultRegex = "Task.*\n.*$expected"
                w.addAssertionToDoc("contains '$endResultRegex' [substitute end result]")
                assert output.findAll(endResultRegex).size() > 0
            }

            return // if substitution occurs, stop checking here
        }

        if (dh.useLocks != null) {
            def expectedOutput = 'Persisted dependency lock state'
            w.addAssertionToDoc("contains '$expectedOutput' [locked & build matches lock version]")
            assert output.contains(expectedOutput)
        }

        if (dh.forceVersion != null) {
            w.addAssertionToDoc("contains 'forced/Forced'")
            assert output.toLowerCase().contains('forced')
        }

        if (dh.forceVersion != null && dh.useLocks == null) {
            def expectedOutput = "${expected.moduleIdentifier}${requestedVersion} -> ${expected.version}"
            w.addAssertionToDoc("contains '$expectedOutput' [forced and not locked]")
            assert output.contains(expectedOutput)
        }

        if (dh.replaceFrom != null) {
            def expectedReason = 'Selected by rule : ✭ replacement'
            w.addAssertionToDoc("contains '$expectedReason' [custom replacement reason]")
            assert output.contains(expectedReason)

            def expectedRegex = "${dh.replaceFrom} -> ${expected}"
            w.addAssertionToDoc("contains '$expectedRegex' [replacement end result]")
            assert output.contains(expectedRegex)
        }

        if (dh.staticVersion != null) {
            if (dh.useLocks) {
                def endResultRegex = "Task.*\n.*\n.*$expected"
                w.addAssertionToDoc("contains '$endResultRegex' [static end result]")
                assert output.findAll(endResultRegex).size() > 0
            } else {
                def endResultRegex = "Task.*\n.*$expected"
                w.addAssertionToDoc("contains '$endResultRegex' [static end result]")
                assert output.findAll(endResultRegex).size() > 0
            }
        }

        if (dh.dynamicVersion != null) {
            def expectedOutput = "${expected.moduleIdentifier}:$dh.dynamicVersion -> ${expected.version}"
            w.addAssertionToDoc("contains '$expectedOutput' [dynamic]")
            assert output.contains(expectedOutput)

            if (dh.useLocks) {
                def endResultRegex = "Task.*\n.*\n.*$expected"
                w.addAssertionToDoc("contains '$endResultRegex' [dynamic end result]")
                assert output.findAll(endResultRegex).size() > 0
            } else {
                def endResultRegex = "Task.*\n.*$expected"
                w.addAssertionToDoc("contains '$endResultRegex' [dynamic end result]")
                assert output.findAll(endResultRegex).size() > 0
            }
        }

        if (dh.recommendedVersion != null) {
            def expectedOutput = "${expected.moduleIdentifier} -> ${expected.version}"
            w.addAssertionToDoc("contains '$expectedOutput' [recommended]")
            assert output.contains(expectedOutput)

            if (dh.useLocks) {
                def endResultRegex = "Task.*\n.*\n.*$expected"
                w.addAssertionToDoc("contains '$endResultRegex' [recommended end result]")
                assert output.findAll(endResultRegex).size() > 0
            } else {
                def endResultRegex = "Task.*\n.*$expected"
                w.addAssertionToDoc("contains '$endResultRegex' [recommended end result]")
                assert output.findAll(endResultRegex).size() > 0
            }

            def bomDependencyConstraint = '\\--- sample:bom:1.0.0'
            w.addAssertionToDoc("contains '$bomDependencyConstraint' [bom dependency constraint - recommended]")
            assert output.findAll(bomDependencyConstraint).size() > 0
        }

        if (dh.resolveRejectionTo) {
            if (dh.dynamicVersion != null) {
                // TODO: would prefer this to show for all See https://github.com/nebula-plugins/gradle-nebula-integration/issues/5
                if (dh.forceVersion == null) {
                    // TODO: in the same vein, would prefer this to show even though it is overridden
                    def expectedReason = '✭ rejection'
                    w.addAssertionToDoc("contains '$expectedReason' [custom substitute reason]")
                    assert output.contains(expectedReason)
                }
            }
        }
    }
}
