package com.github.zhurlik.descriptor

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

import static com.github.zhurlik.Ver.V_1_2
import static java.io.File.separator

/**
 * Generates a xml descriptor for JBoss Module ver.1.2
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_2.xsd
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_2 extends Builder<JBossModule> {

    @Override
    String getXmlDescriptor(JBossModule jmodule) {
        assert jmodule != null, 'JBossModule is null'
        assert jmodule.moduleName != null, 'Module name is null'

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        writeXmlDeclaration(xml)

        if (jmodule.isModuleAlias()) {
            writeModuleAliasType(jmodule, xml)
        } else if (jmodule.isModuleAbsent()) {
            writeModuleAbsentType(jmodule, xml)
        } else {
            writeModuleType(jmodule, xml)
        }

        return writer.toString()
    }

    @Override
    String getPath(JBossModule jbModule) {
        return ['modules', 'system', 'layers', 'base', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot)].join(separator)
    }

    @Override
    protected Ver getVersion() {
        return V_1_2
    }

    @Override
    protected void writeModuleType(JBossModule jmodule, MarkupBuilder xml) {
        // <module xmlns="urn:jboss:module:1.3" name="org.jboss.msc">
        final attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName] + ((jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot])
        xml.module(attrs) {
            writeExports(jmodule, xml)
            writeMainClass(jmodule, xml)
            writeProperties(jmodule, xml)
            writeResourcesType(jmodule, xml)
            writeDependenciesType(jmodule, xml)
            writePermissionsType(jmodule, xml)
        }
    }
}
