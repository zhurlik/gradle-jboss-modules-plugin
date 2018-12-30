package com.github.zhurlik.tag

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import com.github.zhurlik.tag.ModuleAliasTag
import org.junit.Test

import java.util.function.Supplier

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class ModuleAliasTagTest {
    @Test
    void testSupported() {
        (Ver.values() - [Ver.V_1_0]).each { version ->
            // init data
            final String txt = "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<module-alias xmlns='urn:jboss:module:" + version.number + "' " +
                    "name='my.module' ${isSlotSupported(version) ? 'slot=\'1.0\'' : ''} target-name='testTarget' />"

            // call
            ModuleAliasTag.parse(txt).ifPresent({ final Supplier<JBossModule> supplier ->
                final JBossModule jBossModule = supplier.get()

                // verify
                assertTrue(jBossModule.isModuleAlias())
                assertEquals(isSlotSupported(version) ? '1.0' : null, jBossModule.getSlot())
                assertEquals('my.module', jBossModule.getModuleName())
                assertEquals('testTarget', jBossModule.getTargetName())
            })
        }
    }

    private static boolean isSlotSupported(final Ver version) {
        return version in [Ver.V_1_1, Ver.V_1_2, Ver.V_1_3, Ver.V_1_5, Ver.V_1_6]
    }

    @Test
    void testUnsupported() {
        // init data
        final String txt = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.0' target-name='testTarget' />"

        // call
        assertFalse(ModuleAliasTag.parse(txt).isPresent())
    }
}
