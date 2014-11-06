package com.zhurlik.extension

import com.zhurlik.Ver
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
        server.home = this.class.classLoader.getResource('./7.1.1/').path
        assertNotNull server
        assertEquals 1, server.availableModules.entrySet().size()

        log.debug '>> Available Modules:' + server.availableModules

        assertNotNull server.getModule('')

        JBossModule m = server.getModule('javax.xml.bind.api')

    }
}
