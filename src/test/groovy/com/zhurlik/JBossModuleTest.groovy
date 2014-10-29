package com.zhurlik

import org.junit.Test

import static com.zhurlik.descriptor.AbstractBuilder.Ver.V_1_1
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
        module.ver = V_1_1
        module.moduleName = 'my.module'
        module.mainClass = ''
        module.slot = '1.0'
        assertEquals 'Case1:', "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='my.module' slot='1.0' />", module.moduleDescriptor
        assert module.valid

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
        assert module.valid

        // 3
        module = new JBossModule('test-module-3')
        module.moduleName = 'test.module.3'
        module.mainClass = 'test.MainClass'
        assertEquals 'Case3:', "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module.3'>\n" +
                "  <main-class name='test.MainClass' />\n" +
                "</module>", module.moduleDescriptor
        assert module.valid

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
        assert module.valid

        // 5
        module = new JBossModule('test-module-5')
        module.moduleName = 'test.module.5'
        module.resources = ['res1', [name: 'res2', path: 'path2'],[path: 'res2', filter:[include:'incl*', exclude: ['exclude1', 'exclude2']]]]
        assertEquals 'Case5:', "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module.5'>\n" +
                "  <resources>\n" +
                "    <resource-root path='res1' />\n" +
                "    <resource-root name='res2' path='path2' />\n" +
                "    <resource-root path='res2'>\n" +
                "      <filter>\n" +
                "        <include path='incl*' />\n" +
                "        <exclude-set>\n" +
                "          <path name='exclude1' />\n" +
                "          <path name='exclude2' />\n" +
                "        </exclude-set>\n" +
                "      </filter>\n" +
                "    </resource-root>\n" +
                "  </resources>\n" +
                "</module>", module.moduleDescriptor
        assert module.valid

        // 6
        module = new JBossModule('test-module-6')
        module.moduleName = 'test.module.6'
        module.dependencies = ['module1', 'module2',
                               [name: 'module3', slot: '1.3', services: 'none', optional: true, export: 'false',
                                    imports:[exclude: ['exclude1', 'exclude2']],
                                    exports:[include:'**']
                               ]
        ]
        assertEquals 'Case6:', "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module.6'>\n" +
                "  <dependencies>\n" +
                "    <module name='module1' />\n" +
                "    <module name='module2' />\n" +
                "    <module name='module3' slot='1.3' services='none' optional='true' export='false'>\n" +
                "      <imports>\n" +
                "        <exclude-set>\n" +
                "          <path name='exclude1' />\n" +
                "          <path name='exclude2' />\n" +
                "        </exclude-set>\n" +
                "      </imports>\n" +
                "      <exports>\n" +
                "        <include path='**' />\n" +
                "      </exports>\n" +
                "    </module>\n" +
                "  </dependencies>\n" +
                "</module>", module.moduleDescriptor
        assert module.valid
    }
}
