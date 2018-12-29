package com.github.zhurlik.descriptor

import com.github.zhurlik.descriptor.parser.ModuleAbsentTag
import com.github.zhurlik.descriptor.parser.ModuleAliasTag
import com.github.zhurlik.descriptor.parser.ModuleTag
import com.github.zhurlik.descriptor.parser.XmlDeclarationTag
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import static java.io.File.separator

/**
 * Generates a xml descriptor for JBoss Module ver.1.3
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_3.xsd
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class Xsd1_3 extends Xsd {

    @Override
    String getXmlDescriptor(JBossModule jmodule) {
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

    @Override
    String getPath(JBossModule jbModule) {
        return ['modules', 'system', 'layers', 'base', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot)].join(separator)
    }
}
