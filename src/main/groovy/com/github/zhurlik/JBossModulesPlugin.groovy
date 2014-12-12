package com.github.zhurlik

import com.github.zhurlik.extension.JBossModule
import com.github.zhurlik.extension.JBossServer
import com.github.zhurlik.task.CheckModulesTask
import com.github.zhurlik.task.DeployModulesTask
import com.github.zhurlik.task.MakeModulesTask
import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin to make JBoss Modules.
 *
 * @author zhurlik@gmail.com
 */
@Slf4j('logger')
class JBossModulesPlugin implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        logger.info '>> Plugin: JBoss Modules'

        // to be able to use maven repository
        project.configurations.create('jbossmodules')

        // to have a section in the gradle script to specify JBoss Modules
        def modules = project.container(JBossModule)
        project.extensions.modules = modules

        // JBoss Servers
        def servers = project.container(JBossServer)
        project.extensions.jbossrepos = servers

        // special tasks
        project.task('makeModules', type: MakeModulesTask)
        project.task('checkModules', type: CheckModulesTask)
        project.task('deployModules', type: DeployModulesTask)
        project.tasks.checkModules.dependsOn('makeModules')
        project.tasks.deployModules.dependsOn('checkModules')
    }
}
