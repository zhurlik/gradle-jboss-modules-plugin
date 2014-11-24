package com.zhurlik.extension

import com.zhurlik.Ver
import com.zhurlik.descriptor.AbstractBuilder
import com.zhurlik.descriptor.BuilderFactory
import groovy.util.logging.Slf4j
import org.gradle.api.Project

import static com.zhurlik.Ver.V_1_1
import static java.io.File.separator

/**
 * To make JBoss Module.
 * https://docs.jboss.org/author/display/MODULES/Home
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class JBossModule {
    def String name, moduleName, slot, mainClass
    def properties = [:]
    def resources = []
    def dependencies = []
    def exports
    def Ver ver = V_1_1

    /**
     * The special constructor to be able to use in the gradle script
     *
     * modules {*     moduleA {*          moduleName = 'com.moduleA'
     *         slot = '1.0'
     *}*}*
     * @param name
     */
    JBossModule(final String name) {
        this.name = name
    }

    /**
     * A module name, which consists of one or more dot (.)-separated segments. Each segment must begin and end
     * with an alphanumeric or underscore (_), and may otherwise contain alphanumerics, underscores, and hyphens (-).
     *
     * @param name
     */
    void setModuleName(final String name) {
        assert name ==~ /[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?(\.[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?)*/,
                'Module Name must be: [a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?(\\.[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?)*'
        this.moduleName = name
    }

    /**
     * A module version slot. A slot may consist of one or more alphanumerics, hyphens (-), underscores (_),
     * plus signs (+), asterisks (*), or dots (.).
     *
     * @param slot
     */
    void setSlot(final String slot) {
        assert slot ==~ /[-a-zA-Z0-9_+*.]+/,
                'Slot must be: [-a-zA-Z0-9_+*.]+'
        this.slot = slot
    }

    /**
     * Makes a module descriptor is an XML file which describes
     * the structure, content, dependencies, filtering, and other attributes of a module.
     *
     * @return a xml as string
     */
    String getModuleDescriptor() {
        return getBuilder().getXmlDescriptor(this)
    }

    boolean isValid() {
        return getBuilder().isValid(builder.getXmlDescriptor(this))
    }

    /**
     * A path where a module will be stored under JBoss Server.
     *
     * @return string like 'module/name/dir/{main|slot}'
     */
    String getPath() {
        getBuilder().getPath(this)
    }

    private AbstractBuilder<JBossModule> getBuilder() {
        return BuilderFactory.getBuilder(this.ver)
    }

    public void deployToJBoss(final JBossServer server, final Project project) {
        log.debug '>> Deploying the module:{} to JBoss Server:{}', this.moduleName, server.name

        // to have full path for ${project}/${build}/modules/module/name/dir/{main|slot}
        def String source = [project.buildDir.path, getPath()].join(separator)
        def String target = [server.home, getPath()].join(separator)

        new AntBuilder().copy(toDir: target, overwrite: true) {
            fileset(dir: source)
        }
    }

    /**
     * To save main.xml and all resources to Project's folder.
     */
    public void makeLocally(final Project project) {
        log.debug '>> Module:' + this.name

        // to have full path for ${project}/${build}/modules/module/name/dir/{main|slot}
        def String moduleDirName = [project.buildDir.path, getPath()].join(separator)

        // save a xml
        def File moduleDir = new File(moduleDirName)
        if (!moduleDir.exists()) {
            assert moduleDir.mkdirs(), 'Can\'t create a folder'
        }

        def xmlfile = new File(moduleDir, 'module.xml') << getModuleDescriptor()
        log.debug '>> Module Descriptor:' + xmlfile.path

        // copy jars
        def jarNames = this.resources.findAll() { it instanceof String } + this.resources.findAll() {
            !(it instanceof String)
        }.collect() { it.path }
        jarNames.each() { jar ->
            project.configurations.jbossmodules.files.findAll() { it.name == jar }.each {
                final String source = it.path
                final String target = [moduleDirName, jar].join(separator)
                new AntBuilder().copy(file: source, toFile: target, overwrite: true)
                log.debug '>> Resource:' + target
            }
        }

        log.debug('>> Module is available here {}', moduleDir.path)
    }
}
