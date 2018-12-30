package com.github.zhurlik.descriptor

import com.github.zhurlik.descriptor.parser.ModuleAbsentTag
import com.github.zhurlik.descriptor.parser.ModuleAliasTag
import com.github.zhurlik.descriptor.parser.ModuleTag
import com.github.zhurlik.descriptor.parser.XmlDeclarationTag
import com.github.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

/**
 * Generates a xml descriptor for JBoss Module ver.1.8
 * https://github.com/jboss-modules/jboss-modules/tree/1.x/src/main/resources/schema
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_8 extends Xsd {

    @Override
    String getXmlDescriptor(final JBossModule jmodule) {
        Objects.requireNonNull(jmodule, 'JBossModule is null')
        Objects.requireNonNull(jmodule.moduleName, 'Module name is null')

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        XmlDeclarationTag.write().accept(xml)

        if (jmodule.isModuleAlias()) {
            ModuleAliasTag.write(jmodule).accept(xml)
        } else if (jmodule.isModuleAbsent()) {
            ModuleAbsentTag.write(jmodule).accept(xml)
        } else {
            ModuleTag.write(jmodule).accept(xml)
        }

        return writer.toString()
    }
}
