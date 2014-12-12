package com.zhurlik.descriptor

import com.zhurlik.extension.JBossModule
import org.junit.Test

import static com.zhurlik.Ver.V_1_0
import static com.zhurlik.Ver.V_1_1
import static com.zhurlik.Ver.V_1_2
import static com.zhurlik.Ver.V_1_3
import static com.zhurlik.descriptor.BuilderFactory.NONE
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

/**
 * To check {@link BuilderFactory} class
 *
 * @author zhurlik@gmail.com
 */
class BuilderFactoryTest {

    @Test
    public void testMain() throws Exception {
        final Builder<JBossModule> none = BuilderFactory.getBuilder(null)
        assertNotNull none
        assertEquals NONE, none
        assertNull none.xsd
        assertNull none.getPath(null)
        assertNull none.getVersion()

        try {
            none.getXmlDescriptor(new JBossModule('test'))
            assertFalse false
        } catch (UnsupportedOperationException ex) {
            assertTrue true
        }

        final Builder<JBossModule> ver1_0 = BuilderFactory.getBuilder(V_1_0)
        final Builder<JBossModule> ver1_1 = BuilderFactory.getBuilder(V_1_1)
        final Builder<JBossModule> ver1_2 = BuilderFactory.getBuilder(V_1_2)
        final Builder<JBossModule> ver1_3 = BuilderFactory.getBuilder(V_1_3)

        assertTrue ver1_0 instanceof Xsd1_0
        assertTrue ver1_1 instanceof Xsd1_1
        assertTrue ver1_2 instanceof Xsd1_2
        assertTrue ver1_3 instanceof Xsd1_3

        assertNotEquals ver1_0, ver1_1
        assertNotEquals ver1_0, ver1_2
        assertNotEquals ver1_0, ver1_3
        assertNotEquals ver1_1, ver1_2
        assertNotEquals ver1_1, ver1_3
        assertNotEquals ver1_2, ver1_3

        assertEquals ver1_0, BuilderFactory.getBuilder(V_1_0)
        assertEquals ver1_1, BuilderFactory.getBuilder(V_1_1)
        assertEquals ver1_2, BuilderFactory.getBuilder(V_1_2)
        assertEquals ver1_3, BuilderFactory.getBuilder(V_1_3)

        assertEquals 'xsd/module-1_0.xsd', V_1_0.xsd
        assertEquals '1.0', V_1_0.number
        assertEquals 'xsd/module-1_1.xsd', V_1_1.xsd
        assertEquals '1.1', V_1_1.number
        assertEquals 'xsd/module-1_2.xsd', V_1_2.xsd
        assertEquals '1.2', V_1_2.number
        assertEquals 'xsd/module-1_3.xsd', V_1_3.xsd
        assertEquals '1.3', V_1_3.number
    }
}
