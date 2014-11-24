package com.zhurlik.task

import com.zhurlik.extension.JBossModule
import com.zhurlik.extension.JBossServer
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * This Task will deploy modules from Project to JBoss Servers.
 *
 * @author zhurlik@gmail.com
 */
@Slf4j('logger')
class DeployModulesTask extends DefaultTask {
    @TaskAction
    def deployModules() {
        logger.info ">> Deploying Modules to JBoss Servers..."
        project.modules.each() { JBossModule m ->
            project.jbossrepos.each { JBossServer s ->
                m.deployToJBoss(s, project)
            }
        }
    }
}
