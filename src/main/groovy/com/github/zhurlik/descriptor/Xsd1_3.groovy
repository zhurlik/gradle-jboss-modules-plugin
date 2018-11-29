package com.github.zhurlik.descriptor

import com.github.zhurlik.Ver
import com.github.zhurlik.descriptor.parser.DependenciesTag
import com.github.zhurlik.descriptor.parser.ExportsTag
import com.github.zhurlik.descriptor.parser.ModuleAliasTag
import com.github.zhurlik.descriptor.parser.PermissionsTag
import com.github.zhurlik.descriptor.parser.PropertiesTag
import com.github.zhurlik.descriptor.parser.XmlDeclarationTag
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import static com.github.zhurlik.Ver.V_1_3
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
    String getPath(JBossModule jbModule) {
        return ['modules', 'system', 'layers', 'base', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot)].join(separator)
    }

    @Override
    protected Ver getVersion() {
        return V_1_3
    }

    @Override
    protected void writeModuleType(JBossModule jmodule, MarkupBuilder xml) {
        // <module xmlns="urn:jboss:module:1.3" name="org.jboss.msc">
        final attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName] + ((jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot])
        xml.module(attrs) {
            ExportsTag.write(jmodule).accept(xml)
            writeMainClass(jmodule, xml)
            PropertiesTag.write(jmodule).accept(xml)
            writeResourcesType(jmodule, xml)
            DependenciesTag.write(jmodule).accept(xml)
            PermissionsTag.write(jmodule).accept(xml)
        }
    }

    /**
     *  Writes a maven artifact within this deployment.
     *
     *  <p>A maven native artifact within this deployment. This is a jar that contains a lib/ directory
     *  with corresponding platform directories and binaries. This element will cause the jar to
     *  be unzipped within the artifact's local repository directory.</p>
     *
     *  See either <xsd:element name="artifact" type="artifactType"> or <xsd:element name="native-artifact" type="artifactType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeArtifacts(final JBossModule jmodule, final MarkupBuilder xml) {
        jmodule.resources.findAll({
            ((it instanceof Map) && (it.type in ['artifact', 'native-artifact']))
        }).each() { res ->
            // URI that points to the maven artifact "group:artifact:version[:classifier]"
            if ('artifact' == res.type) {
                xml.artifact(name: res.name)
            } else if ('native-artifact' == res.type) {
                xml.'native-artifact'(name: res.name)
            }
        }
    }
}
