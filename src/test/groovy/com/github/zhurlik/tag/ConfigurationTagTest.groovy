package com.github.zhurlik.tag

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import com.github.zhurlik.tag.ConfigurationTag
import org.junit.Test

import java.util.function.Supplier

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class ConfigurationTagTest {

    @Test
    void testSingleLoader() {
        [Ver.V_1_0, Ver.V_1_1].each { version ->
            // init data
            final String txt = "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<configuration xmlns='urn:jboss:module:" + version.number + "' default-loader='test-default1'>\n" +
                    "  <loader name='test-default1' />\n" +
                    "</configuration>"

            // call
            ConfigurationTag.parse(txt)
                    .ifPresent({ final Supplier<JBossModule> supplier ->
                final JBossModule jBossModule = supplier.get()
                // verify
                assertTrue(jBossModule.isModuleConfiguration())
                assertEquals('test-default1', jBossModule.getDefaultLoader())
                assertEquals('[[name:test-default1]]', jBossModule.loaders.toString())
            }
            )
        }
    }

    @Test
    void testTwoLoaders() {
        [Ver.V_1_0, Ver.V_1_1].each { version ->
            // init data
            final String txt = "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<configuration xmlns='urn:jboss:module:" + version.number + "' default-loader='_test-default1'>\n" +
                    "  <loader name='_test-default1' />\n" +
                    "  <loader name='loader2' />\n" +
                    "</configuration>"

            // call
            ConfigurationTag.parse(txt)
                    .ifPresent({ final Supplier<JBossModule> supplier ->
                final JBossModule jBossModule = supplier.get()
                // verify
                assertTrue(jBossModule.isModuleConfiguration())
                assertEquals('_test-default1', jBossModule.getDefaultLoader())
                assertEquals('[[name:_test-default1], [name:loader2]]', jBossModule.loaders.toString())
            }
            )
        }
    }

    @Test
    void testComplexLoaders() {
        [Ver.V_1_0, Ver.V_1_1].each { version ->
            // init data
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

            // call
            ConfigurationTag.parse(txt).ifPresent({ final Supplier<JBossModule> supplier ->
                final JBossModule jBossModule = supplier.get()

                // verify
                assertTrue(jBossModule.isModuleConfiguration())
                assertEquals('_test-default1', jBossModule.getDefaultLoader())
                assertEquals('[[name:_test-default1], [name:loader2], [name:loader3, import:test-import], [name:loader4, module-path:test-path]]',
                        jBossModule.loaders.toString())
            }
            )
        }
    }

    @Test
    void testUnsupported() {
        (Ver.values() - [Ver.V_1_0, Ver.V_1_1]).each { version ->
            // init data
            final String txt = "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<configuration xmlns='urn:jboss:module:" + version.number + "' default-loader='test-default1'/>"

            // call, verify
            assertFalse(ConfigurationTag.parse(txt).isPresent())
        }
    }
}
