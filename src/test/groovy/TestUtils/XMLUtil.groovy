package TestUtils

import static org.junit.Assert.assertEquals

/**
 * Provides specific asserts to compare XML strings.
 */
class XMLUtil {

    static void assertXMLStrings(String expectedString, String actualString) {
        assertXMLStrings(null, expectedString, actualString)
    }

    /**
     * Parses and compares two XML strings.
     * @param message to print when assertion fails
     * @param expectedString expected XML string
     * @param actualString actual XML string
     */
    static void assertXMLStrings(String message, String expectedString, String actualString) {
        def expected = new XmlSlurper().parseText(expectedString)
        def actual = new XmlSlurper().parseText(actualString)
        assertEquals(message, expected, actual)
    }
}
