package com.zhurlik

import com.zhurlik.descriptor.IBuilder
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * Unit test to check all cases to create JBoss Module.
 *
 * @author zhurlik@gmail.com
 */
class JBossModuleTest {

    private JBossModule module

    @Test
    public void testName() throws Exception {
        module = new JBossModule('testModule')
        module.setVer(IBuilder.Ver.V_1_1)
        module.setModuleName('my.module')
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='my.module' />", module.getModuleDescriptor()
    }
}
