package com.github.zhurlik.descriptor

import com.github.zhurlik.extension.JBossModule
import org.junit.Before
import org.junit.Test

import static com.github.zhurlik.Ver.V_1_1
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

/**
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_1Test {

    private Xsd xsd

    @Before
    void setUp() throws Exception {
        xsd = V_1_1.xsd
        assertTrue xsd instanceof Xsd1_1
    }

    @Test
    void testGenerate() throws Exception {
        try {
            xsd.getXmlDescriptor(null)
            assertTrue false
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
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module' />", xsd.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', xsd.getPath(module)

        module = new JBossModule('test')
        module.ver = V_1_1
        module.moduleName = 'test.module'
        module.moduleAlias = true
        module.targetName = 'target.name'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.1' name='test.module' target-name='target.name' />", xsd.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', xsd.getPath(module)

        module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.moduleConfiguration = true
        module.defaultLoader = 'test_loader1'
        module.ver = V_1_1
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<configuration xmlns='urn:jboss:module:1.1' default-loader='test_loader1'>\n" +
                "  <loader name='test_loader1' />\n" +
                "</configuration>", xsd.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', xsd.getPath(module)
        assertTrue 'Valid:', xsd.getVersion().isValid(module.moduleDescriptor)

    }

    @Test
    void testValidate() throws Exception {
        final JBossModule module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_1
        assert xsd.getVersion().isValid(module.moduleDescriptor)

        // not valid
        assert !xsd.getVersion().isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name1='test.module' />")
    }
}
