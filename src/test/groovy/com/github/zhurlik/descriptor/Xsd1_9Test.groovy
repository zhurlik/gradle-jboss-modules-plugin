package com.github.zhurlik.descriptor

import com.github.zhurlik.extension.JBossModule
import org.junit.Before
import org.junit.Test

import static com.github.zhurlik.Ver.V_1_9
import static org.junit.Assert.assertEquals

/**
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_9Test {

    Builder<JBossModule> builder

    @Before
    void setUp() throws Exception {
        builder = V_1_9.builder
        assert builder instanceof Xsd1_9
    }

    @Test
    void testGenerate() throws Exception {
        try {
            builder.getXmlDescriptor(null)
            assert false
        } catch (AssertionError ex) {
            assert true
        }

        try {

            final JBossModule m = new JBossModule('test')
            m.setVer(V_1_9)
            m.setSlot("deprecated")
            builder.getXmlDescriptor()
            assert false
        } catch (AssertionError ex) {
            assert true
        }

        def module = new JBossModule('test')
        module.ver = V_1_9
        module.slot = 'deprecated'
        module.moduleName = 'test.module'
        module.version = '123-456.789'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.9' name='test.module' version='123-456.789' />", builder.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/123-456.789', builder.getPath(module)

        module = new JBossModule('test')
        module.ver = V_1_9
        module.moduleName = 'test.module'
        module.moduleAlias = true
        module.targetName = 'target.name'
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.9' name='test.module' target-name='target.name' />", builder.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', builder.getPath(module)

        module = new JBossModule('test')
        module.ver = V_1_9
        module.moduleName = 'test.module'
        module.moduleAbsent = true
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-absent xmlns='urn:jboss:module:1.9' name='test.module' />", builder.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', builder.getPath(module)

        module = new JBossModule('test')
        module.ver = V_1_9
        module.moduleName = 'test.module'
        module.moduleAbsent = true
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-absent xmlns='urn:jboss:module:1.9' name='test.module' />", builder.getXmlDescriptor(module)
        assertEquals 'modules/system/layers/base/test/module/main', builder.getPath(module)
    }

    @Test
    void testValidate() throws Exception {
        def module = new JBossModule('test')
        module.ver = V_1_9
        module.moduleName = 'test.module'

        assert builder.isValid(module.moduleDescriptor)

        // not valid
        assert !builder.isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.9' name1='test.module' />")

        module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_9
        module.moduleAlias = true
        module.targetName = 'target.name'

        assert builder.isValid(module.getModuleDescriptor())

        // not valid
        assert !builder.isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:1.9' name='test.module'/>")
    }
}
