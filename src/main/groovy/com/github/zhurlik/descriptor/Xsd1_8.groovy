package com.github.zhurlik.descriptor

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import javax.xml.transform.stream.StreamSource

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

    /**
     * Writes lists items that are statically provided by this module.
     *  <providers>
     *    <service name=''>
     *        <with-class name='class'/>
     *    <service/>
     *  </providers>
     * <p>Lists items that are statically provided by this module.</p>
     *
     * See <xsd:element name="provides" type="providesType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeProvides(final JBossModule jmodule, final MarkupBuilder xml) {
        if (!jmodule.provides.isEmpty()) {
            xml.provides {
                jmodule.provides.each {s ->
                    // simple
                    if (s instanceof String || s instanceof GString) {
                        service(name: s.toString())
                    }

                    // complex
                    if (s instanceof Map) {
                        service(name: s.name) {
                            def withClass = s['with-class']
                            if (withClass instanceof String || s instanceof GString) {
                                'with-class'(name: withClass.toString())
                            }
                            if (withClass instanceof Collection) {
                                withClass.each {
                                    'with-class'(name: it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    StreamSource getXsd() {
        return new StreamSource(getClass().classLoader.getResourceAsStream(getVersion().xsd))
    }

    @Override
    String getPath(JBossModule jbModule) {
        return ['modules', 'system', 'layers', 'base', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.version in [null, '']) ? 'main' : jbModule.version)].join(separator)
    }

    @Override
    protected Ver getVersion() {
        return V_1_8
    }

    @Override
    protected void writeModuleType(JBossModule jmodule, MarkupBuilder xml) {
        // <module xmlns="urn:jboss:module:1.8" name="org.jboss.msc">
        final attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName] +
                ((jmodule.version in [null, '']) ? [:] : [version: jmodule.version])
        xml.module(attrs) {
            writeExports(jmodule, xml)
            writeMainClass(jmodule, xml)
            writeProperties(jmodule, xml)
            writeResourcesType(jmodule, xml)
            writeDependenciesType(jmodule, xml)
            writePermissionsType(jmodule, xml)
            writeProvides(jmodule, xml)
        }
    }
}
