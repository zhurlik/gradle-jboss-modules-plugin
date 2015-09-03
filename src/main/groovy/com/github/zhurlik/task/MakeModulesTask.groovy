package com.github.zhurlik.task
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
/**
 * The main task to create folder structure for JBoss Modules like this
 *
 *     ${project}/${build}/modules/module/name/dir/{main|slot}
 *                                              *.jars
 *                                              main.xml
 * @author zhurlik@gmail.com
 */
@Slf4j('logger')
class MakeModulesTask extends DefaultTask {
    @TaskAction
    def makeModules() {
        logger.info ">> Creating JBoss Modules locally..."

        if (project.jbossrepos.isEmpty()) {
            throw new GradleException('You need at least one JBoss Server');
        }

        project.modules.each() { JBossModule m ->
            m.makeLocally(project)
        }
    }
}
