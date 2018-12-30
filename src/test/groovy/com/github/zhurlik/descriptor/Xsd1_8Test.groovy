package com.github.zhurlik.descriptor

import com.github.zhurlik.extension.JBossModule
import org.junit.Test

import static com.github.zhurlik.Ver.V_1_8
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_8Test {

    @Test
    void testGenerate() throws Exception {
        try {
            V_1_8.getXmlDescriptor(null)
        } catch (NullPointerException ex) {
            assertEquals('JBossModule is null', ex.getMessage())
        }

        JBossModule module = new JBossModule('test')
        try {
            module.setVer(V_1_8)
            module.setSlot('deprecated')
            V_1_8.getXmlDescriptor(module)
        } catch (NullPointerException ex) {
            assertEquals('Module name is null', ex.getMessage())
        }

        module = new JBossModule('test')
        module.ver = V_1_8
        module.slot = 'deprecated'
        module.moduleName = 'test.module'
        module.version =  '123-456.789'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.8' name='test.module' version='123-456.789' />", V_1_8.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/123-456.789', V_1_8.getModulePath(module).toString()

        module = new JBossModule('test')
        module.ver = V_1_8
        module.moduleName = 'test.module'
        module.moduleAlias = true
        module.targetName = 'target.name'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.8' name='test.module' target-name='target.name' />", V_1_8.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', V_1_8.getModulePath(module).toString()

        module = new JBossModule('test')
        module.ver = V_1_8
        module.moduleName = 'test.module'
        module.moduleAbsent = true
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-absent xmlns='urn:jboss:module:1.8' name='test.module' />", V_1_8.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', V_1_8.getModulePath(module).toString()

        module = new JBossModule('test')
        module.ver = V_1_8
        module.moduleName = 'test.module'
        module.moduleAbsent = true
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-absent xmlns='urn:jboss:module:1.8' name='test.module' />", V_1_8.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', V_1_8.getModulePath(module).toString()
    }

    @Test
    void testValidate() throws Exception {
        def module = new JBossModule('test')
        module.ver = V_1_8
        module.moduleName = 'test.module'

        assertTrue V_1_8.isValid(module.moduleDescriptor)

        // not valid
        assertTrue !V_1_8.isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.8' name1='test.module' />")

        module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_8
        module.moduleAlias = true
        module.targetName = 'target.name'

        assertTrue V_1_8.isValid(module.getModuleDescriptor())

        // not valid
        assertTrue !V_1_8.isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.8' name='test.module'/>")
    }
}
