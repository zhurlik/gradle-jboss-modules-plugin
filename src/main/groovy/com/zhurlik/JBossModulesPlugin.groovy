package com.zhurlik

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin to make JBoss Modules.
 *
 * @author zhurlik@gmail.com
 */
class JBossModulesPlugin implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        println '>> Plugin: JBoss Modules'
        def modules = project.container(JBossModule)
        project.extensions.modules = modules
    }
}
