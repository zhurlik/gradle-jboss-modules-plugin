package com.github.zhurlik
import com.github.zhurlik.extension.JBossModule
import com.github.zhurlik.extension.JBossServer
import com.github.zhurlik.task.CheckModulesTask
import com.github.zhurlik.task.DeployModulesTask
import com.github.zhurlik.task.MakeModulesTask
import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

import static java.io.File.separator
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

        // distribution plugin to make tar/zip
        project.getPlugins().apply('distribution')

        // to be able to use maven repository
        project.configurations.create('jbossmodules')

        // to have a section in the gradle script to specify JBoss Modules
        def modules = project.container(JBossModule)
        project.extensions.modules = modules

        // JBoss Servers
        def servers = project.container(JBossServer)
        project.extensions.jbossrepos = servers

        // to have a list of JBoss Servers
        project.afterEvaluate {
            // make for each Server a Distribution object
            project.jbossrepos.each { JBossServer server ->
                project.distributions.create(server.name)
                project.distributions[server.name].baseName = server.name
                project.distributions[server.name].contents.from([project.buildDir.path, 'install', server.name, 'modules'].join(separator))

                // {server.name}DistTar/Zip tasks depend on checkModules to be able to create modules
                project.tasks.findAll { it.name.startsWith(server.name + 'Dist') }.each { t ->
                    logger.info '>> Task:' + t.name + ' will depend on chechModules'
                    t.dependsOn 'checkModules'
                }
            }

            // compress all tar files
            project.tasks.withType(Tar) {
                compression = Compression.GZIP
            }
        }

        // special tasks
        project.task('makeModules', type: MakeModulesTask)
        project.task('checkModules', type: CheckModulesTask)
        project.task('deployModules', type: DeployModulesTask)
        project.tasks.checkModules.dependsOn('makeModules')
        project.tasks.deployModules.dependsOn('checkModules')
    }
}
