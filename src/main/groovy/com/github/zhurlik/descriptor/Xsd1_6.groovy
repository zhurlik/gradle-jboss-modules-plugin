package com.github.zhurlik.descriptor

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import javax.xml.transform.stream.StreamSource

import static com.github.zhurlik.Ver.V_1_6
import static java.io.File.separator

/**
 * Generates a xml descriptor for JBoss Module ver.1.6
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_6.xsd
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class Xsd1_6 extends Builder<JBossModule> {

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
    StreamSource getXsd() {
        return new StreamSource(getClass().classLoader.getResourceAsStream(getVersion().xsd))
    }

    @Override
    String getPath(JBossModule jbModule) {
        return ['modules', 'system', 'layers', 'base', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot)].join(separator)
    }

    @Override
    protected Ver getVersion() {
        return V_1_6
    }

    @Override
    protected void writeModuleType(JBossModule jmodule, MarkupBuilder xml) {
        // <module xmlns="urn:jboss:module:1.5" name="org.jboss.msc">
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
                if (res.filter == null) {
                    xml.artifact(name: res.name)
                } else {
                    xml.artifact(name: res.name) {
                        xml.filter() {
                            // include
                            if (res.filter.include != null) {
                                if (res.filter.include instanceof String || res.filter.include.size() == 1) {
                                    xml.'include'(path: res.filter.include.toString())
                                } else if (res.filter.include.size() > 1) {
                                    xml.'include-set'() {
                                        res.filter.include.each() {
                                            xml.'path'(name: it)
                                        }
                                    }
                                }
                            }

                            //exclude
                            if (res.filter.exclude != null) {
                                if (res.filter.exclude instanceof String || res.filter.exclude.size() == 1) {
                                    xml.'exclude'(path: res.filter.exclude.toString())
                                } else if (res.filter.exclude.size() > 1) {
                                    xml.'exclude-set'() {
                                        res.filter.exclude.each() {
                                            xml.'path'(name: it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if ('native-artifact' == res.type) {
                if (res.filter == null) {
                    xml.'native-artifact'(name: res.name)
                } else {
                    xml.'native-artifact'(name: res.name) {
                        xml.filter() {
                            // include
                            if (res.filter.include != null) {
                                if (res.filter.include instanceof String || res.filter.include.size() == 1) {
                                    xml.'include'(path: res.filter.include.toString())
                                } else if (res.filter.include.size() > 1) {
                                    xml.'include-set'() {
                                        res.filter.include.each() {
                                            xml.'path'(name: it)
                                        }
                                    }
                                }
                            }

                            //exclude
                            if (res.filter.exclude != null) {
                                if (res.filter.exclude instanceof String || res.filter.exclude.size() == 1) {
                                    xml.'exclude'(path: res.filter.exclude.toString())
                                } else if (res.filter.exclude.size() > 1) {
                                    xml.'exclude-set'() {
                                        res.filter.exclude.each() {
                                            xml.'path'(name: it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
