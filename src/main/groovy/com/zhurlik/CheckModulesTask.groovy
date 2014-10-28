package com.zhurlik

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

import static java.io.File.separator

/**
 * A task to isValid xml descriptors of JBoss Modules
 *
 * @author zhurlik@gmail.com
 */
class CheckModulesTask extends DefaultTask{
    @TaskAction
    def checkModules() {
        println ">> Validation process for JBoss Modules"
        project.modules.each() { JBossModule m ->
            println String.format('>> Xml Descriptor:%s isValid=%s', m.moduleName, m.isValid())
        }
    }
}
