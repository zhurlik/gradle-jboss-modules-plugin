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

        // to be able to use maven repository
        project.configurations.create('jbossmodules')

        // to have a section in the gradle script to specify JBoss Modules
        def modules = project.container(JBossModule)
        project.extensions.modules = modules

        // special tasks
        project.task('makeModules', type: com.zhurlik.MakeModulesTask)
        project.task('checkModules', type: CheckModulesTask)
        project.tasks.checkModules.dependsOn('makeModules')
    }
}
