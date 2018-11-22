package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import org.junit.Test

import static org.mockito.Mockito.never
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify


class ModuleAliasTagTest {
    private JBossModule jBossModule

    @Test
    void testSupported() {
        (Ver.values() - [Ver.V_1_0]).each { version ->
            // init data
            jBossModule = spy(new JBossModule('test-module'))
            final String txt = "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<module-alias xmlns='urn:jboss:module:" + version.number + "' " +
                    "name='my.module' ${isSlotSupported(version) ? 'slot=\'1.0\'' : ''} target-name='testTarget' />"

            final GPathResult xml = new XmlSlurper().parseText(txt)

            // call
            ModuleAliasTag.parse(xml).accept(jBossModule)

            // verify
            verify(jBossModule, times(1)).setModuleAlias(true)
            verify(jBossModule, times(isSlotSupported(version) ? 1 : 0)).setSlot('1.0')
            verify(jBossModule, times(1)).setModuleName('my.module')
            verify(jBossModule, times(1)).setTargetName('testTarget')
        }
    }

    private static boolean isSlotSupported(final Ver version) {
        return version in [Ver.V_1_1, Ver.V_1_2, Ver.V_1_3, Ver.V_1_5, Ver.V_1_6]
    }

    @Test
    void testUnsupported() {
        // init data
        jBossModule = spy(new JBossModule('test-module'))
        final String txt = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.0' target-name='testTarget' />"
        final GPathResult xml = new XmlSlurper().parseText(txt)

        // call
        ModuleAliasTag.parse(xml).accept(jBossModule)

        // verify
        verify(jBossModule, never()).setModuleAlias(true)
    }
}
