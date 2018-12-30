package com.github.zhurlik.descriptor

import com.github.zhurlik.extension.JBossModule
import org.junit.Before
import org.junit.Test

import static com.github.zhurlik.Ver.V_1_0
import static org.junit.Assert.assertEquals
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
        } catch (NullPointerException ex) {
            assertEquals('JBossModule is null', ex.getMessage())
        }

        JBossModule module = new JBossModule('test')
        try {
            module.setVer(V_1_0)
            xsd.getXmlDescriptor(module)
        } catch (NullPointerException ex) {
            assertEquals('Module name is null', ex.getMessage())
        }

        module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_0
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.0' name='test.module' />", xsd.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', V_1_0.getModulePath(module).toString()

        module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.moduleConfiguration = true
        module.defaultLoader = 'test_loader1'
        module.ver = V_1_0
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<configuration xmlns='urn:jboss:module:1.0' default-loader='test_loader1'>\n" +
                "  <loader name='test_loader1' />\n" +
                "</configuration>", xsd.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', V_1_0.getModulePath(module).toString()
        assertTrue 'Valid:', V_1_0.isValid(module.moduleDescriptor)
    }

    @Test
    void testValidate() throws Exception {
        final JBossModule module = new JBossModule('test')
        module.moduleName = 'test.module'

        assertTrue V_1_0.isValid(module.moduleDescriptor)

        // not valid
        assertTrue !V_1_0.isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.0' name1='test.module' />")
    }
}
