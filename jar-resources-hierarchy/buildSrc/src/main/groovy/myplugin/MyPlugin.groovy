package myplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class MyPlugin implements  Plugin<Project> {

    @Override
    void apply(Project project) {
        println " Thread.currentThread().getContextClassLoader().getResources('something/here').hasMoreElements: " +  Thread.currentThread().getContextClassLoader().getResources('something/here').hasMoreElements()
    }
}