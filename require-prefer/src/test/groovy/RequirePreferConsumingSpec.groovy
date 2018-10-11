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

class RequirePreferConsumingSpec extends AbstractRequirePreferSpec {
    public static final String MAVEN_POM = 'mavenPom'
    public static final String IVY_DESCRIPTOR = 'ivyDescriptor'
    public static final String GRADLE_METADATA = 'gradleMetadata'
    DocWriter docWriter
    def publishingUrlLookup

    def setup() {
        publishingUrlLookup = ImmutableMap.of(
                'maven', projectDir.absolutePath + File.separator + 'maven-repo',
                'ivy', projectDir.absolutePath + File.separator + 'ivy-repo'
        )
    }

    @Unroll
    def "consuming published dependencies from #publishingWith with #metadataSource metadata using #whichBlock block"() {
        given:
        def enableGradleMetadata = metadataSource == GRADLE_METADATA
        docWriter = new DocWriter(methodName, projectDir, 'consuming')

        createTransitiveDependencies(publishingWith, enableGradleMetadata)
        createDirectDependency(publishingWith, prefer, require, requireAndPreferInSameDependencyBlock, metadataSource, enableGradleMetadata)
        createBuildFile(publishingWith, metadataSource, enableGradleMetadata)

        when:
        def insightResultForConsumer = runTasks('dependencyInsight', '--dependency', dep)

        def outputToWrite = "Dependency insight:\n\n${insightResultForConsumer.output}"
        writeRelevantOutput(docWriter, outputToWrite, prefer, null)

        File publishedMetadata = metadataSource == GRADLE_METADATA ?
                publishedGradleMetadata(projectDir, publishingWith, first, publishedVersion) :
                publishedIvyOrMavenMetadata(projectDir, publishingWith, first, publishedVersion)

        then:
        !insightResultForConsumer.output.contains('FAILED')

        docWriter.addAssertionToDoc("Published first order dependency $publishingWith metadata contains prefer version '$prefer'")
        assert publishedMetadata.text.contains(prefer)

        docWriter.addAssertionToDoc("Transitive dependency version resolves to preferred version in first order dependency: '$prefer'")
        assert insightResultForConsumer.output.contains(prefer)

        docWriter.writeFooter('completed assertions')

        where:
        publishingWith | prefer | require      | requireAndPreferInSameDependencyBlock | metadataSource  | whichBlock
        // same dependency blocks
        'maven'        | '1.5'  | '[1.2, 2.0)' | false                                 | MAVEN_POM       | 'different'
        'ivy'          | '1.5'  | '[1.2, 2.0)' | false                                 | IVY_DESCRIPTOR  | 'different'
        'maven'        | '1.5'  | '[1.2, 2.0)' | false                                 | GRADLE_METADATA | 'different'
        'ivy'          | '1.5'  | '[1.2, 2.0)' | false                                 | GRADLE_METADATA | 'different'
//        // different dependency blocks // TODO: commented out for now, until nebula-plugins/gradle-nebula-integration#44 (gradle/gradle#7046) is resolved
//        'maven'        | '1.5'  | '[1.2, 2.0)' | true                                  | MAVEN_POM       | 'same'
//        'ivy'          | '1.5'  | '[1.2, 2.0)' | true                                  | IVY_DESCRIPTOR  | 'same'
//        'maven'        | '1.5'  | '[1.2, 2.0)' | true                                  | GRADLE_METADATA | 'same'
//        'ivy'          | '1.5'  | '[1.2, 2.0)' | true                                  | GRADLE_METADATA | 'same'
    }

    private void createDirectDependency(String publishingWith, String prefer, String require, boolean requireAndPreferInSameDependencyBlock,
                                        String metadataSource, boolean enableGradleMetadata) {
        createPublishingBuildAndSettingsFile(publishedVersion, publishingWith, first, enableGradleMetadata)
        addDependencyBlockToBuildFile(publishingWith, dep, prefer, require, requireAndPreferInSameDependencyBlock, metadataSource)

        def insightResultForProducer = runTasks('dependencyInsight', '--dependency', dep)
        def publishResult = runTasks('publish')
        docWriter.writeProjectFiles('direct-dependency', true)

        assert !insightResultForProducer.output.contains('FAILED')
        assert !publishResult.output.contains('FAILED')
        buildFile.delete()
        buildFile.createNewFile()
        settingsFile.delete()
        settingsFile.createNewFile()
    }

    private void addDependencyBlockToBuildFile(String publishingWith, String dependency, String prefer, String require,
                                               boolean sameDependencyBlock, String metadataSource) {
        String url = publishingUrlLookup[publishingWith]

        def requireBlock = ''
        def requireStatement = ''
        if (require != null) {
            if (sameDependencyBlock) {
                requireStatement = "\n            require '$require'"
            } else {
                requireBlock = """
    api ('$group:$dep') {
        version {
            require '${require}'
        }
    }"""
            }
        }

        buildFile << """
dependencies {
    api ('$group:$dependency') {
        version {
            prefer '${prefer}'$requireStatement
        }
    }
    $requireBlock
}
repositories {
    $publishingWith { 
        url { '$url' }
        metadataSources { ${metadataSource}() } 
    }
}
"""

    }

    private void createBuildFile(String publishingWith, String metadataSource, boolean enableGradleMetadata) {
        def url = publishingUrlLookup[publishingWith]

        buildFile << """
plugins {
    id 'java-library'
}

group '$group'
version '1.0'

dependencies {
    api '$group:$first:$publishedVersion'
}

repositories {
    $publishingWith { 
        url { '$url' }
        metadataSources { ${metadataSource}() } 
    }
}
"""
        settingsFile << "rootProject.name='$second'\n"
        if (enableGradleMetadata) {
            settingsFile << 'enableFeaturePreview(\'GRADLE_METADATA\')'
        }
    }

    private void createTransitiveDependencies(String publishingWith, boolean enableGradleMetadata) {
        def major = 1
        for (def minor in 0..9) {
            String version = "${major}.${minor}"

            createPublishingBuildAndSettingsFile(version, publishingWith, dep, enableGradleMetadata)

            when:
            def insightResultForBaseDep = runTasks('dependencyInsight', '--dependency', dep)
            def publishingResult = runTasks('publish')

            if (version == '1.0') {
                docWriter.writeProjectFiles('sample-transitive-dep', true)
            }

            then:
            assert !insightResultForBaseDep.output.contains('FAILED')
            assert !publishingResult.output.contains('FAILED')
            buildFile.delete()
            buildFile.createNewFile()
            settingsFile.delete()
            settingsFile.createNewFile()
        }
    }

    def createPublishingBuildAndSettingsFile(String version, String publishingWith, String artifactName, boolean enableGradleMetadata) {
        String url = publishingUrlLookup[publishingWith]

        def publicationBlock
        if (publishingWith == 'maven') {
            publicationBlock = """
        maven(MavenPublication) {
            groupId = '$group'
            artifactId = '${artifactName}'
            version '$version'
            from components.java
        }"""
        } else {
            publicationBlock = """
        ivy(IvyPublication) {
            from components.java
        }"""
        }

        def mavenMetadataFile = publishedIvyOrMavenMetadata(projectDir, 'maven', artifactName, version)
        def ivyMetadataFile = publishedIvyOrMavenMetadata(projectDir, 'ivy', artifactName, version)
        if (!mavenMetadataFile.exists() && !ivyMetadataFile.exists()) {
            buildFile << """
plugins {
    id 'java-library'
    id '$publishingWith-publish'
}
group '$group'
version '$version'
publishing {
    repositories {
        $publishingWith { url '$url' }
    }
    publications {
        $publicationBlock  
    }
}
"""
            settingsFile << "rootProject.name='${artifactName}'\n"
            if (enableGradleMetadata) {
                settingsFile << 'enableFeaturePreview(\'GRADLE_METADATA\')'
            }
        }
    }

}
