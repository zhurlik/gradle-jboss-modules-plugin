package com.zhurlik

import com.zhurlik.extension.JBossModule
import com.zhurlik.extension.JBossServer
import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin to make JBoss Modules.
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class JBossModulesPlugin implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        log.info '>> Plugin: JBoss Modules'

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
        project.tasks.checkModules.dependsOn('makeModules')
    }
}
