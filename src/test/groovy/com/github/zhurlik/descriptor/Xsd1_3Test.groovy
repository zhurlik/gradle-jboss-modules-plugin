package com.github.zhurlik.descriptor

import com.github.zhurlik.extension.JBossModule
import org.junit.Before
import org.junit.Test

import static com.github.zhurlik.Ver.V_1_3
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_3Test {

    private Xsd xsd

    @Before
    void setUp() throws Exception {
        xsd = V_1_3.xsd
        assertTrue xsd instanceof Xsd1_3
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
            module.setVer(V_1_3)
            xsd.getXmlDescriptor(module)
        } catch (NullPointerException ex) {
            assertEquals('Module name is null', ex.getMessage())
        }

        module = new JBossModule('test')
        module.ver = V_1_3
        module.moduleName = 'test.module'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.3' name='test.module' />", xsd.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', xsd.getPath(module)

        module = new JBossModule('test')
        module.ver = V_1_3
        module.moduleName = 'test.module'
        module.moduleAlias = true
        module.targetName = 'target.name'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.3' name='test.module' target-name='target.name' />", xsd.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', xsd.getPath(module)

        module = new JBossModule('test')
        module.ver = V_1_3
        module.moduleName = 'test.module'
        module.moduleAbsent = true
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-absent xmlns='urn:jboss:module:1.3' name='test.module' />", xsd.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', xsd.getPath(module)

    }

    @Test
    void testValidate() throws Exception {
        def module = new JBossModule('test')
        module.ver = V_1_3
        module.moduleName = 'test.module'

        assertTrue V_1_3.isValid(module.moduleDescriptor)

        // not valid
        assertTrue !V_1_3.isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.3' name1='test.module' />")

        module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_3
        module.moduleAlias = true
        module.targetName = 'target.name'

        assertTrue V_1_3.isValid(module.getModuleDescriptor())

        // not valid
        assertTrue !V_1_3.isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.3' name='test.module'/>")

    }
}
