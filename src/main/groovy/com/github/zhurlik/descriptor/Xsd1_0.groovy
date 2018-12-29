package com.github.zhurlik.descriptor

import com.github.zhurlik.descriptor.parser.ConfigurationTag
import com.github.zhurlik.descriptor.parser.ModuleTag
import com.github.zhurlik.descriptor.parser.XmlDeclarationTag
import com.github.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

import static java.io.File.separator

/**
 * Generates a xml descriptor for JBoss Module ver.1.0
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_0.xsd
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_0 extends Xsd {

    @Override
    String getXmlDescriptor(final JBossModule jmodule) {
        Objects.requireNonNull(jmodule, 'JBossModule is null')

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        XmlDeclarationTag.write().accept(xml)

        if (jmodule.isModuleConfiguration()) {
            ConfigurationTag.write(jmodule).accept(xml)
        } else {
            Objects.requireNonNull(jmodule.moduleName, 'Module name is null')
            ModuleTag.write(jmodule).accept(xml)
        }

        return writer.toString()
    }

    @Override
    String getPath(final JBossModule jbModule) {
        return ['modules', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot)].join(separator)
    }
}
