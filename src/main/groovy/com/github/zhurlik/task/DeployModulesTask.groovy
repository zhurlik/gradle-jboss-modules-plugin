package com.github.zhurlik.task

import com.github.zhurlik.extension.JBossModule
import com.github.zhurlik.extension.JBossServer
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction

/**
 * This Task will deploy modules from Project to JBoss Servers.
 *
 * @author zhurlik@gmail.com
 */
@Slf4j('logger')
class DeployModulesTask extends DefaultTask {
    @InputDirectory
    File inputDir = project.file("$project.buildDir/install")

    @TaskAction
    def deployModules() {
        logger.info ">> Deploying Modules to JBoss Servers..."

        project.delete deployDirectories

        project.modules.each() { JBossModule m ->
            project.jbossrepos.each { JBossServer s ->
                m.deployToJBoss(s, project)
            }
        }
    }

    @OutputDirectories
    def getDeployDirectories() {
        project.jbossrepos.collect { new File(it.home.toString()) }
    }
}
