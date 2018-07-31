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
}
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

    def createForceConfigurationIfNeeded(String dep, String forceVersion, Map lookupRequestedModuleIdentifier) {
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

    def createBomIfNeeded(String recVersion) {
        if (recVersion != null) {
            repo.mkdirs()

            def localBom = new Pom('sample', 'bom', '1.0.0', ArtifactType.POM)
            localBom.addManagementDependency('com.google.guava', 'guava', '19.0')
            localBom.addManagementDependency('org.slf4j', 'slf4j-api', '1.7.25')
            localBom.addManagementDependency('org.mockito', 'mockito-core', '1.9.5')
            ArtifactHelpers.setupSamplePomWith(repo, localBom, localBom.generate())
        }
    }
}