package com.zhurlik

import com.zhurlik.descriptor.AbstractBuilder
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
        // 1
        module = new JBossModule('testModule')
        module.setVer(AbstractBuilder.Ver.V_1_1)
        module.setModuleName('my.module')
        assertEquals 'Case1:', "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='my.module' />", module.moduleDescriptor
        // 2
        module = new JBossModule('spring-core')
        module.moduleName  = 'org.springframework.spring-core'
        module.resources = ['spring-core-3.2.5.RELEASE.jar']
        module.dependencies = ['javax.api',
                               'org.apache.commons.logging',
                               'org.jboss.vfs']

        assertEquals 'Case2:', "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='org.springframework.spring-core'>\n" +
                "  <resources>\n" +
                "    <resource-root path='spring-core-3.2.5.RELEASE.jar' />\n" +
                "  </resources>\n" +
                "  <dependencies>\n" +
                "    <module name='javax.api' />\n" +
                "    <module name='org.apache.commons.logging' />\n" +
                "    <module name='org.jboss.vfs' />\n" +
                "  </dependencies>\n" +
                "</module>", module.moduleDescriptor

    }
}
