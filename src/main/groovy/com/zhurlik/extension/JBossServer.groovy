package com.zhurlik.extension

import com.zhurlik.Ver
import com.zhurlik.descriptor.Builder
import com.zhurlik.repository.Server
import groovy.util.logging.Slf4j

import static com.zhurlik.descriptor.BuilderFactory.getBuilder
import static java.io.File.separator

/**
 * This class allows an access all modules under JBoss Server
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class JBossServer implements Server {
    def String name, home
    private Map<String, File> modules = [:]
    private File modulesDir

    // default value
    def Ver version = Ver.V_1_1

    /**
     * The special constructor to be able to use in the gradle script
     *
     * jbossrepos {*     serverA {*         home = '/jboss/home/dir'
     *         version = '1.0'
     *         ...
     *}*}*
     * @param name
     */
    JBossServer(final String name) {
        this.name = name
    }

    /**
     * After setting a home folder we are fetching all modules under this JBoss Server.
     *
     * @param home
     */
    void setHome(final String home) {
        assert home != null
        this.home = home
        this.modulesDir = new File(home + separator + 'modules')
    }

    public void initTree() {
        modules.clear()

        // under jboss
        this. modulesDir.eachDirRecurse() {
            it.eachFileMatch(~/module.xml/) { file ->
                def m = new XmlSlurper().parse(file)
                modules.put(m.@name.toString(), file)
            }
        }
    }

    @Override
    JBossModule getModule(final String name) {
        //result
        JBossModule jbModule = new JBossModule(name)

        if (modules.containsKey(name) && modules[name].exists()) {
            final Builder<JBossModule> builder = getBuilder(version)
            jbModule = builder.makeModule(modules[name].getText('UTF-8'))

            log.debug '>> Module: \'{}\' has been loaded', name
            return jbModule
        }

        log.warn '>> Module:\'{}\' is not available', name
        return jbModule
    }

    @Override
    List<String> getNames() {
        return modules.keySet().toList()
    }

    @Override
    File getModulesDir() {
        return this.modulesDir
    }

    @Override
    void undeployModule(m) {
        JBossModule jbModule

        if (m instanceof String) {
            jbModule = getModule(m)
        } else if(m instanceof JBossModule) {
            jbModule = m
        } else {
            return
        }

        if (jbModule != null) {
            log.debug '>> Undeploying module: {} from Server:{}', jbModule.moduleName, this.name

            String toDel = this.home + jbModule.path
            def delDir = {
                new AntBuilder().delete(dir: it)
                log.debug '>> Directory:{} has been erased', it
            }

            delDir(toDel)

            File f = new File(toDel).parentFile
            while(f.path != modulesDir.path && f.isDirectory() && (f.list() as List).empty) {
                delDir(f)
                f = f.parentFile
            }
        }
    }

    @Override
    String getMainXml(String name) {
        return modules[name].text
    }
}
