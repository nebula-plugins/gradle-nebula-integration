package test

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class MyTask extends DefaultTask {
    @TaskAction
    void test() {
        println "something should happen here"
    }
}