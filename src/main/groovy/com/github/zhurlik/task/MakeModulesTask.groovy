package com.github.zhurlik.task
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectCollection
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

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
    @OutputDirectory
    File installDir = project.file("$project.buildDir/install")

    @TaskAction
    def makeModules() {
        logger.info ">> Creating JBoss Modules locally..."

        project.delete installDir

        if (project.jbossrepos.isEmpty()) {
            throw new GradleException('You need at least one JBoss Server');
        }

        project.modules.each() { JBossModule m ->
            m.makeLocally(project)
        }
    }

    @Input
    private Collection<String> getModuleDescriptions() {
        project.modules.collect { it.moduleDescriptor }
    }

    @InputFiles
    private FileCollection getModuleResources() {
        def resources = project.configurations.jbossmodules
        project.modules.each { JBossModule module ->
            if (module.configuration != null) {
                resources += module.configuration
            }
        }

        return resources
    }
}
