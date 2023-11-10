package com.github.zhurlik.extension

import TestUtils.XMLUtil
import com.github.zhurlik.Ver
import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

import static java.io.File.separator
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

/**
 * A common stuff for testing.
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
abstract class BasicJBossModuleTest {
    protected JBossModule module
    protected final File projectDir = new File(getClass().getClassLoader().getResource('').toURI().path + separator + 'projectTest')
    protected String prefix = ""

    @Before
    void setUp() throws Exception {
        if (!projectDir.exists()) {
            assertTrue projectDir.mkdir()
        }
    }

    @After
    void tearDown() throws Exception {
        if (projectDir.exists() && projectDir.isDirectory()) {
            assertTrue projectDir.deleteDir()
        }
    }

    @Test
    void testValidation() throws Exception {
        try {
            module  = new JBossModule("wrong")
            module.slot = '~#?!'
            assertFalse true
        } catch (NullPointerException ex) {
            assertEquals('Slot must be: [-a-zA-Z0-9_+*.]+', ex.getMessage())
        }

        try {
            module  = new JBossModule("wrong")
            module.moduleName = '~#?!'
            assertFalse true
        } catch (NullPointerException ex) {
            assertEquals('Module Name must be: ' +
                    '[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?(\\.[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?)*',
                    ex.getMessage())
        }

        try {
            module  = new JBossModule("wrong")
            module.defaultLoader = '~#?!'
            assertFalse true
        } catch (NullPointerException ex) {
            assertEquals('Default-Loader must be: [-0-9a-zA-Z_]+', ex.getMessage())
        }

        try {
            module  = new JBossModule("wrong")
            module.targetName = '~#?!'
            assertFalse true
        } catch (NullPointerException ex) {
            assertEquals('Target Name must be: ' +
                    '[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?(\\.[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?)*',
                    ex.getMessage())
        }
    }

    @Test
    void testResources() throws Exception {
        // 1
        module = new JBossModule('testModule')
        module.ver = getVersion()
        module.moduleName = 'my.module'
        module.slot = '1.0'
        module.resources = [
                [name: 'testName', path: 'testPath', filter: [include: 'all', exclude: 'not this']]
        ]

        String xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:" + getVersion().number + "' name='my.module' slot='1.0'>\n" +
                "  <resources>\n" +
                "    <resource-root name='testName' path='testPath'>\n" +
                "      <filter>\n" +
                "        <include path='all' />\n" +
                "        <exclude path='not this' />\n" +
                "      </filter>\n" +
                "    </resource-root>\n" +
                "  </resources>\n" +
                "</module>"
        XMLUtil.assertXMLStrings 'Case1:', xml, module.moduleDescriptor
        assertTrue module.valid
        XMLUtil.assertXMLStrings 'Reverse:', xml, Ver.makeModule(xml).moduleDescriptor

        // 2
        module = new JBossModule('testModule')
        module.ver = getVersion()
        module.moduleName = 'my.module1'
        module.slot = '1.1'
        module.resources = [
                [name: 'testName', path: 'testPath', filter: [include: ['11', '22' ], exclude: ['not aa', 'not bb']]]
        ]

        xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:" + getVersion().number + "' name='my.module1' slot='1.1'>\n" +
                "  <resources>\n" +
                "    <resource-root name='testName' path='testPath'>\n" +
                "      <filter>\n" +
                "        <include-set>\n" +
                "          <path name='11' />\n" +
                "          <path name='22' />\n" +
                "        </include-set>\n" +
                "        <exclude-set>\n" +
                "          <path name='not aa' />\n" +
                "          <path name='not bb' />\n" +
                "        </exclude-set>\n" +
                "      </filter>\n" +
                "    </resource-root>\n" +
                "  </resources>\n" +
                "</module>"
        XMLUtil.assertXMLStrings 'Case2:', xml, module.moduleDescriptor
        assertTrue module.valid
        XMLUtil.assertXMLStrings 'Reverse:', xml, Ver.makeModule(xml).moduleDescriptor

    }

    @Test
    void testExports() throws Exception {
        // 1
        module = new JBossModule('testModule')
        module.ver = getVersion()
        module.moduleName = 'my.module'
        module.slot = '1.0'
        module.exports = [
                include: 'all',
                exclude: ['not this', 'not 1']
        ]

        String xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:" + getVersion().number + "' name='my.module'${isSlotSupported() ? ' slot=\'1.0\'' : ''}>\n" +
                "  <exports>\n" +
                "    <include path='all' />\n" +
                "    <exclude-set>\n" +
                "      <path name='not this' />\n" +
                "      <path name='not 1' />\n" +
                "    </exclude-set>\n" +
                "  </exports>\n" +
                "</module>"
        XMLUtil.assertXMLStrings 'Case1:', xml, module.moduleDescriptor
        assertTrue module.valid
        XMLUtil.assertXMLStrings 'Reverse:', xml, Ver.makeModule(xml).moduleDescriptor

        // 2
        module = new JBossModule('testModule1')
        module.ver = getVersion()
        module.moduleName = 'my.module1'
        module.slot = '1.1'
        final String t = 123
        module.exports = [
                include: ['all', "$t"],
                exclude: 'not this'
        ]

        xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:" + getVersion().number + "' name='my.module1'${isSlotSupported() ? ' slot=\'1.1\'' : ''}>\n" +
                "  <exports>\n" +
                "    <include-set>\n" +
                "      <path name='all' />\n" +
                "      <path name='123' />\n" +
                "    </include-set>\n" +
                "    <exclude path='not this' />\n" +
                "  </exports>\n" +
                "</module>"
        XMLUtil.assertXMLStrings 'Case2:', xml, module.moduleDescriptor
        assertTrue module.valid
        XMLUtil.assertXMLStrings 'Reverse:', xml, Ver.makeModule(xml).moduleDescriptor
    }

    @Test
    void testPermissionsTag() throws Exception {
        // 1
        module = new JBossModule('testModule')
        module.ver = getVersion()
        module.moduleName = 'my.module'
        module.slot = '1.0'
        module.permissions = [
                'permission0',
                [permission: 'permission1'],
                [permission: 'permission2', name: 'test-name', actions: 'test-actions']
        ]

        String xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:" + getVersion().number + "' name='my.module'${isSlotSupported() ? ' slot=\'1.0\'' : ''}>\n" +
                "  <permissions>\n" +
                "    <grant permission='permission0' />\n" +
                "    <grant permission='permission1' />\n" +
                "    <grant actions='test-actions' name='test-name' permission='permission2' />\n" +
                "  </permissions>\n" +
                "</module>"
        XMLUtil.assertXMLStrings 'Case1:', xml, module.moduleDescriptor
        assertTrue module.valid
        XMLUtil.assertXMLStrings 'Reverse:', xml, Ver.makeModule(xml).moduleDescriptor
    }


    @Test
    void testConfigurationTag() throws Exception {
        // 1
        try {
            module = new JBossModule('testModule')
            module.ver = getVersion()
            module.moduleName = 'my.module'
            module.moduleConfiguration = true
            assertTrue false
        } catch (AssertionError ex) {
            assertTrue 'Case1:', true
        }

        //2
        module = new JBossModule('testModule')
        module.ver = getVersion()
        module.moduleName = 'my.module'
        module.moduleConfiguration = true
        module.defaultLoader = 'test-default1'
        String xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<configuration xmlns='urn:jboss:module:" + getVersion().number + "' default-loader='test-default1'>\n" +
                "  <loader name='test-default1' />\n" +
                "</configuration>"
        assertEquals 'Case2:', xml, module.moduleDescriptor
        assertTrue module.valid
        assertEquals 'Reverse:', xml, Ver.makeModule(xml).moduleDescriptor

        //3
        module = new JBossModule('testModule')
        module.ver = getVersion()
        module.moduleName = 'my.module'
        module.moduleConfiguration = true
        module.defaultLoader = '_test-default1'
        module.loaders = ['_test-default1', [name: 'loader2']]
        xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<configuration xmlns='urn:jboss:module:" + getVersion().number + "' default-loader='_test-default1'>\n" +
                "  <loader name='_test-default1' />\n" +
                "  <loader name='loader2' />\n" +
                "</configuration>"
        assertEquals 'Case3:', xml, module.moduleDescriptor
        assertTrue module.valid
        assertEquals 'Reverse:', xml, Ver.makeModule(xml).moduleDescriptor

        //4
        module = new JBossModule('testModule')
        module.ver = getVersion()
        module.moduleName = 'my.module'
        module.moduleConfiguration = true
        module.defaultLoader = '_test-default1'
        module.loaders = ['_test-default1', [name: 'loader2'], [name: 'loader3', import: 'test-import'], [name: 'loader4', 'module-path': 'test-path']]
        xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<configuration xmlns='urn:jboss:module:" + getVersion().number + "' default-loader='_test-default1'>\n" +
                "  <loader name='_test-default1' />\n" +
                "  <loader name='loader2' />\n" +
                "  <loader name='loader3'>\n" +
                "    <import>test-import</import>\n" +
                "  </loader>\n" +
                "  <loader name='loader4'>\n" +
                "    <module-path name='test-path' />\n" +
                "  </loader>\n" +
                "</configuration>"
        assertEquals 'Case4:', xml, module.moduleDescriptor
        assertTrue module.valid
        assertEquals 'Reverse:', xml, Ver.makeModule(xml).moduleDescriptor
    }

    @Test
    /**
     * Test for downloading a resource and saving a jboss module
     */
    void testMake() throws Exception {
        log.debug '>> Test for making a module and saving locally...'
        // 1
        module = new JBossModule('log4j')
        module.ver = getVersion()
        // only for testServer
        module.servers = ['testServer']
        module.moduleName = 'org.apache.log4j'
        module.resources = ['.', 'log4j-1.2.17.jar']

        // empty project with all needed
        final Project project = ProjectBuilder.builder()
                .withName('test-project')
                .withProjectDir(projectDir)
                .build()
        project.apply plugin: 'com.github.zhurlik.jbossmodules'

        // using a maven to download jar files
        project.repositories {
            mavenCentral()
        }

        // to have a reference for jar file
        project.dependencies {
            jbossmodules 'log4j:log4j:1.2.17'

        }

        // describe an instance of jboss server
        final testVer = getVersion()
        project.jbossrepos {
            testServer {
                home = projectDir.path + separator + "testServer"
                version = testVer
            }
            testServer1 {
                home = projectDir.path + separator + "testServer1"
            }
        }

        // test call
        module.makeLocally(project)

        // nothing for testServer1
        assertNull getClass().getClassLoader().getResource('projectTest/build/install/testServer1/modules/' + prefix + 'org/apache/log4j/main/log4j-1.2.17.jar')
        assertNull getClass().getClassLoader().getResource('projectTest/build/install/testServer1/modules/' + prefix + 'org/apache/log4j/main/module.xml')

        assertTrue new File(getClass().getClassLoader().getResource('projectTest/build/install/testServer/modules/' + prefix + 'org/apache/log4j/main/log4j-1.2.17.jar').toURI().path).exists()
        assertTrue new File(getClass().getClassLoader().getResource('projectTest/build/install/testServer/modules/' + prefix + 'org/apache/log4j/main/module.xml').toURI().path).exists()
        XMLUtil.assertXMLStrings 'Module Descriptor:', "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:" + getVersion().number + "' name='org.apache.log4j'>\n" +
                "  <resources>\n" +
                "    <resource-root path='.' />\n" +
                "    <resource-root path='log4j-1.2.17.jar' />\n" +
                "  </resources>\n" +
                "</module>", new File(getClass().getClassLoader().getResource('projectTest/build/install/testServer/modules/' + prefix + 'org/apache/log4j/main/module.xml').toURI().path).text
    }

    @Test
    void testModuleAliasTag() throws Exception {
        // 1
        module = new JBossModule('testModule')
        module.ver = getVersion()
        module.moduleName = 'my.module'
        module.slot = '1.0'
        module.targetName = 'testTarget'
        module.setModuleAlias(true)
        String xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module-alias xmlns='urn:jboss:module:" + getVersion().number + "' name='my.module' ${isSlotSupported() ? 'slot=\'1.0\' ' : ''}target-name='testTarget' />"
        assertEquals 'Case1:', xml, module.moduleDescriptor
        assertTrue module.valid
        assertEquals 'Reverse:', xml, Ver.makeModule(xml).moduleDescriptor
    }

    @Test
    void testDependencies() throws Exception {
        //1
        module = new JBossModule('testModule')
        module.ver = getVersion()
        module.moduleName = 'my.module'
        module.dependencies = [[type: 'system']]
        String xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:" + getVersion().number + "' name='my.module'>\n" +
                "  <dependencies>\n" +
                "    <system />\n" +
                "  </dependencies>\n" +
                "</module>"
        XMLUtil.assertXMLStrings 'Case1:', xml, module.moduleDescriptor
        assertTrue !module.valid

        // 2
        module = new JBossModule('testModule')
        module.ver = getVersion()
        module.moduleName = 'my.module'
        String t = "**"
        module.dependencies = [
                'module1', 'module2',
                [name   : 'module3', slot: '1.3', services: 'none', optional: true, export: 'false',
                 imports: [exclude: ['exclude1', 'exclude2'], include: "$t"],
                 exports: [include: "$t", exclude: ['not a', 'not b']]
                ],
                [name: 'module4', exports:[include: ['1111', '222'], exclude: "all$t"]],
                [name: 'module5', imports:[include: ['1111', '222'], exclude: "all$t"]],
                [type: 'system', paths: 'test-path'],
                [type: 'system', export: true, paths: ['path1', 'path2'], exports: [exclude: ['exclude1', 'exclude2'], include: "but include $t"]],
                [type: 'system', export: false, paths: 'test-path', exports: [include: ['11', '22'], exclude: "but not $t"]]
        ]
        module.slot = '1.0'
        xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:" + getVersion().number + "' name='my.module'${isSlotSupported() ? ' slot=\'1.0\'' : ''}>\n" +
                "  <dependencies>\n" +
                "    <module name='module1' />\n" +
                "    <module name='module2' />\n" +
                "    <module export='false' name='module3' optional='true' services='none'${isSlotSupported() ? ' slot=\'1.3\'' : ''}>\n" +
                "      <imports>\n" +
                "        <include path='**' />\n" +
                "        <exclude-set>\n" +
                "          <path name='exclude1' />\n" +
                "          <path name='exclude2' />\n" +
                "        </exclude-set>\n" +
                "      </imports>\n" +
                "      <exports>\n" +
                "        <include path='**' />\n" +
                "        <exclude-set>\n" +
                "          <path name='not a' />\n" +
                "          <path name='not b' />\n" +
                "        </exclude-set>\n" +
                "      </exports>\n" +
                "    </module>\n" +
                "    <module name='module4'>\n" +
                "      <exports>\n" +
                "        <include-set>\n" +
                "          <path name='1111' />\n" +
                "          <path name='222' />\n" +
                "        </include-set>\n" +
                "        <exclude path='all**' />\n" +
                "      </exports>\n" +
                "    </module>\n" +
                "    <module name='module5'>\n" +
                "      <imports>\n" +
                "        <include-set>\n" +
                "          <path name='1111' />\n" +
                "          <path name='222' />\n" +
                "        </include-set>\n" +
                "        <exclude path='all**' />\n" +
                "      </imports>\n" +
                "    </module>\n" +
                "    <system>\n" +
                "      <paths>\n" +
                "        <path name='test-path' />\n" +
                "      </paths>\n" +
                "    </system>\n" +
                "    <system export='true'>\n" +
                "      <paths>\n" +
                "        <path name='path1' />\n" +
                "        <path name='path2' />\n" +
                "      </paths>\n" +
                "      <exports>\n" +
                "        <include path='but include **' />\n" +
                "        <exclude-set>\n" +
                "          <path name='exclude1' />\n" +
                "          <path name='exclude2' />\n" +
                "        </exclude-set>\n" +
                "      </exports>\n" +
                "    </system>\n" +
                "    <system>\n" +
                "      <paths>\n" +
                "        <path name='test-path' />\n" +
                "      </paths>\n" +
                "      <exports>\n" +
                "        <include-set>\n" +
                "          <path name='11' />\n" +
                "          <path name='22' />\n" +
                "        </include-set>\n" +
                "        <exclude path='but not **' />\n" +
                "      </exports>\n" +
                "    </system>\n" +
                "  </dependencies>\n" +
                "</module>"
        XMLUtil.assertXMLStrings 'Case2:', xml, module.moduleDescriptor
        assertTrue module.valid
        XMLUtil.assertXMLStrings 'Reverse:', xml, Ver.makeModule(xml).moduleDescriptor
    }

    abstract protected Ver getVersion()

    abstract protected boolean isSlotSupported()
}
