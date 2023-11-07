package com.github.zhurlik.task

import TestUtils.XMLUtil
import com.github.zhurlik.extension.JBossModule
import com.github.zhurlik.extension.JBossServer
import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

import static com.github.zhurlik.Ver.V_1_3
import static java.io.File.separator
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * Testing a chain of tasks which are provided by plugin.
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class TasksTest {

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
    public void greeterPluginAddsGreetingTaskToProject() {
        // empty project with all needed
        final Project project = ProjectBuilder.builder()
                .withName('test-project')
                .withProjectDir(projectDir)
                .build()

        project.apply plugin: 'com.github.zhurlik.jbossmodules'

        // describe a module via gradle
        project.modules {
            slf4j {
                ver = V_1_3
                moduleName = 'org.slf4j'
                dependencies = ['org.slf4j.impl']
            }
        }

        // describe an instance of jboss server
        project.jbossrepos {
            testServer {
                home = projectDir.path + separator + "testServer"
                version = V_1_3
            }
        }

        // init a server
        final JBossServer server = project.jbossrepos['testServer']
        final File jbModulesDir = new File([server.home, 'modules'].join(separator))
        jbModulesDir.deleteDir()
        jbModulesDir.mkdirs()

        assertTrue(project.tasks.checkModules instanceof CheckModulesTask)

        log.debug '>> Task: makeModules'
        project.tasks.makeModules.actions.each {
            it.execute(project.tasks.makeModules)
        }

        log.debug '>> Task: checkModules'
        project.tasks.checkModules.actions.each {
            it.execute(project.tasks.checkModules)
        }

        // to call afterEvaluate()
        project.evaluate()
        project.file('build/distributions').mkdirs()
        log.debug '>> Task: testServerDistTar'
        project.tasks.testServerDistTar.actions.each {
            it.execute(project.tasks.testServerDistTar)
        }

        log.debug '>> Task: deployModules'
        project.tasks.deployModules.actions.each {
            it.execute(project.tasks.deployModules)
        }

        // checking modules on the server
        log.debug 'OK'
        server.initTree()
        final JBossModule testM = server.getModule('org.slf4j')
        assertEquals 'org.slf4j', testM.name
        assertEquals 'org.slf4j', testM.moduleName
        assertEquals 'org.slf4j.impl', testM.dependencies[0].name
        XMLUtil.assertXMLStrings "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<module xmlns='urn:jboss:module:" + V_1_3.number + "' name='org.slf4j'>\n" +
                "  <dependencies>\n" +
                "    <module name='org.slf4j.impl' />\n" +
                "  </dependencies>\n" +
                "</module>", new File([server.home, testM.path, 'module.xml'].join(separator)).text

    }

    @Test
    public void testInitFromPom() throws Exception {
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

        project.dependencies {
            jbossmodules 'org.springframework:spring-oxm:4.1.6.RELEASE'
        }

        def task = project.task('initModule', type: InitModuleTask)
        task.pomName = 'spring-oxm-4.1.6.RELEASE'

        log.debug '>> Task: initModule'
        project.tasks.initModule.actions.each {
            it.execute(project.tasks.initModule)
        }

        assertEquals '''\
sprinoxm {
    moduleName = 'org.springframework.spring-oxm\'
    resources = ['spring-oxm-4.1.6.RELEASE.jar']
    dependencies = [org.springframework.spring-beans, org.springframework.spring-core]
    // or via more resources [spring-beans-4.1.6.RELEASE.jar, spring-core-4.1.6.RELEASE.jar]

    // optional which can be needed:
    // either resources = [xstream-1.4.7.jar, xmlbeans-2.6.0.jar, castor-xml-1.3.3.jar, jibx-run-1.2.6.jar]
    // or dependencies = [com.thoughtworks.xstream, org.apache.xmlbeans, org.codehaus.castor.castor-xml, org.jibx.jibx-run]
}''', project.tasks.initModule.result
    }
}
