package TestUtils

import static org.junit.Assert.assertEquals

class XMLUtil {

    static void assertXMLStrings(String expectedString, String actualString) {
        assertXMLStrings(null, expectedString, actualString)
    }

    static void assertXMLStrings(String message, String expectedString, String actualString) {
        def expected = new XmlSlurper().parseText(expectedString)
        def actual = new XmlSlurper().parseText(actualString)
        assertEquals(message, expected, actual)
    }
}
