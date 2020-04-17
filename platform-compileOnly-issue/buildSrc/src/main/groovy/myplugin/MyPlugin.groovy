package myplugin

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.AttributesSchema
import org.gradle.api.attributes.Category

import static org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE

class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate {
            project.dependencies.attributesSchema(new Action<AttributesSchema>() {
                @Override
                void execute(AttributesSchema s) {
                    s.getMatchingStrategy(CATEGORY_ATTRIBUTE).ordered(new Comparator<Category>() {
                        @Override
                        int compare(Category o1, Category o2) {
                            if (o1.name == o2.name) {
                                return 0
                            }
                            if (o1.name == 'platform') {
                                return 1
                            }
                            return -1
                        }
                    })
                }
            })
        }
    }
}