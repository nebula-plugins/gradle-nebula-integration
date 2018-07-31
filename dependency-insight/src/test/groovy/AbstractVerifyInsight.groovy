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
import nebula.test.dependencies.maven.ArtifactType
import nebula.test.dependencies.maven.Pom

import java.time.LocalDateTime

abstract class AbstractVerifyInsight extends TestKitSpecification {
    static def coreGradleV = '4.9' // gradle v

    File repo

    def setup() {
        repo = new File(projectDir, 'repo')
    }

    def cleanupSpec() {
        def file = new File("docs", "lastUpdated.txt")
        file.delete()
        file.createNewFile()
        file << "Last updated at: ${LocalDateTime.now()}"
    }

    def tasksFor(String dependencyName) {
        ['dependencyInsight', '--dependency', "${dependencyName}", '--warning-mode', 'all']
    }

    void createSimpleBuildFile(String recVersion) {
        def recRepo = recVersion != null ? '\n    maven { url \'repo\' }' : ''

        buildFile <<
                """apply plugin: 'java'

repositories {
    jcenter()$recRepo
}\n
""".stripIndent()
    }

    String createMainFile() {
        """
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Main {
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(Main.class);
        log.info("Hello, " + ImmutableList.of("friend").get(0));
    }
}
""".stripIndent()
    }

    void createSettingsFile() {
        settingsFile << 'enableFeaturePreview(\'IMPROVED_POM_SUPPORT\')\n'
    }

    void createLocksIfNeeded(Boolean lockVersion) {
        if (lockVersion) {

            buildFile << """
                dependencyLocking {
                    lockAllConfigurations()
                }
                """.stripIndent()
        }
    }

    def createBomIfNeeded(String recVersion) {
        if (recVersion != null) {
            repo.mkdirs()

            def localBom = new Pom('sample', 'bom', '1.0.0', ArtifactType.POM)
            localBom.addManagementDependency('com.google.guava', 'guava', '19.0')
            localBom.addManagementDependency('org.slf4j', 'slf4j-api', '1.7.25')
            localBom.addManagementDependency('org.mockito', 'mockito-core', '1.9.5')
            localBom.addManagementDependency('io.netty', 'netty-all', '4.1.22.FINAL')
            ArtifactHelpers.setupSamplePomWith(repo, localBom, localBom.generate())
        }
    }

    def createForceConfigurationIfNeeded(String dep, String forceVersion, ImmutableMap<String, String> lookupRequestedModuleIdentifier) {
        if (forceVersion != null) {
            buildFile << """
                configurations.all {
                    resolutionStrategy {
                        force '${"${lookupRequestedModuleIdentifier[dep]}:${forceVersion}"}'
                    }
                }
                """.stripIndent()
        }
    }

    def createSubstitutionConfigurationIfNeeded(String dep, String substituteTo, ImmutableMap<String, String> lookupRequestedModuleIdentifier) {
        if (substituteTo != null) {
            def substituteFromModuleIdentifier = lookupRequestedModuleIdentifier[dep]
            buildFile << """
                def substitutionMessage = '✭ substitution $substituteFromModuleIdentifier -> $substituteTo'
                configurations.all {
                    resolutionStrategy.dependencySubstitution {
                        substitute module('$substituteFromModuleIdentifier') because (substitutionMessage) with module('$substituteTo')
                    }
                }
                """.stripIndent()
        }
    }

    def createExclusionConfigurationIfNeeded(String dep, Boolean exclude, ImmutableMap<String, String> lookupRequestedModuleIdentifier) {
        if (exclude) {
            def excludeDepModuleIdentifier = lookupRequestedModuleIdentifier[dep]
            def depGroupAndArtifact = excludeDepModuleIdentifier.split(':')

            buildFile << """
                def exclusionMessage = '✭ exclusion $excludeDepModuleIdentifier'
                configurations.all {
                    exclude group: '${depGroupAndArtifact[0]}', module: '${depGroupAndArtifact[1]}'
                }
                """.stripIndent()
        }
    }

    def createRejectionConfigurationIfNeeded(String dep, String rejectedVersion, String candidateVersion, ImmutableMap<String, String> lookupRequestedModuleIdentifier) {
        if (rejectedVersion != null) {
            def excludeDepModuleIdentifier = lookupRequestedModuleIdentifier[dep]
            def depGroupAndArtifact = excludeDepModuleIdentifier.split(':')
            def group = depGroupAndArtifact[0]
            def artifact = depGroupAndArtifact[1]

            buildFile << """
                import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.DefaultVersionSelectorScheme
                import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.DefaultVersionComparator
                configurations.all {
                    resolutionStrategy.componentSelection.all({ selection ->
                        def candidate = selection.candidate
                        def rejectionMessage = '✭ rejection of com.google.guava:guava:$rejectedVersion'
                        if (candidate.group == '${group}' && candidate.module == '${artifact}') {
                            def comparator = new DefaultVersionComparator()
                            def scheme = new DefaultVersionSelectorScheme(comparator)

                            def versionSelector = scheme.parseSelector('$rejectedVersion')
                            if (candidate.version == null || candidate.version == '' || versionSelector.accept(candidate.version)) {
                                selection.reject(rejectionMessage)
                            }
                        }
                    })
                }
                """.stripIndent()
        }
    }

    def createReplacementConfigurationIfNeeded(String dep, Coordinate replaceFrom, ImmutableMap<String, String> lookupRequestedModuleIdentifier) {
        if (replaceFrom != null) {
            def replaceToModuleIdentifier = lookupRequestedModuleIdentifier[dep]
            buildFile << """
                project.dependencies.modules.module('${replaceFrom.moduleIdentifier}') {
                    def details = it as ComponentModuleMetadataDetails
                    def message = "✭ replacement ${replaceFrom.moduleIdentifier} -> ${replaceToModuleIdentifier}"
                    details.replacedBy('${replaceToModuleIdentifier}', message)
                }
                """.stripIndent()
        }
    }
}