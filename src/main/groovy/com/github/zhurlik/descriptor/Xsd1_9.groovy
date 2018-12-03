package com.github.zhurlik.descriptor

import com.github.zhurlik.Ver
import com.github.zhurlik.descriptor.parser.DependenciesTag
import com.github.zhurlik.descriptor.parser.ExportsTag
import com.github.zhurlik.descriptor.parser.ModuleAliasTag
import com.github.zhurlik.descriptor.parser.PermissionsTag
import com.github.zhurlik.descriptor.parser.PropertiesTag
import com.github.zhurlik.descriptor.parser.ResourcesTag
import com.github.zhurlik.descriptor.parser.XmlDeclarationTag
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import static com.github.zhurlik.Ver.V_1_9
import static com.github.zhurlik.descriptor.Xsd1_8.writeProvides
import static java.io.File.separator

/**
 * Generates a xml descriptor for JBoss Module ver.1.9
 * https://github.com/jboss-modules/jboss-modules/tree/1.x/src/main/resources/schema
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class Xsd1_9 extends Builder<JBossModule> {

    @Override
    String getXmlDescriptor(JBossModule jmodule) {
        assert jmodule != null, 'JBossModule is null'
        assert jmodule.moduleName != null, 'Module name is null'

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        XmlDeclarationTag.write().accept(xml)

        if (jmodule.isModuleAlias()) {
            ModuleAliasTag.write(jmodule).accept(xml)
        } else if (jmodule.isModuleAbsent()) {
            writeModuleAbsentType(jmodule, xml)
        } else {
            writeModuleType(jmodule, xml)
        }

        return writer.toString()
    }

    @Override
    String getPath(final JBossModule jbModule) {
        return ['modules', 'system', 'layers', 'base', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.version in [null, '']) ? 'main' : jbModule.version)].join(separator)
    }

    @Override
    protected Ver getVersion() {
        return V_1_9
    }

    @Override
    protected void writeModuleType(final JBossModule jmodule, final MarkupBuilder xml) {
        // <module xmlns="urn:jboss:module:1.9" name="org.jboss.msc">
        final attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName] +
                ((jmodule.version in [null, '']) ? [:] : [version: jmodule.version])
        xml.module(attrs) {
            ExportsTag.write(jmodule).accept(xml)
            writeMainClass(jmodule, xml)
            PropertiesTag.write(jmodule).accept(xml)
            ResourcesTag.write(jmodule).accept(xml)
            DependenciesTag.write(jmodule).accept(xml)
            PermissionsTag.write(jmodule).accept(xml)
            writeProvides(jmodule, xml)
        }
    }
}
