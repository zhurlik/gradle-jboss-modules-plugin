package com.zhurlik.descriptor

import com.zhurlik.Ver
import com.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import javax.xml.transform.stream.StreamSource

import static com.zhurlik.Ver.V_1_3
import static java.io.File.separator
/**
 * Generates a xml descriptor for JBoss Module ver.1.3
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_3.xsd
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class Xsd1_3 extends Builder<JBossModule> {

    @Override
    String getXmlDescriptor(JBossModule jmodule) {
        assert jmodule != null, 'JBossModule is null'
        assert jmodule.moduleName != null, 'Module name is null'

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        writeXmlDeclaration(xml)

        if (jmodule.isModuleAlias()) {
            writeModuleAlias(jmodule, xml)
        } else if (jmodule.isModuleAbsent()) {
            writeModuleAbsent(jmodule, xml)
        } else {
            // <module xmlns="urn:jboss:module:1.3" name="org.jboss.msc">
            final attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName] + ((jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot])
            xml.module(attrs) {
                writeExports(jmodule, xml)
                writeMainClass(jmodule, xml)
                writeProperties(jmodule, xml)
                writeResources(jmodule, xml)
                writeDependencies(jmodule, xml)
            }
        }

        return writer.toString()
    }

    @Override
    StreamSource getXsd() {
        return new StreamSource(getClass().classLoader.getResourceAsStream(getVersion().xsd))
    }

    @Override
    String getPath(JBossModule jbModule) {
        return ['modules', 'system', 'layers', 'base', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot)].join(separator)
    }

    /**
     * Writes a root element for an absent module.
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeModuleAbsent(final JBossModule jmodule, final MarkupBuilder xml) {
        assert jmodule.moduleName != null, 'Module Name is null'

        def attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName]
        attrs += (jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot]

        xml.'module-absent'(attrs)
    }

    @Override
    protected Ver getVersion() {
        return V_1_3
    }
}
