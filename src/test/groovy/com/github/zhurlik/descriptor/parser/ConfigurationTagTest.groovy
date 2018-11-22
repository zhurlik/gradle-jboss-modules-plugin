package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import org.junit.Test

import static junit.framework.Assert.assertEquals
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify


class ConfigurationTagTest {
    private JBossModule jBossModule

    @Test
    void testSingleLoader() {
        Ver.values().each { version ->
            // init data
            jBossModule = spy(new JBossModule('test-module'))
            final String txt = "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<configuration xmlns='urn:jboss:module:" + version.number + "' default-loader='test-default1'>\n" +
                    "  <loader name='test-default1' />\n" +
                    "</configuration>"
            final GPathResult xml = new XmlSlurper().parseText(txt)

            // call
            ConfigurationTag.apply(xml).accept(jBossModule)

            // verify
            verify(jBossModule, times(1)).setModuleConfiguration(true)
            verify(jBossModule, times(1)).setDefaultLoader('test-default1')
            assertEquals('[[name:test-default1]]', jBossModule.loaders.toString())
        }
    }

    @Test
    void testTwoLoaders() {
        Ver.values().each { version ->
            // init data
            jBossModule = spy(new JBossModule('test-module'))
            final String txt = "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<configuration xmlns='urn:jboss:module:" + version.number + "' default-loader='_test-default1'>\n" +
                    "  <loader name='_test-default1' />\n" +
                    "  <loader name='loader2' />\n" +
                    "</configuration>"
            final GPathResult xml = new XmlSlurper().parseText(txt)

            // call
            ConfigurationTag.apply(xml).accept(jBossModule)

            // verify
            verify(jBossModule, times(1)).setModuleConfiguration(true)
            verify(jBossModule, times(1)).setDefaultLoader('_test-default1')
            assertEquals('[[name:_test-default1], [name:loader2]]', jBossModule.loaders.toString())
        }
    }

    @Test
    void testComplexLoaders() {
        Ver.values().each { version ->
            // init data
            jBossModule = spy(new JBossModule('test-module'))
            final String txt = "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<configuration xmlns='urn:jboss:module:" + version.number + "' default-loader='_test-default1'>\n" +
                    "  <loader name='_test-default1' />\n" +
                    "  <loader name='loader2' />\n" +
                    "  <loader name='loader3'>\n" +
                    "    <import>test-import</import>\n" +
                    "  </loader>\n" +
                    "  <loader name='loader4'>\n" +
                    "    <module-path name='test-path' />\n" +
                    "  </loader>\n" +
                    "</configuration>"
            final GPathResult xml = new XmlSlurper().parseText(txt)

            // call
            ConfigurationTag.apply(xml).accept(jBossModule)

            // verify
            verify(jBossModule, times(1)).setModuleConfiguration(true)
            verify(jBossModule, times(1)).setDefaultLoader('_test-default1')
            assertEquals('[[name:_test-default1], [name:loader2], [name:loader3, import:test-import], [name:loader4, module-path:test-path]]',
                    jBossModule.loaders.toString())
        }
    }
}
