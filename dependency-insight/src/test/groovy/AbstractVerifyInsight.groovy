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

abstract class AbstractVerifyInsight extends TestKitSpecification {
    File repo

    def setup() {
        repo = new File(projectDir, 'repo')
    }

    def cleanupSpec() {
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
        settingsFile << '\n'
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

    def createBomIfNeeded(Map<String, String> dependencies) {
        if (dependencies.size() != 0) {
            repo.mkdirs()
            def localBom = new Pom('sample', 'bom', '1.0.0', ArtifactType.POM)

            dependencies.each { moduleIdentifier, recVersion ->
                def split = moduleIdentifier.split(':')
                String group = split[0]
                String artifact = split[1]

                localBom.addManagementDependency(group, artifact, recVersion)
            }

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

    def createDependencySubstitutionConfigurationIfNeeded(String dep, String substituteTo, ImmutableMap<String, String> lookupRequestedModuleIdentifier) {
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

    def createEachDepSubstitutionConfigurationIfNeeded(String dep, String eachDepSubstituteTo, ImmutableMap<String, String> lookupRequestedModuleIdentifier) {
        // based off of https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/ResolutionStrategy.html#eachDependency-org.gradle.api.Action-
        // and https://github.com/gradle/gradle/blob/master/subprojects/dependency-management/src/main/java/org/gradle/api/internal/artifacts/ivyservice/dependencysubstitution/DefaultDependencyResolveDetails.java

        if (eachDepSubstituteTo != null) {
            def substituteFromModuleIdentifier = lookupRequestedModuleIdentifier[dep]
            def splitModuleIdentifier = substituteFromModuleIdentifier.split(':')

            buildFile << """
                def substitutionEachDependencyMessage = "✭ substitution for each dependency with group '${
                splitModuleIdentifier[0]
            }' to version '${eachDepSubstituteTo}'"
                configurations.all {
                    resolutionStrategy.eachDependency { DependencyResolveDetails details ->
                        if (details.requested.group == '${splitModuleIdentifier[0]}') {
                            details.useVersion '${eachDepSubstituteTo}'
                            details.because(substitutionEachDependencyMessage) 
                        }
                    }
                }
                """.stripIndent()
            // could also use something like
            // details.useTarget group: '${splitEachDepSubstituteTo[0]}', name: details.requested.name, version: '${splitEachDepSubstituteTo[2]}'
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

    def createAlignmentConfiguration(String dep, ImmutableMap<String, String> lookupRequestedModuleIdentifier) {
        def alignmentModuleIdentifier = lookupRequestedModuleIdentifier[dep]
        def depGroupAndArtifact = alignmentModuleIdentifier.split(':')
        def group = depGroupAndArtifact[0]
        def platform = group.split('\\.').last() + '-platform'

        buildFile << """
            project.dependencies.components.all(AlignGroup.class)\n
            class AlignGroup implements ComponentMetadataRule {
                void execute(ComponentMetadataContext ctx) {
                    ctx.details.with { it ->
                        if (it.getId().getGroup().startsWith("$group")) {
                            it.belongsTo("$group:$platform:\${it.getId().getVersion()}")
                        }
                    }
                }
            }
            """.stripIndent()
    }
}