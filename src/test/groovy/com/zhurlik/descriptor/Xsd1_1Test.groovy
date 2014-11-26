package com.zhurlik.descriptor

import com.zhurlik.extension.JBossModule
import org.junit.Before
import org.junit.Test

import static com.zhurlik.Ver.V_1_1
import static org.junit.Assert.assertEquals


/**
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_1Test {

    def Builder<JBossModule> builder

    @Before
    public void setUp() throws Exception {
        builder = BuilderFactory.getBuilder(V_1_1)
        assert builder instanceof Xsd1_1
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
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module' />", builder.getXmlDescriptor(module)
    }

    @Test
    public void testVallidate() throws Exception {
        def module = new JBossModule('test')
        module.moduleName = 'test.module'

        assert builder.isValid(module.moduleDescriptor)

        // not valid
        assert !builder.isValid("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name1='test.module' />")
    }
}
