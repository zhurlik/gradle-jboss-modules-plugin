package com.github.zhurlik.extension
import com.github.zhurlik.Ver
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.api.Project

import static com.github.zhurlik.Ver.V_1_0
import static java.io.File.separator

/**
 * To make JBoss Module.
 * https://docs.jboss.org/author/display/MODULES/Home
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class JBossModule {
    @Deprecated
    def slot
    def String name, moduleName, mainClass, targetName, version
    def properties = [:]
    def resources = []
    def dependencies = []
    def exports = []
    def Ver ver = V_1_0
    def boolean moduleAlias = false
    def boolean moduleAbsent = false
    def String defaultLoader
    def boolean moduleConfiguration = false
    // A defined loader. More than one loader may be defined.
    def loaders = []
    def permissions = []

    // a list of server names for which this module will be available, empty - for all
    def servers = []
    def configuration

    /**
     * The special constructor to be able to use in the gradle script
     *
     * modules {
     *    moduleA {
     *        moduleName = 'com.moduleA'
     *        slot = '1.0'
     *    }
     * }
     * @param name
     */
    JBossModule(final String name) {
        this.name = name
    }

    /**
     * The loader to use. The name matches the value of the "name" attribute of one of the defined loaders.
     *
     * @param name
     */
    void setDefaultLoader(final String name) {
        assert name ==~ /[-0-9a-zA-Z_]+/,
                'Default-Loader must be: [-0-9a-zA-Z_]+'
        this.defaultLoader = name
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
     * A module name, which consists of one or more dot (.)-separated segments. Each segment must begin and end
     * with an alphanumeric or underscore (_), and may otherwise contain alphanumerics, underscores, and hyphens (-).
     *
     * @param name
     */
    void setTargetName(final String name) {
        assert name ==~ /[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?(\.[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?)*/,
                'Target Name must be: [a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?(\\.[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?)*'
        this.targetName = name
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
     * The version of this module (optional).
     * ([a-zA-Z0-9]+)([-_+.][a-zA-Z0-9]+)*
     *
     * @param version
     */
    void setVersion(final String version) {
        assert version ==~ /([a-zA-Z0-9]+)([-_+.][a-zA-Z0-9]+)*/,
                'Version must be: ([a-zA-Z0-9]+)([-_+.][a-zA-Z0-9]+)*'
        this.version = version
    }

    /**
     * Makes a module descriptor is an XML file which describes
     * the structure, content, dependencies, filtering, and other attributes of a module.
     *
     * @return a xml as string
     */
    String getModuleDescriptor() {
        return this.ver.builder.getXmlDescriptor(this)
    }

    boolean isValid() {
        return this.ver.builder.isValid(this.ver.builder.getXmlDescriptor(this))
    }

    /**
     * A path where a module will be stored under JBoss Server.
     *
     * @return string like 'module/name/dir/{main|slot}'
     */
    String getPath() {
        this.ver.builder.getPath(this)
    }

    public void deployToJBoss(final JBossServer server, final Project project) {
        log.debug '>> Deploying the module:{} to JBoss Server:{}', this.moduleName, server.name

        // to have full path for ${project}/${build}/install/{server}/modules/module/name/dir/{main|slot}
        def String source = [project.buildDir.path, 'install', server.name, getPath()].join(separator)
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
        def configuration = this.configuration ?: project.configurations.jbossmodules

        project.jbossrepos.each {JBossServer server ->

            // for servers that were specified, by default for all
            if (!servers.isEmpty() && !(server.name in servers)) {
                return //continue
            }

            // to have full path for ${project}/${build}/install/{serverName}/modules/module/name/dir/{main|slot}
            def String moduleDirName = [project.buildDir.path, 'install', server.name, getPath()].join(separator)

            // save a xml
            def File moduleDir = new File(moduleDirName)
            if (!moduleDir.exists()) {
                assert moduleDir.mkdirs(), 'Can\'t create a folder'
            }

            // use server's version
            def originalVer = this.ver
            this.ver = server.version

            def xmlfile = new File(moduleDir, 'module.xml')
            xmlfile.text = getModuleDescriptor()
            log.debug '>> Module Descriptor:' + xmlfile.path

            // revert version
            this.ver = originalVer

            // copy jars
            def jarNames = this.resources.findAll() {
                it instanceof String || it instanceof GString
            } + this.resources.findAll() {
                !(it instanceof String || it instanceof GString)
            }.collect() { it.path }

            jarNames.each() { jar ->
                if (jar == '.') {
                    return // the case if you would like to have <resource-root path='.' />
                }

                // jar names can contain the regex 'spring-web.*'
                def jarFiles = configuration.files.findAll() { it.name ==~ jar.toString() }

                // throw an error if the regex doesn't match any files
                if (jarFiles.size() == 0) {
                    throw new GradleException("Could not resolve files from $configuration for pattern '$jar' on module '${this.name}'")
                }

                jarFiles.each {
                    final String source = it.path
                    final String target = [moduleDirName, it.name].join(separator)
                    final AntBuilder ant = new AntBuilder()
                    ant.copy(file: source, toFile: target, overwrite: true)
                    log.debug '>> Resource:' + target

                    // replacing the regex with the correct jar name
                    ant.replace(file: xmlfile.path, token: jar.toString(), value: it.name)
                }
            }

            log.debug('>> Module is available here {}', moduleDir.path)
        }
    }
}
