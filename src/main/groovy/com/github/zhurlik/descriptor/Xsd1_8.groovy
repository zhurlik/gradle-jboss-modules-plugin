package com.github.zhurlik.descriptor

import com.github.zhurlik.Ver
import com.github.zhurlik.descriptor.parser.DependenciesTag
import com.github.zhurlik.descriptor.parser.ExportsTag
import com.github.zhurlik.descriptor.parser.ModuleAbsentTag
import com.github.zhurlik.descriptor.parser.ModuleAliasTag
import com.github.zhurlik.descriptor.parser.ModuleTag
import com.github.zhurlik.descriptor.parser.PermissionsTag
import com.github.zhurlik.descriptor.parser.PropertiesTag
import com.github.zhurlik.descriptor.parser.ProvidesTag
import com.github.zhurlik.descriptor.parser.ResourcesTag
import com.github.zhurlik.descriptor.parser.XmlDeclarationTag
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import static com.github.zhurlik.Ver.V_1_8
import static java.io.File.separator

/**
 * Generates a xml descriptor for JBoss Module ver.1.8
 * https://github.com/jboss-modules/jboss-modules/tree/1.x/src/main/resources/schema
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class Xsd1_8 extends Builder<JBossModule> {

    @Override
    String getXmlDescriptor(final JBossModule jmodule) {
        assert jmodule != null, 'JBossModule is null'
        assert jmodule.moduleName != null, 'Module name is null'

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        XmlDeclarationTag.write().accept(xml)

        if (jmodule.isModuleAlias()) {
            ModuleAliasTag.write(jmodule).accept(xml)
        } else if (jmodule.isModuleAbsent()) {
            ModuleAbsentTag.write(jmodule).accept(xml)
        } else {
            writeModuleType(jmodule, xml)
        }

        return writer.toString()
    }


    @Override
    String getPath(final JBossModule jbModule) {
        return ['modules', 'system', 'layers', 'base', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.version in [null, '']) ? 'main' : jbModule.version)].join(separator)
    }

    protected Ver getVersion() {
        return V_1_8
    }

    /**
     * Writes the module declaration type; contains dependencies, resources, and the main class specification.
     * <p>
     * Root element for a module declaration.
     * </p>
     * See <xsd:element name="module" type="moduleType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeModuleType(final JBossModule jmodule, final MarkupBuilder xml) {
        // <module xmlns="urn:jboss:module:1.8" name="org.jboss.msc">
        final attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName] +
                ((jmodule.version in [null, '']) ? [:] : [version: jmodule.version])
        xml.module(attrs) {
            ExportsTag.write(jmodule).accept(xml)
            ModuleTag.writeMainClass(jmodule).accept(xml)
            PropertiesTag.write(jmodule).accept(xml)
            ResourcesTag.write(jmodule).accept(xml)
            DependenciesTag.write(jmodule).accept(xml)
            PermissionsTag.write(jmodule).accept(xml)
            ProvidesTag.write(jmodule).accept(xml)
        }
    }
}
