package com.github.zhurlik.extension

import groovy.util.logging.Slf4j
import org.junit.Test

import static com.github.zhurlik.Ver.V_1_1
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

/**
 * To check {@link JBossServer}
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class JBossServerTest {
    private JBossServer server

    @Test
    void testCreation() throws Exception {
        log.debug '>> JBossServer...'

        log.debug '>> Case1:'
        server = new JBossServer('jboss-test-7.1.1')
        assertNotNull server
        assertEquals 'Default version', V_1_1, server.version
        assertEquals 'Server Name', 'jboss-test-7.1.1', server.name

        log.debug 'Case2:'
        server = new JBossServer('jboss-test-7.1.1')
        server.home = this.class.classLoader.getResource('./install/7.1.1/').toURI().path
        server.initTree()
        assertNotNull server

        log.debug '>> Available Modules:' + server.names

        assertNotNull server.getModule('')

        JBossModule m = server.getModule('javax.xml.bind.api')
        log.debug('>> Generated:\n{}', m.moduleDescriptor)
        log.debug('>> From file:\n{}', server.getMainXml('javax.xml.bind.api'))
        assertTrue m.isValid()
    }

    @Test
    void testUndeploy() throws Exception {
        log.debug '>> Testing undeploying process...'
        server = new JBossServer('jboss-test-7.1.1')
        server.home = this.class.classLoader.getResource('./install/7.1.1/').toURI().path
        server.initTree()

        JBossModule jbModule = server.getModule('org.apache.xml-resolver')
        assertNotNull jbModule
        assertEquals 'org.apache.xml-resolver', jbModule.getModuleName()
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='org.apache.xml-resolver'>\n" +
                "  <properties>\n" +
                "    <property name='jboss.api' value='private' />\n" +
                "  </properties>\n" +
                "  <resources>\n" +
                "    <resource-root path='.' />\n" +
                "    <resource-root path='xml-resolver-1.2.fake' />\n" +
                "  </resources>\n" +
                "  <dependencies>\n" +
                "    <module name='javax.api' />\n" +
                "  </dependencies>\n" +
                "</module>", jbModule.getModuleDescriptor()
        server.undeployModule(jbModule)

        // check
        server.initTree()
        jbModule = server.getModule('org.apache.xml-resolver')
        assertNotNull jbModule
        assertNull jbModule.getModuleName()
        assertFalse new File(server.modulesDir.path + '/org').exists()
    }
}
