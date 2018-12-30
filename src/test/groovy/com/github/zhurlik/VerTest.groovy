package com.github.zhurlik

import org.junit.Test

import static junit.framework.Assert.assertEquals

class VerTest {
    @Test
    void testMain() {
        assertEquals('[V_1_0, V_1_1, V_1_2, V_1_3, V_1_5, V_1_6, V_1_7, V_1_8, V_1_9]', Ver.values().toArrayString())

        assertEquals('1.0', Ver.V_1_0.number)
        assertEquals('xsd/module-1_0.xsd', Ver.V_1_0.xsdPath)

        assertEquals('1.1', Ver.V_1_1.number)
        assertEquals('xsd/module-1_1.xsd', Ver.V_1_1.xsdPath)

        assertEquals('1.2', Ver.V_1_2.number)
        assertEquals('xsd/module-1_2.xsd', Ver.V_1_2.xsdPath)

        assertEquals('1.3', Ver.V_1_3.number)
        assertEquals('xsd/module-1_3.xsd', Ver.V_1_3.xsdPath)

        assertEquals('1.5', Ver.V_1_5.number)
        assertEquals('xsd/module-1_5.xsd', Ver.V_1_5.xsdPath)

        assertEquals('1.6', Ver.V_1_6.number)
        assertEquals('xsd/module-1_6.xsd', Ver.V_1_6.xsdPath)

        assertEquals('1.7', Ver.V_1_7.number)
        assertEquals('xsd/module-1_7.xsd', Ver.V_1_7.xsdPath)

        assertEquals('1.8', Ver.V_1_8.number)
        assertEquals('xsd/module-1_8.xsd', Ver.V_1_8.xsdPath)

        assertEquals('1.9', Ver.V_1_9.number)
        assertEquals('xsd/module-1_9.xsd', Ver.V_1_9.xsdPath)
    }
}
