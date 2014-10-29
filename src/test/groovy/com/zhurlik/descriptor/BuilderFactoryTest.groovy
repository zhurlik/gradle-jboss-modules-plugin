package com.zhurlik.descriptor

import com.zhurlik.JBossModule
import org.junit.Test

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
        def none = BuilderFactory.NONE
        assertNotNull none
        assertNull none.xsd
        try {
            none.getXmlDescriptor(new JBossModule('test'))
            assert false
        } catch (UnsupportedOperationException ex) {
            assert true
        }

        def ver1_0 = BuilderFactory.<JBossModule>getBuilder(AbstractBuilder.Ver.V_1_0)
        def ver1_1 = BuilderFactory.<JBossModule>getBuilder(AbstractBuilder.Ver.V_1_1)
        def ver1_2 = BuilderFactory.<JBossModule>getBuilder(AbstractBuilder.Ver.V_1_2)
        def ver1_3 = BuilderFactory.<JBossModule>getBuilder(AbstractBuilder.Ver.V_1_3)

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

        assertEquals ver1_0, BuilderFactory.<JBossModule>getBuilder(AbstractBuilder.Ver.V_1_0)
        assertEquals ver1_1, BuilderFactory.<JBossModule>getBuilder(AbstractBuilder.Ver.V_1_1)
        assertEquals ver1_2, BuilderFactory.<JBossModule>getBuilder(AbstractBuilder.Ver.V_1_2)
        assertEquals ver1_3, BuilderFactory.<JBossModule>getBuilder(AbstractBuilder.Ver.V_1_3)

        assertEquals 'xsd/module-1_0.xsd', AbstractBuilder.Ver.V_1_0.xsd
        assertEquals '1.0', AbstractBuilder.Ver.V_1_0.version
        assertEquals 'xsd/module-1_1.xsd', AbstractBuilder.Ver.V_1_1.xsd
        assertEquals '1.1', AbstractBuilder.Ver.V_1_1.version
        assertEquals 'xsd/module-1_2.xsd', AbstractBuilder.Ver.V_1_2.xsd
        assertEquals '1.2', AbstractBuilder.Ver.V_1_2.version
        assertEquals 'xsd/module-1_3.xsd', AbstractBuilder.Ver.V_1_3.xsd
        assertEquals '1.3', AbstractBuilder.Ver.V_1_3.version
    }
}
