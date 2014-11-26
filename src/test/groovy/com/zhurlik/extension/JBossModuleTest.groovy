package com.zhurlik.extension

import com.zhurlik.descriptor.Builder
import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

import static com.zhurlik.Ver.V_1_1
import static com.zhurlik.descriptor.BuilderFactory.getBuilder
import static java.io.File.separator
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue;

/**
 * Unit test to check all cases to create JBoss Module.
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class JBossModuleTest {

    private JBossModule module
    private Builder<JBossModule> builder = getBuilder(V_1_1)
    private
    final File projectDir = new File(getClass().getClassLoader().getResource('').toURI().path + separator + 'projectTest')

    @Before
    public void setUp() throws Exception {
        if (!projectDir.exists()) {
            assert projectDir.mkdir()
        }
    }

    @After
    public void tearDown() throws Exception {
        if (projectDir.exists() && projectDir.isDirectory()) {
            assert projectDir.deleteDir()
        }
    }

    @Test
    public void testName() throws Exception {
        // 1
        module = new JBossModule('testModule')
        module.ver = V_1_1
        module.moduleName = 'my.module'
        module.mainClass = ''
        module.slot = '1.0'
        String xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='my.module' slot='1.0' />"
        assertEquals 'Case1:', xml, module.moduleDescriptor
        assert module.valid
        assertEquals 'Reverse:', xml, builder.makeModule(xml).moduleDescriptor

        // 2
        module = new JBossModule('spring-core')
        module.moduleName = 'org.springframework.spring-core'
        module.resources = ['spring-core-3.2.5.RELEASE.jar']
        module.dependencies = ['javax.api',
                               'org.apache.commons.logging',
                               'org.jboss.vfs']
        xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='org.springframework.spring-core'>\n" +
                "  <resources>\n" +
                "    <resource-root path='spring-core-3.2.5.RELEASE.jar' />\n" +
                "  </resources>\n" +
                "  <dependencies>\n" +
                "    <module name='javax.api' />\n" +
                "    <module name='org.apache.commons.logging' />\n" +
                "    <module name='org.jboss.vfs' />\n" +
                "  </dependencies>\n" +
                "</module>"
        assertEquals 'Case2:', xml, module.moduleDescriptor
        assert module.valid
        assertEquals 'Reverse:', xml, builder.makeModule(xml).moduleDescriptor

        // 3
        module = new JBossModule('test-module-3')
        module.moduleName = 'test.module.3'
        module.mainClass = 'test.MainClass'
        xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module.3'>\n" +
                "  <main-class name='test.MainClass' />\n" +
                "</module>"
        assertEquals 'Case3:', xml, module.moduleDescriptor
        assert module.valid
        assertEquals 'Reverse:', xml, builder.makeModule(xml).moduleDescriptor

        // 4
        module = new JBossModule('test-module-4')
        module.moduleName = 'test.module.4'
        module.properties = [prop1: 'value1', prop2: 'value2', '': '']
        xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module.4'>\n" +
                "  <properties>\n" +
                "    <property name='prop1' value='value1' />\n" +
                "    <property name='prop2' value='value2' />\n" +
                "  </properties>\n" +
                "</module>"
        assertEquals 'Case4:', xml, module.moduleDescriptor
        assert module.valid
        assertEquals 'Reverse:', xml, builder.makeModule(xml).moduleDescriptor

        // 5
        module = new JBossModule('test-module-5')
        module.moduleName = 'test.module.5'
        module.resources = ['res1', [name: 'res2', path: 'path2'], [path: 'res3', filter: [include: 'incl*', exclude: ['exclude1', 'exclude2']]]]
        xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module.5'>\n" +
                "  <resources>\n" +
                "    <resource-root path='res1' />\n" +
                "    <resource-root name='res2' path='path2' />\n" +
                "    <resource-root path='res3'>\n" +
                "      <filter>\n" +
                "        <include path='incl*' />\n" +
                "        <exclude-set>\n" +
                "          <path name='exclude1' />\n" +
                "          <path name='exclude2' />\n" +
                "        </exclude-set>\n" +
                "      </filter>\n" +
                "    </resource-root>\n" +
                "  </resources>\n" +
                "</module>"
        assertEquals 'Case5:', xml, module.moduleDescriptor
        assert module.valid
        assertEquals 'Reverse:', xml, builder.makeModule(xml).moduleDescriptor

        // 6
        module = new JBossModule('test-module-6')
        module.moduleName = 'test.module.6'
        module.dependencies = ['module1', 'module2',
                               [name   : 'module3', slot: '1.3', services: 'none', optional: true, export: 'false',
                                imports: [exclude: ['exclude1', 'exclude2']],
                                exports: [include: '**']
                               ]
        ]
        xml = "<?xml version='1.0' encoding='utf-8'?>\n" +
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
                "</module>"
        assertEquals 'Case6:', xml, module.moduleDescriptor
        assert module.valid
        assertEquals 'Reverse:', "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='test.module.6'>\n" +
                "  <dependencies>\n" +
                "    <module name='module1' />\n" +
                "    <module name='module2' />\n" +
                "    <module services='none' export='false' name='module3' optional='true' slot='1.3'>\n" +
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
                "</module>", builder.makeModule(xml).moduleDescriptor
    }

    @Test
    /**
     * Test for downloading a resource and saving a jboss module
     */
    public void testMake() throws Exception {
        log.debug '>> Test for making a module and saving locally...'
        // 1
        module = new JBossModule('log4j')
        module.moduleName = 'org.apache.log4j'
        module.resources = ['log4j-1.2.17.jar']

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

        // test call
        module.makeLocally(project)

        assert new File(getClass().getClassLoader().getResource('projectTest/build/modules/org/apache/log4j/main/log4j-1.2.17.jar').toURI().path).exists()
        assert new File(getClass().getClassLoader().getResource('projectTest/build/modules/org/apache/log4j/main/module.xml').toURI().path).exists()
        assertEquals 'Module Descriptor:', '<?xml version=\'1.0\' encoding=\'utf-8\'?>\n' +
                '<module xmlns=\'urn:jboss:module:1.1\' name=\'org.apache.log4j\'>\n' +
                '  <resources>\n' +
                '    <resource-root path=\'log4j-1.2.17.jar\' />\n' +
                '  </resources>\n' +
                '</module>', new File(getClass().getClassLoader().getResource('projectTest/build/modules/org/apache/log4j/main/module.xml').toURI().path).text
    }

    @Test
    public void testDeploy() throws Exception {
        log.debug '>> A test for deployment process...'

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
            jbossmodules 'org.slf4j:slf4j-api:1.7.7'
        }

        // describe a module via gradle
        project.modules {
            slf4j {
                moduleName = 'org.slf4j'
                resources = ['slf4j-api-1.7.7.jar']
                dependencies = ['org.slf4j.impl']
            }
        }

        // describe an instance of jboss server
        project.jbossrepos {
            testServer {
                home = projectDir.path + separator + "testServer"
            }
        }

        // init a server
        final JBossServer server = project.jbossrepos['testServer']
        final File jbModulesDir = new File([server.home, 'modules'].join(separator))
        jbModulesDir.deleteDir()
        jbModulesDir.mkdirs()

        // there is nothing on the server
        assertNotNull server
        server.initTree()
        JBossModule testM = server.getModule('org.slf4j')
        assertEquals 'org.slf4j', testM.name
        assertEquals null, testM.moduleName
        assertTrue testM.dependencies.isEmpty()

        // makes a module
        final JBossModule module = project.modules['slf4j']
        module.makeLocally(project)

        // deployment
        module.deployToJBoss(project.jbossrepos['testServer'], project)

        // checking modules on the server
        log.debug 'OK'
        server.initTree()
        testM = server.getModule('org.slf4j')
        assertEquals 'org.slf4j', testM.name
        assertEquals 'org.slf4j', testM.moduleName
        assertEquals 'org.slf4j.impl', testM.dependencies[0].name
        assertTrue new File([server.home, testM.path, 'slf4j-api-1.7.7.jar'].join(separator)).exists()
        assertEquals "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:1.1' name='org.slf4j'>\n" +
                "  <resources>\n" +
                "    <resource-root path='slf4j-api-1.7.7.jar' />\n" +
                "  </resources>\n" +
                "  <dependencies>\n" +
                "    <module name='org.slf4j.impl' />\n" +
                "  </dependencies>\n" +
                "</module>", new File([server.home, testM.path, 'module.xml'].join(separator)).text
    }
}
