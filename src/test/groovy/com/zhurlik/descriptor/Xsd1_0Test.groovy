package com.zhurlik.descriptor

import com.zhurlik.extension.JBossModule
import static com.zhurlik.Ver.V_1_0
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_0Test {

    def Builder<JBossModule> builder

    @Before
    public void setUp() throws Exception {
        builder = BuilderFactory.getBuilder(V_1_0)
        assert builder instanceof Xsd1_0
    }

    @Test
    public void testGenerate() throws Exception {
        try {
            builder.getXmlDescriptor(null)
            assert false
        } catch (AssertionError ex) {
            assert true
        }

        try {
            builder.getXmlDescriptor(new JBossModule('test'))
            assert false
        } catch (AssertionError ex) {
            assert true
        }

        def module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_0
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.0' name='test.module' />", builder.getXmlDescriptor(module)
        assertEquals 'modules/test/module/main', builder.getPath(module)
    }

    @Test
    public void testValidate() throws Exception {
        def module = new JBossModule('test')
        module.moduleName = 'test.module'
        module.ver = V_1_0

        assert builder.isValid(module.moduleDescriptor)

        // not valid
        assert !builder.isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.0' name1='test.module' />")
    }
}
