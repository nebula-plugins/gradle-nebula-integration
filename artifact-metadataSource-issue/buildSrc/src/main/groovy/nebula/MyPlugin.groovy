package nebula

import org.gradle.api.Plugin
import org.gradle.api.Project

class MyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.with {
            repositories {
                maven {
                    url "https://artifacts.netflix.com/package-tarballs"
                    metadataSources {
                        artifact()
                    }
                }
            }

            configurations {
                protolock
            }

            dependencies {
                protolock "nilslice:protolock:20190316T034824Z:linux-amd64@tgz"
            }
        }
    }
}