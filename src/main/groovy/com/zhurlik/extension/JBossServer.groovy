package com.zhurlik.extension

import com.zhurlik.Ver
import com.zhurlik.descriptor.AbstractBuilder
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
    }

    public void initTree() {
        modules.clear()

        // under jboss
        def jboss = new File(home + separator + 'modules')
        jboss.eachDirRecurse() {
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
            final AbstractBuilder<JBossModule> builder = getBuilder(version)
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
    void deployModule(final JBossModule module) {
        //todo: not implemented yet
        throw new UnsupportedOperationException("Not implemented yet")
    }

    @Override
    String getMainXml(String name) {
        return modules[name].text
    }
}
