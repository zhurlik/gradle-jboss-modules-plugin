package com.zhurlik.extension

import groovy.util.logging.Slf4j
import org.junit.Test

import static com.zhurlik.Ver.V_1_1
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

/**
 * To check {@link JBossServer}
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class JBossServerTest {
    private JBossServer server

    @Test
    public void testCreation() throws Exception {
        log.debug '>> JBossServer...'

        log.debug '>> Case1:'
        server = new JBossServer('jboss-test-7.1.1')
        assertNotNull server
        assertEquals 'Default version', V_1_1, server.version
        assertEquals 'Server Name', 'jboss-test-7.1.1', server.name

        log.debug 'Case2:'
        server = new JBossServer('jboss-test-7.1.1')
        server.home = this.class.classLoader.getResource('./7.1.1/').toURI().path
        server.initTree()
        assertNotNull server
        assertEquals 1, server.names.size()

        log.debug '>> Available Modules:' + server.names

        assertNotNull server.getModule('')

        JBossModule m = server.getModule('javax.xml.bind.api')
        log.debug('>> Generated:\n{}', m.moduleDescriptor)
        log.debug('>> From file:\n{}', server.getMainXml('javax.xml.bind.api'))
        assert m.isValid()
    }

    @Test
    public void testDeploy() throws Exception {
        log.debug '>> To test deployment process...'


    }
}
