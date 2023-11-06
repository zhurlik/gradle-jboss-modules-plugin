package com.github.zhurlik.descriptor

import static org.junit.Assert.assertEquals

class XMLUtil {

    static void assertXMLStrings(String expectedString, String actualString) {
        def expected = new XmlSlurper().parseText(expectedString)
        def actual = new XmlSlurper().parseText(actualString)
        assertEquals expected, actual
    }
}
