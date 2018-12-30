package com.github.zhurlik

import com.github.zhurlik.descriptor.Xsd1_0
import com.github.zhurlik.descriptor.Xsd1_1
import com.github.zhurlik.descriptor.Xsd1_2
import com.github.zhurlik.descriptor.Xsd1_3
import com.github.zhurlik.descriptor.Xsd1_5
import com.github.zhurlik.descriptor.Xsd1_6
import com.github.zhurlik.descriptor.Xsd1_7
import com.github.zhurlik.descriptor.Xsd1_8
import com.github.zhurlik.descriptor.Xsd1_9
import org.junit.Test

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue

class VerTest {
    @Test
    void testMain() {
        assertEquals('[V_1_0, V_1_1, V_1_2, V_1_3, V_1_5, V_1_6, V_1_7, V_1_8, V_1_9]', Ver.values().toArrayString())

        assertTrue(Ver.V_1_0.xsd instanceof Xsd1_0)
        assertEquals('1.0', Ver.V_1_0.number)
        assertEquals('xsd/module-1_0.xsd', Ver.V_1_0.xsdPath)

        assertTrue(Ver.V_1_1.xsd instanceof Xsd1_1)
        assertEquals('1.1', Ver.V_1_1.number)
        assertEquals('xsd/module-1_1.xsd', Ver.V_1_1.xsdPath)

        assertTrue(Ver.V_1_2.xsd instanceof Xsd1_2)
        assertEquals('1.2', Ver.V_1_2.number)
        assertEquals('xsd/module-1_2.xsd', Ver.V_1_2.xsdPath)

        assertTrue(Ver.V_1_3.xsd instanceof Xsd1_3)
        assertEquals('1.3', Ver.V_1_3.number)
        assertEquals('xsd/module-1_3.xsd', Ver.V_1_3.xsdPath)

        assertTrue(Ver.V_1_5.xsd instanceof Xsd1_5)
        assertEquals('1.5', Ver.V_1_5.number)
        assertEquals('xsd/module-1_5.xsd', Ver.V_1_5.xsdPath)

        assertTrue(Ver.V_1_6.xsd instanceof Xsd1_6)
        assertEquals('1.6', Ver.V_1_6.number)
        assertEquals('xsd/module-1_6.xsd', Ver.V_1_6.xsdPath)

        assertTrue(Ver.V_1_7.xsd instanceof Xsd1_7)
        assertEquals('1.7', Ver.V_1_7.number)
        assertEquals('xsd/module-1_7.xsd', Ver.V_1_7.xsdPath)

        assertTrue(Ver.V_1_8.xsd instanceof Xsd1_8)
        assertEquals('1.8', Ver.V_1_8.number)
        assertEquals('xsd/module-1_8.xsd', Ver.V_1_8.xsdPath)

        assertTrue(Ver.V_1_9.xsd instanceof Xsd1_9)
        assertEquals('1.9', Ver.V_1_9.number)
        assertEquals('xsd/module-1_9.xsd', Ver.V_1_9.xsdPath)
    }
}
