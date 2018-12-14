package com.github.zhurlik.descriptor

import com.github.zhurlik.extension.JBossModule
import org.junit.Before
import org.junit.Test

import static com.github.zhurlik.Ver.V_1_2
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_2Test {

    private Xsd xsd

    @Before
    void setUp() throws Exception {
        xsd = V_1_2.xsd
        assertTrue xsd instanceof Xsd1_2
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
            xsd.getXmlDescriptor(new JBossModule('test').setVer(V_1_2))
            assertTrue false
        } catch (AssertionError ex) {
            assertTrue true
        }

        def module = new JBossModule('test')
        module.ver = V_1_2
        module.moduleName = 'test.module'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.2' name='test.module' />", xsd.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', xsd.getPath(module)

        module = new JBossModule('test')
        module.ver = V_1_2
        module.moduleName = 'test.module'
        module.moduleAlias = true
        module.targetName = 'target.name'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.2' name='test.module' target-name='target.name' />", xsd.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', xsd.getPath(module)

        module = new JBossModule('test')
        module.ver = V_1_2
        module.moduleName = 'test.module'
        module.moduleAbsent = true
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-absent xmlns='urn:jboss:module:1.2' name='test.module' />", xsd.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', xsd.getPath(module)

    }

    @Test
    void testValidate() throws Exception {
        def module = new JBossModule('test')
        module.ver = V_1_2
        module.moduleName = 'test.module'

        assertTrue xsd.getVersion().isValid(module.moduleDescriptor)

        // not valid
        assertTrue !xsd.getVersion().isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.2' name1='test.module' />")

        module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_2
        module.moduleAlias = true
        module.targetName = 'target.name'

        assertTrue xsd.getVersion().isValid(module.getModuleDescriptor())

        // not valid
        assertTrue !xsd.getVersion().isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.2' name='test.module'/>")
    }
}
