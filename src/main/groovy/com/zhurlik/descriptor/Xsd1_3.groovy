package com.zhurlik.descriptor

import com.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

import javax.xml.transform.stream.StreamSource

import static com.zhurlik.Ver.V_1_3

/**
 * Generates a xml descriptor for JBoss Module ver.1.3
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_3.xsd
 *
 * @author zhurlik@gmail.com
 */
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
        } else {
            // <module xmlns="urn:jboss:module:1.3" name="org.jboss.msc">
            final attrs = [xmlns: 'urn:jboss:module:' + V_1_3.number, name: jmodule.moduleName] + ((jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot])
            xml.module(attrs) {
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
        return new StreamSource(getClass().classLoader.getResourceAsStream(V_1_3.xsd))
    }

    @Override
    String getPath(JBossModule module) {
        return null
    }

    @Override
    JBossModule makeModule(final String txt) {
        throw new UnsupportedOperationException("Not implemented yet")
    }

    /**
     * Writes <module-alias xmlns="urn:jboss:module:1.3" name="javax.json.api" target-name="org.glassfish.javax.json"/>
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeModuleAlias(final JBossModule jmodule, final MarkupBuilder xml) {
        assert jmodule.targetName != null, 'Target Name is null'

        def attrs = [xmlns: 'urn:jboss:module:' + V_1_3.number, name: jmodule.moduleName]
        attrs += (jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot]
        attrs.put('target-name', jmodule.targetName)
        xml.'module-alias'(attrs)
    }
}
