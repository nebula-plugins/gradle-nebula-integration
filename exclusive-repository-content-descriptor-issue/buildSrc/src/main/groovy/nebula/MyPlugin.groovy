package nebula

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.ExclusiveContentRepository
import org.gradle.api.artifacts.repositories.InclusiveRepositoryContentDescriptor
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor

class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        createMaven(project.repositories, "myMavenRepo",  URI.create("https://repo.mycompany.com/maven2"), MAVEN_REPO_DEFAULT_METADATA_SOURCES, configureModuleExclusion(["my.company"]))

        createGradleModuleMetadataExclusiveContentRepository(project.repositories, "myMavenGradleMetadataRepo", URI.create("https://repo.mycompany.com/maven2"), ["my.company"])

        println project.repositories
    }

    private static void createMaven(RepositoryHandler repositories, String name, URI url, Action<MavenArtifactRepository.MetadataSources> metadataSources, Action<RepositoryContentDescriptor> repositoryContentDescriptor) {
        repositories.maven { MavenArtifactRepository r ->
            r.name = name
            r.url = url
            r.metadataSources(metadataSources)
            if(repositoryContentDescriptor) {
                r.content(repositoryContentDescriptor)
            }
        }
    }

    private static void createGradleModuleMetadataExclusiveContentRepository(RepositoryHandler repositories, String repoName, URI repoUrl, List<String> modulesWithGradleMetadata) {
        repositories.exclusiveContent(new Action<ExclusiveContentRepository>() {
            @Override
            void execute(ExclusiveContentRepository exclusiveContentRepository) {
                exclusiveContentRepository.forRepository {
                    repositories.maven {  r ->
                        r.name = repoName
                        r.url = repoUrl
                        r.metadataSources(MAVEN_REPO_GRADLE_METADATA_METADATA_SOURCES)
                    }
                }
                exclusiveContentRepository.filter(new Action<InclusiveRepositoryContentDescriptor>() {
                    @Override
                    void execute(InclusiveRepositoryContentDescriptor inclusiveRepositoryContentDescriptor) {
                        modulesWithGradleMetadata.each { module ->
                            println module
                            includeDependency(module, inclusiveRepositoryContentDescriptor)
                        }
                    }
                })
            }
        })
    }

    private static Action<MavenArtifactRepository.MetadataSources> MAVEN_REPO_GRADLE_METADATA_METADATA_SOURCES = new Action<MavenArtifactRepository.MetadataSources>() {
        @Override
        void execute(MavenArtifactRepository.MetadataSources metadataSources) {
            metadataSources.gradleMetadata()
            metadataSources.mavenPom()
        }
    }

    private static Action<MavenArtifactRepository.MetadataSources> MAVEN_REPO_DEFAULT_METADATA_SOURCES = new Action<MavenArtifactRepository.MetadataSources>() {
        @Override
        void execute(MavenArtifactRepository.MetadataSources metadataSources) {
            metadataSources.mavenPom()
            metadataSources.artifact()
            metadataSources.ignoreGradleMetadataRedirection()
        }
    }

    private static Action<RepositoryContentDescriptor> configureModuleExclusion(List<String> modules) {
        return new Action<RepositoryContentDescriptor>() {
            @Override
            void execute(RepositoryContentDescriptor repositoryContentDescriptor) {
                modules.each {
                    excludeDependency(it, repositoryContentDescriptor)
                }
            }
        }
    }


    private static void excludeDependency(String module, RepositoryContentDescriptor c) {
        def parts = module.split(':')
        switch (parts.size()) {
            case 1:
                c.excludeGroup(parts[0])
                break
            case 2:
                c.excludeModule(parts[0], parts[1])
                break
            case 3:
                c.excludeVersion(parts[0], parts[1], parts[2])
                break
        }

    }

    private static void includeDependency(String module, InclusiveRepositoryContentDescriptor c) {
        def parts = module.split(':')
        switch (parts.size()) {
            case 1:
                c.includeGroup(parts[0])
                break
            case 2:
                c.includeModule(parts[0], parts[1])
                break
            case 3:
                c.includeVersion(parts[0], parts[1], parts[2])
                break
        }
    }

}