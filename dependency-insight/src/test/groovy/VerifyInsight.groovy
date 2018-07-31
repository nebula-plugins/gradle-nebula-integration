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

    static def mockito = 'mockito'
    static def mockitoDependency = 'org.mockito:mockito-all'
    static def mockitoStatic = '1.8.0'
    static def mockitoDynamic = '1.8.+'
    static def mockitoForce = '1.10.17'
    static def mockitoLock = '2.1.0'
    static def mockitoRec = '1.9.5'
    static def mockitoSubTo = 'org.mockito:mockito-core:1.10.19'

    static def netty = 'netty'
    static def nettyDependency = 'io.netty:netty-all'
    static def nettyStatic = '4.1.20.FINAL'
    static def nettyDynamic = '4.1.+'
    static def nettyForce = '4.1.10.FINAL'
    static def nettyLock = '4.1.15.FINAL'
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
        gradleVersion = coreGradleV
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

        createForceConfigurationIfNeeded(dep, forceVersion, lookupRequestedModuleIdentifier)
        createReplacementConfigurationIfNeeded(dep, replaceFrom, lookupRequestedModuleIdentifier)
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
        DocWriter w = new DocWriter(title, coreGradleV, projectDir, group)
        w.writeCleanedUpBuildOutput('=== For the dependency under test ===\n' +
                "Tasks: ${tasks.join(' ')}\n\n" +
                result.output)
        w.writeProjectFiles()

        verifyInsightOutput(result.output, dependencyHelper, dep, w)
        w.writeFooter('completed assertions')

        where:
        dep   | staticVersion | dynamicVersion | recVersion | forceVersion | useLocks  | replaceFrom      | substitute | exclude | group         | title
//        static
        guava | guavaStatic   | null           | null       | null         | null      | null             | null       | null    | 'basic'       | 'static'
        guava | guavaStatic   | null           | null       | guavaForce   | null      | null             | null       | null    | 'basic'       | 'static-force'
        guava | guavaStatic   | null           | null       | guavaForce   | guavaLock | null             | null       | null    | 'basic'       | 'static-force-lock'
        guava | guavaStatic   | null           | null       | null         | guavaLock | null             | null       | null    | 'basic'       | 'static-lock'
//         dynamic
        guava | null          | guavaDynamic   | null       | null         | null      | null             | null       | null    | 'basic'       | 'dynamic'
        guava | null          | guavaDynamic   | null       | guavaForce   | null      | null             | null       | null    | 'basic'       | 'dynamic-force'
        guava | null          | guavaDynamic   | null       | guavaForce   | guavaLock | null             | null       | null    | 'basic'       | 'dynamic-force-lock'
        guava | null          | guavaDynamic   | null       | null         | guavaLock | null             | null       | null    | 'basic'       | 'dynamic-lock'
//         recommendation
        guava | null          | null           | guavaRec   | null         | null      | null             | null       | null    | 'basic'       | 'rec'
        guava | null          | null           | guavaRec   | guavaForce   | null      | null             | null       | null    | 'basic'       | 'rec-force'
        guava | null          | null           | guavaRec   | guavaForce   | guavaLock | null             | null       | null    | 'basic'       | 'rec-force-lock'
        guava | null          | null           | guavaRec   | null         | guavaLock | null             | null       | null    | 'basic'       | 'rec-lock'
//        replacement - static
        guava | guavaStatic   | null           | null       | null         | null      | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-static'
        guava | guavaStatic   | null           | null       | guavaForce   | null      | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-static-force'
        guava | guavaStatic   | null           | null       | guavaForce   | guavaLock | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-static-force-lock'
        guava | guavaStatic   | null           | null       | null         | guavaLock | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-static-lock'
//        replacement - dynamic
        guava | null          | guavaDynamic   | null       | null         | null      | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-dynamic'
        guava | null          | guavaDynamic   | null       | guavaForce   | null      | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-dynamic-force'
        guava | null          | guavaDynamic   | null       | guavaForce   | guavaLock | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-dynamic-force-lock'
        guava | null          | guavaDynamic   | null       | null         | guavaLock | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-dynamic-lock'
//        replacement - with recommendation
        guava | null          | null           | guavaRec   | null         | null      | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-rec'
        guava | null          | null           | guavaRec   | guavaForce   | null      | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-rec-force'
        guava | null          | null           | guavaRec   | guavaForce   | guavaLock | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-rec-force-lock'
        guava | null          | null           | guavaRec   | null         | guavaLock | guavaReplaceFrom | null       | null    | 'replacement' | 'replacement-rec-lock'
////        substitution - static
//        mockito | mockitoStatic | null           | null       | null         | null        | null             | mockitoSubTo | null    | 'substitute' | 'substitute-static'
//        mockito | mockitoStatic | null           | null       | mockitoForce | null        | null             | mockitoSubTo | null    | 'substitute' | 'substitute-static-force'
//        mockito | mockitoStatic | null           | null       | mockitoForce | mockitoLock | null             | mockitoSubTo | null    | 'substitute' | 'substitute-static-force-lock'
//        mockito | mockitoStatic | null           | null       | null         | mockitoLock | null             | mockitoSubTo | null    | 'substitute' | 'substitute-static-lock'
////        substitution - dynamic
//        mockito | null          | mockitoDynamic | null       | null         | null        | null             | mockitoSubTo | null    | 'substitute' | 'substitute-dynamic'
//        mockito | null          | mockitoDynamic | null       | mockitoForce | null        | null             | mockitoSubTo | null    | 'substitute' | 'substitute-dynamic-force'
//        mockito | null          | mockitoDynamic | null       | mockitoForce | mockitoLock | null             | mockitoSubTo | null    | 'substitute' | 'substitute-dynamic-force-lock'
//        mockito | null          | mockitoDynamic | null       | null         | mockitoLock | null             | mockitoSubTo | null    | 'substitute' | 'substitute-dynamic-lock'
////        substitution - with recommendation
//        mockito | null          | null           | mockitoRec | null         | null        | null             | mockitoSubTo | null    | 'substitute' | 'substitute-rec'
//        mockito | null          | null           | mockitoRec | mockitoForce | null        | null             | mockitoSubTo | null    | 'substitute' | 'substitute-rec-force'
//        mockito | null          | null           | mockitoRec | mockitoForce | mockitoLock | null             | mockitoSubTo | null    | 'substitute' | 'substitute-rec-force-lock'
//        mockito | null          | null           | mockitoRec | null         | mockitoLock | null             | mockitoSubTo | null    | 'substitute' | 'substitute-rec-lock'
////        exclude - static
//        netty   | nettyStatic   | null           | null       | null         | null        | null             | null         | true    | 'exclude' | 'exclude-static'
//        netty   | nettyStatic   | null           | null       | nettyForce   | null        | null             | null         | true    | 'exclude' | 'exclude-static-force'
//        netty   | nettyStatic   | null           | null       | nettyForce   | nettyLock   | null             | null         | true    | 'exclude' | 'exclude-static-force-lock'
//        netty   | nettyStatic   | null           | null       | null         | nettyLock   | null             | null         | true    | 'exclude' | 'exclude-static-lock'
////        exclude - dynamic
//        netty   | null          | nettyDynamic   | null       | null         | null        | null             | null         | true    | 'exclude' | 'exclude-dynamic'
//        netty   | null          | nettyDynamic   | null       | nettyForce   | null        | null             | null         | true    | 'exclude' | 'exclude-dynamic-force'
//        netty   | null          | nettyDynamic   | null       | nettyForce   | nettyLock   | null             | null         | true    | 'exclude' | 'exclude-dynamic-force-lock'
//        netty   | null          | nettyDynamic   | null       | null         | nettyLock   | null             | null         | true    | 'exclude' | 'exclude-dynamic-lock'
////        exclude - with recommendation
//        netty   | null          | null           | nettyRec   | null         | null        | null             | null         | true    | 'exclude' | 'exclude-rec'
//        netty   | null          | null           | nettyRec   | nettyForce   | null        | null             | null         | true    | 'exclude' | 'exclude-rec-force'
//        netty   | null          | null           | nettyRec   | nettyForce   | nettyLock   | null             | null         | true    | 'exclude' | 'exclude-rec-force-lock'
//        netty   | null          | null           | nettyRec   | null         | nettyLock   | null             | null         | true    | 'exclude' | 'exclude-rec-lock'
////        exclude - static & with substitution
//        netty   | nettyStatic   | null           | null       | null         | null        | null             | nettySubTo   | true    | 'exclude' | 'exclude-substitute-static'
//        netty   | nettyStatic   | null           | null       | nettyForce   | null        | null             | nettySubTo   | true    | 'exclude' | 'exclude-substitute-static-force'
//        netty   | nettyStatic   | null           | null       | nettyForce   | nettyLock   | null             | nettySubTo   | true    | 'exclude' | 'exclude-substitute-static-force-lock'
//        netty   | nettyStatic   | null           | null       | null         | nettyLock   | null             | nettySubTo   | true    | 'exclude' | 'exclude-substitute-static-lock'

    }

    private static void verifyInsightOutput(String output, DependencyHelper dh, String dep, DocWriter w) {
        def expected = dh.results()
        def requestedVersion = dh.findRequestedVersion()

        assert expected.version != null || expected.moduleIdentifierWithVersion != null

        if (dh.exclude != null) {
            def expectedOutput = 'No dependencies matching given input were found in configuration'
            w.addAssertionToDoc("contains '$expectedOutput' [exclude]")
            assert output.contains(expectedOutput)
            return // if exclude occurs, stop checking here
        }

        if (dh.substituteWith != null) {
            if (dh.staticVersion != null) {
                def expectedOutput = "${lookupRequestedModuleIdentifier[dep]}:${dh.staticVersion} -> ${expected}"
                w.addAssertionToDoc("contains '$expectedOutput' [substitute & static]")
                assert output.contains(expectedOutput)

            } else if (dh.recommendedVersion != null) {
                def expectedOutput = "${lookupRequestedModuleIdentifier[dep]} -> ${expected}"
                w.addAssertionToDoc("contains '$expectedOutput' [substitute & recommended]")
                assert output.contains(expectedOutput)

            } else {
                def expectedOutput = "${lookupRequestedModuleIdentifier[dep]}:${dh.dynamicVersion} -> ${expected}"
                w.addAssertionToDoc("contains '$expectedOutput' [substitute & dynamic]")
                assert output.contains(expectedOutput)
            }

            if (output.contains('locked')) { // whole substitutions supercede locks of substituted dependency
                w.addAssertionToDoc("contains 'nebula-dependency-lock tag line if \"locked\" is in output' [substitute]")
                assert output.contains('nebula.dependency-lock locked with: dependencies.lock')
            } else {
                w.addAssertionToDoc("does not contain 'locked' [substitute]")
            }

            w.addAssertionToDoc("does not contain 'forced/Forced' [substitute]")
            assert !output.toLowerCase().contains('forced')
            // whole substitutions supercede forces of substituted dependency

            def endResultRegex = "Task.*\n.*$expected"
            w.addAssertionToDoc("contains '$endResultRegex' [substitute end result]")
            assert output.findAll { endResultRegex }.size() > 0

            return // if substitution occurs, stop checking here
        }

        if (dh.useLocks != null) {
            def expectedOutput = 'Persisted dependency lock state'
            w.addAssertionToDoc("contains '$expectedOutput' [locked & build matches lock version]")
            assert output.contains(expectedOutput)
        }

        if (dh.forceVersion != null) {
            // FIXME: currently locked and forced go only down the 'locked' route of assertions
            w.addAssertionToDoc("contains 'forced/Forced'")
            assert output.toLowerCase().contains('forced')
        }

        if (dh.forceVersion != null && dh.useLocks == null) {
            def expectedOutput = "${expected.moduleIdentifier}${requestedVersion} -> ${expected.version}"
            w.addAssertionToDoc("contains '$expectedOutput' [forced and not locked]")
            assert output.contains(expectedOutput)
        }

        if (dh.replaceFrom != null) {
            def expectedOutput = 'Selected by rule : replacement'
            w.addAssertionToDoc("contains '$expectedOutput' [replacement]")
            assert output.contains(expectedOutput)

            def expectedRegex = "${dh.replaceFrom} -> ${expected}"
            w.addAssertionToDoc("contains '$expectedRegex' [replacement end result]")
            assert output.contains(expectedRegex)
        }

        if (dh.staticVersion != null) {
            def endResultRegex = "Task.*\n.*$expected"
            w.addAssertionToDoc("contains '$endResultRegex' [static version end result]")
            assert output.findAll { endResultRegex }.size() > 0
        }

        if (dh.dynamicVersion != null) {
            def expectedOutput = "${expected.moduleIdentifier}:$dh.dynamicVersion -> ${expected.version}"
            w.addAssertionToDoc("contains '$expectedOutput' [dynamic]")
            assert output.contains(expectedOutput)
//            assert output.contains('Was requested')

            def endResultRegex = "Task.*\n.*$expected"
            w.addAssertionToDoc("contains '$endResultRegex' [dynamic version end result]")
            assert output.findAll { endResultRegex }.size() > 0
        }

        if (dh.recommendedVersion != null) {
//                assert output.contains("Recommending version ${m.recommendedVersion} for dependency")
            def expectedOutput = "${expected.moduleIdentifier} -> ${expected.version}"
            w.addAssertionToDoc("contains '$expectedOutput' [recommended]")
            assert output.contains(expectedOutput)

            def endResultRegex = "Task.*\n.*$expected"
            w.addAssertionToDoc("contains '$endResultRegex' [recommended end result]")
            assert output.findAll { endResultRegex }.size() > 0

            def bomDependencyConstraint = '\\--- sample:bom:1.0.0'
            w.addAssertionToDoc("contains '$bomDependencyConstraint' [bom dependency constraint - recommended]")
            assert output.findAll { bomDependencyConstraint }.size() > 0
        }
    }
}
