package com.github.zhurlik.descriptor

import com.github.zhurlik.Ver
import com.github.zhurlik.descriptor.parser.ConfigurationTag
import com.github.zhurlik.descriptor.parser.DependenciesTag
import com.github.zhurlik.descriptor.parser.ExportsTag
import com.github.zhurlik.descriptor.parser.ModuleAliasTag
import com.github.zhurlik.descriptor.parser.PropertiesTag
import com.github.zhurlik.descriptor.parser.XmlDeclarationTag
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import static com.github.zhurlik.Ver.V_1_1
import static java.io.File.separator

/**
 * Generates a xml descriptor for JBoss Module ver.1.1
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_1.xsd
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class Xsd1_1 extends Builder<JBossModule> {

    @Override
    String getXmlDescriptor(final JBossModule jmodule) {
        assert jmodule != null, 'JBossModule is null'
        assert (jmodule.moduleName != null || jmodule.moduleConfiguration), 'Module name is null'

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        XmlDeclarationTag.write().accept(xml)

        if (jmodule.isModuleAlias()) {
            ModuleAliasTag.write(jmodule).accept(xml)
        } else if (jmodule.isModuleConfiguration()) {
            ConfigurationTag.write(jmodule).accept(xml)
        } else {
            writeModuleType(jmodule, xml)
        }

        return writer.toString()
    }

    @Override
    String getPath(final JBossModule jbModule) {
        return ['modules', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot)].join(separator)
    }

    @Override
    protected Ver getVersion() {
        return V_1_1
    }

    @Override
    protected void writeModuleType(JBossModule jmodule, MarkupBuilder xml) {
        // <module xmlns="urn:jboss:module:1.1" name="org.jboss.msc">
        xml.module([xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName] + ((jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot])) {
            ExportsTag.write(jmodule).accept(xml)
            writeMainClass(jmodule, xml)
            PropertiesTag.write(jmodule).accept(xml)
            writeResourcesType(jmodule, xml)
            DependenciesTag.write(jmodule).accept(xml)
        }
    }
}
