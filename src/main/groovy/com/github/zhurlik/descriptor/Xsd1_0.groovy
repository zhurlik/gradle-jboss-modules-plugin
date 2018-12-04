package com.github.zhurlik.descriptor

import com.github.zhurlik.Ver
import com.github.zhurlik.descriptor.parser.ConfigurationTag
import com.github.zhurlik.descriptor.parser.DependenciesTag
import com.github.zhurlik.descriptor.parser.ExportsTag
import com.github.zhurlik.descriptor.parser.ResourcesTag
import com.github.zhurlik.descriptor.parser.XmlDeclarationTag
import com.github.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

import static com.github.zhurlik.Ver.V_1_0

/**
 * Generates a xml descriptor for JBoss Module ver.1.0
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_0.xsd
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_0 extends Builder<JBossModule> {

    @Override
    String getXmlDescriptor(final JBossModule jmodule) {
        assert jmodule != null, 'JBossModule is null'
        assert (jmodule.moduleName != null || jmodule.moduleConfiguration), 'Module name is null'

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        XmlDeclarationTag.write().accept(xml)

        if (jmodule.isModuleConfiguration()) {
            ConfigurationTag.write(jmodule).accept(xml)
        } else {
            writeModuleType(jmodule, xml)
        }

        return writer.toString()
    }

    @Override
    String getPath(final JBossModule jbModule) {
        return ['modules', jbModule.moduleName.replaceAll('\\.', "/"), ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot)].join("/")
    }

    @Override
    protected Ver getVersion() {
        return V_1_0
    }

    @Override
    protected void writeModuleType(JBossModule jmodule, MarkupBuilder xml) {
        // <module xmlns="urn:jboss:module:1.0" name="org.jboss.msc">
        xml.module([xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName] + ((jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot])) {
            ExportsTag.write(jmodule).accept(xml)
            writeMainClass(jmodule, xml)
            ResourcesTag.write(jmodule).accept(xml)
            DependenciesTag.write(jmodule).accept(xml)
        }
    }
}
