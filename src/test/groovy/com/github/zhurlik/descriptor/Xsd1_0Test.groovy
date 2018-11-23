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

    private Builder<JBossModule> builder

    @Before
    void setUp() throws Exception {
        builder = V_1_0.builder
        assertTrue builder instanceof Xsd1_0
    }

    @Test
    void testGenerate() throws Exception {
        try {
            builder.getXmlDescriptor(null)
            assertFalse false
        } catch (AssertionError ex) {
            assertTrue true
        }

        try {
            builder.getXmlDescriptor(new JBossModule('test'))
            assertFalse false
        } catch (AssertionError ex) {
            assertTrue true
        }

        def module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_0
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.0' name='test.module' />", builder.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', builder.getPath(module)

        module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.moduleConfiguration = true
        module.defaultLoader = 'test_loader1'
        module.ver = V_1_0
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<configuration xmlns='urn:jboss:module:1.0' default-loader='test_loader1'>\n" +
                "  <loader name='test_loader1' />\n" +
                "</configuration>", builder.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', builder.getPath(module)
        assertTrue 'Valid:', builder.getVersion().isValid(module.moduleDescriptor)
    }

    @Test
    void testValidate() throws Exception {
        final JBossModule module = new JBossModule('test')
        module.moduleName = 'test.module'

        assertTrue builder.getVersion().isValid(module.moduleDescriptor)

        // not valid
        assertTrue !builder.getVersion().isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.0' name1='test.module' />")
    }
}
