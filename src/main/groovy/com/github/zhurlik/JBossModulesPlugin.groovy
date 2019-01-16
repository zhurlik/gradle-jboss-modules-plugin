package com.github.zhurlik

import com.github.zhurlik.extension.JBossModule
import com.github.zhurlik.extension.JBossServer
import com.github.zhurlik.task.CheckModulesTask
import com.github.zhurlik.task.DeployModulesTask
import com.github.zhurlik.task.MakeModulesTask
import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

import java.nio.file.Paths

/**
 * Gradle plugin to make JBoss Modules.
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class JBossModulesPlugin implements Plugin<Project> {

    private static final String MODULES = 'modules'

    @Override
    void apply(final Project project) {
        log.info('>> Plugin: JBoss Modules')

        // distribution plugin to make tar/zip
        project.plugins.apply('distribution')

        // to be able to use maven repository
        project.configurations.create('jbossmodules')

        // to have a section in the gradle script to specify JBoss Modules
        project.extensions.modules = project.container(JBossModule)

        // JBoss Servers
        project.extensions.jbossrepos = project.container(JBossServer)

        // to have a list of JBoss Servers
        project.afterEvaluate {
            // make for each Server a Distribution object
            project.jbossrepos.each { final JBossServer server ->
                project.distributions.create(server.name)
                project.distributions[server.name].baseName = server.name
                project.distributions[server.name].contents.from(
                        Paths.get(project.buildDir.path, 'install', server.name, MODULES).toFile()
                ) {
                    into MODULES
                }

                // {server.name}DistTar/Zip tasks depend on checkModules to be able to create modules
                project.tasks.findAll { it.name.startsWith(server.name + 'Dist') }.each { t ->
                    log.info '>> Task:' + t.name + ' will depend on chechModules'
                    t.dependsOn CheckModulesTask.NAME
                }
            }

            // compress all tar files
            project.tasks.withType(Tar) {
                compression = Compression.GZIP
            }
        }

        // special tasks
        Task makeModulesTask = project.task(MakeModulesTask.NAME, type: MakeModulesTask)
        project.task(CheckModulesTask.NAME, type: CheckModulesTask) {
            outputs.upToDateWhen {
                !makeModulesTask.didWork
            }
        }
        project.task(DeployModulesTask.NAME, type: DeployModulesTask)
        project.tasks.checkModules.dependsOn(MakeModulesTask.NAME)
        project.tasks.deployModules.dependsOn(CheckModulesTask.NAME)
    }
}
