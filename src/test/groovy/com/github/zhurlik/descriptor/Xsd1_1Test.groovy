package com.github.zhurlik.descriptor

import com.github.zhurlik.extension.JBossModule
import org.junit.Test

import static com.github.zhurlik.Ver.V_1_1
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_1Test {

    @Test
    void testGenerate() throws Exception {
        try {
            V_1_1.getXmlDescriptor(null)
        } catch (NullPointerException ex) {
            assertEquals('JBossModule is null', ex.getMessage())
        }

        try {
            V_1_1.getXmlDescriptor(new JBossModule('test'))
        } catch (NullPointerException ex) {
            assertEquals('Module name is null', ex.getMessage())
        }

        def module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_1
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module' />", V_1_1.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', V_1_1.getModulePath(module).toString()

        module = new JBossModule('test')
        module.ver = V_1_1
        module.moduleName = 'test.module'
        module.moduleAlias = true
        module.targetName = 'target.name'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.1' name='test.module' target-name='target.name' />", V_1_1.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', V_1_1.getModulePath(module).toString()

        module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.moduleConfiguration = true
        module.defaultLoader = 'test_loader1'
        module.ver = V_1_1
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<configuration xmlns='urn:jboss:module:1.1' default-loader='test_loader1'>\n" +
                "  <loader name='test_loader1' />\n" +
                "</configuration>", V_1_1.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', V_1_1.getModulePath(module).toString()
        assertTrue 'Valid:', V_1_1.isValid(module.moduleDescriptor)

    }

    @Test
    void testValidate() throws Exception {
        final JBossModule module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_1
        assertTrue V_1_1.isValid(module.moduleDescriptor)

        // not valid
        assertTrue !V_1_1.isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name1='test.module' />")
    }
}
