package com.github.zhurlik.extension

import com.github.zhurlik.Ver
import com.github.zhurlik.repository.Server
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult

import java.nio.file.Paths

import static com.github.zhurlik.Ver.V_1_1

/**
 * This class allows an access all modules under JBoss Server
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class JBossServer implements Server {
    String name, home
    private final Map<String, File> modules = [:]
    private File modulesDir

    // default value
    Ver version = V_1_1

    /**
     * The special constructor to be able to use in the gradle script
     *
     * jbossrepos {
     *         serverA {
     *             home = '/jboss/home/dir'
     *             version = '1.0'
     *         ...
     *         }
     * }
     *
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
        this.modulesDir = Paths.get(home,'modules').toFile()
    }

    void initTree() {
        modules.clear()

        // under jboss
        if (this.modulesDir != null && this.modulesDir.exists()) {
            this.modulesDir.eachDirRecurse() {
                it.eachFileMatch(~/module.xml/) { file ->
                    GPathResult xml = new XmlSlurper().parse(file)
                    modules.put(xml.@name.toString(), file)
                }
            }
        }
    }

    @Override
    JBossModule getModule(final String name) {
        //result
        JBossModule jbModule = new JBossModule(name)

        if (modules.containsKey(name) && modules[name].exists()) {
            jbModule = version.makeModule(modules[name].getText('UTF-8'))

            log.debug '>> Module: \'{}\' has been loaded', name
            jbModule
        }

        log.warn '>> Module:\'{}\' is not available', name
        jbModule
    }

    @Override
    List<String> getNames() {
        modules.keySet().toList()
    }

    @Override
    File getModulesDir() {
        this.modulesDir
    }

    @Override
    void undeployModule(m) {
        JBossModule jbModule

        if (m instanceof String) {
            jbModule = getModule(m)
        } else if (m instanceof JBossModule) {
            jbModule = m
        } else {
            return
        }

        if (jbModule != null) {
            log.debug '>> Undeploying module: {} from Server:{}', jbModule.moduleName, this.name

            String toDel = this.home + jbModule.path
            Closure delDir = {
                new AntBuilder().delete(dir:it)
                log.debug '>> Directory:{} has been erased', it
            }

            delDir(toDel)

            // deleting empty folders
            File f = Paths.get(toDel).toFile().parentFile
            while (f.path != modulesDir.path && f.isDirectory() && (f.list() as List).empty) {
                delDir(f)
                f = f.parentFile
            }
        }
    }

    @Override
    String getMainXml(String name) {
        modules[name].text
    }
}
