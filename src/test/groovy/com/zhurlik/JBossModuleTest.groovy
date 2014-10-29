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
        module.ver = AbstractBuilder.Ver.V_1_1
        module.moduleName = 'my.module'
        module.mainClass = ''
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

        // 3
        module = new JBossModule('test-module-3')
        module.moduleName = 'test.module.3'
        module.mainClass = 'test.MainClass'
        assertEquals 'Case3:', "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module.3'>\n" +
                "  <main-class name='test.MainClass' />\n" +
                "</module>", module.moduleDescriptor

        // 4
        module = new JBossModule('test-module-4')
        module.moduleName = 'test.module.4'
        module.properties = [prop1: 'value1', prop2: 'value2', '':'']
        assertEquals 'Case4:', "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module.4'>\n" +
                "  <properties>\n" +
                "    <property name='prop1' value='value1' />\n" +
                "    <property name='prop2' value='value2' />\n" +
                "  </properties>\n" +
                "</module>", module.moduleDescriptor

    }
}
