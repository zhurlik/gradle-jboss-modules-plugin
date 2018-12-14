package com.github.zhurlik.descriptor

import com.github.zhurlik.extension.JBossModule
import org.junit.Before
import org.junit.Test

import static com.github.zhurlik.Ver.V_1_0
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

/**
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_0Test {

    private Xsd xsd

    @Before
    void setUp() throws Exception {
        xsd = V_1_0.xsd
        assertTrue xsd instanceof Xsd1_0
    }

    @Test
    void testGenerate() throws Exception {
        try {
            xsd.getXmlDescriptor(null)
            assertFalse false
        } catch (AssertionError ex) {
            assertTrue true
        }

        try {
            xsd.getXmlDescriptor(new JBossModule('test'))
            assertFalse false
        } catch (AssertionError ex) {
            assertTrue true
        }

        def module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_0
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.0' name='test.module' />", xsd.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', xsd.getPath(module)

        module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.moduleConfiguration = true
        module.defaultLoader = 'test_loader1'
        module.ver = V_1_0
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<configuration xmlns='urn:jboss:module:1.0' default-loader='test_loader1'>\n" +
                "  <loader name='test_loader1' />\n" +
                "</configuration>", xsd.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', xsd.getPath(module)
        assertTrue 'Valid:', xsd.getVersion().isValid(module.moduleDescriptor)
    }

    @Test
    void testValidate() throws Exception {
        final JBossModule module = new JBossModule('test')
        module.moduleName = 'test.module'

        assertTrue xsd.getVersion().isValid(module.moduleDescriptor)

        // not valid
        assertTrue !xsd.getVersion().isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.0' name1='test.module' />")
    }
}
