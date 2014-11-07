package com.zhurlik

import com.zhurlik.extension.JBossModule
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.io.File.separator

/**
 * The main task to create folder structure for JBoss Modules like this
 *
 *     ${project}/${build}/modules/module/name/dir/{main|slot}
 *                                              *.jars
 *                                              main.xml
 * @author zhurlik@gmail.com
 */
class MakeModulesTask extends DefaultTask {

    @OutputDirectory
    def File outputDir = new File(project.buildDir.path + separator + 'modules')

    @TaskAction
    def makeModules() {
        log.info ">> Creating JBoss Modules..."
        project.modules.each() { JBossModule m ->

            // to have full path for ${project}/${build}/modules/module/name/dir/{main|slot}
            def String moduleDirName = [outputDir.path, m.moduleName.replaceAll('\\.', separator), ((m.slot in [null, '']) ? 'main' : m.slot)].join(separator)

            // save a xml
            def File moduleDir = new File(moduleDirName)
            if (!moduleDir.exists()) {
                assert moduleDir.mkdirs(), 'Can\'t create a folder'
            }
            def xmlfile = new File(moduleDir, 'module.xml') << m.moduleDescriptor
            log.debug '>> Module Descriptor:' + xmlfile.path

            // copy jars
            def jarNames = m.resources.findAll() { it instanceof String } + m.resources.findAll() { !(it instanceof String) }.collect() { it.path }
            jarNames.each() { jar ->
                project.configurations.jbossmodules.files.findAll() { it.name == jar }.each {
                    def Path source = Paths.get(it.path)
                    def Path target = Paths.get(moduleDirName, jar)
                    Files.copy(source, target)
                    log.debug '>> Resource:' + target
                }
            }
        }
    }
}
