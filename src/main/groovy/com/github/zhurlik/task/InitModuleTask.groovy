package com.github.zhurlik.task

import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * The main goal is to generate {@link JBossModule} from a pom file.
 *
 * @author zhurlik@gmail.com
 */
@Slf4j('logger')
class InitModuleTask extends DefaultTask {
    def String pomName
    def String result

    @TaskAction
    def initModule() {
        logger.info ">> Extracting JBossModule from a pom file..."

        // looking for a pom file
        project.configurations.jbossmodules.files.findAll { it.name.startsWith(pomName) }.each {
            def jarName = it.name
            it.parentFile.parentFile.eachFileRecurse() {
                if (it.name == this.pomName + '.pom') {
                    // to parse a xml
                    def pomXml = new XmlSlurper().parse(it)
                    logger.debug('>> POM file: \n{}', it.getText())

                    // to generate a groovy object that can be used in the gradle script
                    def tpl = '''\
${module} {
    moduleName = '${moduleName}'
    resources = ['${resources}']
    dependencies = ${requiredDependencies}
    // or via more resources ${requiredResources}

    // optional which can be needed:
    // either resources = ${optionalResources}
    // or dependencies = ${optionalDependencies}
}'''

                    def template = new groovy.text.SimpleTemplateEngine().createTemplate(tpl)

                    // input parameters for a template
                    def binding = [
                            module              : pomXml.artifactId.text().replaceAll('.-', ''),
                            moduleName          : pomXml.groupId.text() + '.' + pomXml.artifactId.text(),
                            resources           : jarName,
                            requiredResources   : pomXml.dependencies.dependency.findAll({
                                it.scope.text() == 'compile' && it.optional.text() != 'true'
                            }).collect {
                                it.artifactId.text() + '-' + it.version.text() + '.jar'
                            }.toString(),
                            optionalResources   : pomXml.dependencies.dependency.findAll({
                                it.scope.text() == 'compile' && it.optional.text() == 'true'
                            }).collect {
                                it.artifactId.text() + '-' + it.version.text() + '.jar'
                            }.toString(),
                            requiredDependencies: pomXml.dependencies.dependency.findAll({
                                it.scope.text() == 'compile' && it.optional.text() != 'true'
                            }).collect {
                                (it.groupId.text().contains(it.artifactId.text())) ? it.groupId.text() : it.groupId.text() + '.' + it.artifactId.text()
                            }.toString(),
                            optionalDependencies: pomXml.dependencies.dependency.findAll({
                                it.scope.text() == 'compile' && it.optional.text() == 'true'
                            }).collect {
                                (it.groupId.text().contains(it.artifactId.text())) ? it.groupId.text() : it.groupId.text() + '.' + it.artifactId.text()
                            }.toString()
                    ]

                    // final output
                    def String response = template.make(binding)
                    result = response

                    logger.debug('>> JBossModules: \n{}', response)
                }
            }
        }
    }
}
