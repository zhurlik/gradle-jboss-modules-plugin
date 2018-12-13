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

    private Builder builder

    @Before
    void setUp() throws Exception {
        builder = V_1_3.builder
        assertTrue builder instanceof Xsd1_3
    }

    @Test
    void testGenerate() throws Exception {
        try {
            builder.getXmlDescriptor(null)
            assertTrue false
        } catch (AssertionError ex) {
            assertTrue true
        }

        try {
            builder.getXmlDescriptor(new JBossModule('test').setVer(V_1_3))
            assert false
        } catch (AssertionError ex) {
            assert true
        }

        def module = new JBossModule('test')
        module.ver = V_1_3
        module.moduleName = 'test.module'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.3' name='test.module' />", builder.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', builder.getPath(module)

        module = new JBossModule('test')
        module.ver = V_1_3
        module.moduleName = 'test.module'
        module.moduleAlias = true
        module.targetName = 'target.name'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.3' name='test.module' target-name='target.name' />", builder.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', builder.getPath(module)

        module = new JBossModule('test')
        module.ver = V_1_3
        module.moduleName = 'test.module'
        module.moduleAbsent = true
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-absent xmlns='urn:jboss:module:1.3' name='test.module' />", builder.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', builder.getPath(module)

    }

    @Test
    void testValidate() throws Exception {
        def module = new JBossModule('test')
        module.ver = V_1_3
        module.moduleName = 'test.module'

        assertTrue builder.getVersion().isValid(module.moduleDescriptor)

        // not valid
        assertTrue !builder.getVersion().isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.3' name1='test.module' />")

        module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_3
        module.moduleAlias = true
        module.targetName = 'target.name'

        assertTrue builder.getVersion().isValid(module.getModuleDescriptor())

        // not valid
        assertTrue !builder.getVersion().isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.3' name='test.module'/>")

    }
}
