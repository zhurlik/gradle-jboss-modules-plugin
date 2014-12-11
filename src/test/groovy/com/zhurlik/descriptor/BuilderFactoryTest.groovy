package com.zhurlik.descriptor

import com.zhurlik.extension.JBossModule
import org.junit.Test

import static com.zhurlik.Ver.V_1_0
import static com.zhurlik.Ver.V_1_1
import static com.zhurlik.Ver.V_1_2
import static com.zhurlik.Ver.V_1_3
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull

/**
 * To check {@link BuilderFactory} class
 *
 * @author zhurlik@gmail.com
 */
class BuilderFactoryTest {

    @Test
    public void testMain() throws Exception {
        def none = BuilderFactory.getBuilder(null)
        assertNotNull none
        assertNull none.xsd
        assertNull none.getPath(null)
        assertNull none.getVersion()

        try {
            none.getXmlDescriptor(new JBossModule('test'))
            assert false
        } catch (UnsupportedOperationException ex) {
            assert true
        }

        def ver1_0 = BuilderFactory.getBuilder(V_1_0)
        def ver1_1 = BuilderFactory.getBuilder(V_1_1)
        def ver1_2 = BuilderFactory.getBuilder(V_1_2)
        def ver1_3 = BuilderFactory.getBuilder(V_1_3)

        assert ver1_0 instanceof Xsd1_0
        assert ver1_1 instanceof Xsd1_1
        assert ver1_2 instanceof Xsd1_2
        assert ver1_3 instanceof Xsd1_3

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
