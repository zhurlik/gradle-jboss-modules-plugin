package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

import java.util.function.Consumer

/**
 *     <xsd:complexType name="systemDependencyType">
 *         <xsd:all>
 *             <xsd:element name="paths" type="pathSetType">
 *                 <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                     <documentation>
 *                         The list of paths which are applicable for this system dependency.
 *                     </documentation>
 *                 </annotation>
 *             </xsd:element>
 *             <xsd:element name="exports" type="filterType" minOccurs="0">
 *                 <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                     <documentation>
 *                         A filter used to restrict what packages or directories from this dependency are re-exported by
 *                         this module. See also the "export" and "services" attributes. The default action of this filter
 *                         list is controlled by the value of the "export" attribute. Regardless of the setting of these
 *                         attributes, this filter always behaves as if it has a final entry which rejects META-INF and
 *                         all of its subdirectories.
 *                     </documentation>
 *                 </annotation>
 *             </xsd:element>
 *         </xsd:all>
 *         <xsd:attribute name="export" type="xsd:boolean" use="optional" default="false">
 *             <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                 <documentation>
 *                     Specifies whether this module dependency is re-exported by default (default is "false"). Setting
 *                     this attribute to true sets the default action for the export filter list to "accept"; leaving it
 *                     as false sets the default action to "reject". Thus you can still export dependency resources even
 *                     if this attribute is false by listing explicit paths for the export list.
 *                 </documentation>
 *             </annotation>
 *         </xsd:attribute>
 *     </xsd:complexType>
 */
class SystemDependencyTag {
    /**
     * To parse <xsd:complexType name="systemDependencyType">
     *
     * @param it a little bit of xml
     * @param jbModule
     * @return
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        return { final JBossModule jbModule ->
            xml.system.each() { s ->
                def system = [:]

                if (s.@export.toBoolean() == true) {
                    system.export = true
                }

                // paths
                def paths = []
                system.type = 'system'

                s.paths.each {
                    paths += it.path.collect({ it.@name.text() })
                }

                if (!paths.empty) {
                    system.paths = paths
                }

                // exports
                s.exports.each() {
                    def map = [:]
                    it.include.each() {
                        map.include = it.@path.text()
                    }
                    it.exclude.each() {
                        map.exclude = it.@path.text()
                    }
                    if (it.'exclude-set'.children().size() > 0) {
                        map.exclude = it.'exclude-set'.path.collect() { it.@name.text() }
                    }
                    if (it.'include-set'.children().size() > 0) {
                        map.include = it.'include-set'.path.collect() { it.@name.text() }
                    }
                    system.exports = map
                }

                jbModule.dependencies.add(system)
            }
        }
    }

    /**
     *  Writes the list of paths which are applicable for this system dependency.
     *      <xsd:element name="system" type="systemDependencyType">
     *            <annotation xmlns="http://www.w3.org/2001/XMLSchema">
     *               <documentation>
     *                   A dependency on the system (or embedding) class loader.
     *               </documentation>
     *           </annotation>
     *       </xsd:element>
     * <p>A dependency on the system (or embedding) class loader.</p>
     *
     * See <xsd:element name="system" type="systemDependencyType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        return { final MarkupBuilder xml ->
            jmodule.dependencies.findAll({ it instanceof Map && it.type == 'system' }).each { dep ->

                // Specifies whether this module dependency is re-exported by default (default is "false")
                xml.system((dep.export in [null, ''] || !dep.export) ? null : [export: dep.export]) {

                    // paths: the list of paths which are applicable for this system dependency.
                    if (dep.paths instanceof String) {
                        xml.paths {
                            xml.path([name: dep.paths])
                        }
                    } else if (dep.paths instanceof List) {
                        xml.paths {
                            dep.paths.each {
                                xml.path([name: it])
                            }
                        }
                    }

                    // exports: a filter used to restrict what packages or directories from this dependency are re-exported by this module
                    if (!(dep.exports in ['', null])) {
                        xml.exports() {
                            // include
                            if (dep.exports.include != null) {
                                if (dep.exports.include instanceof String || dep.exports.include instanceof GString || dep.exports.include.size() == 1) {
                                    xml.'include'(path: dep.exports.include.toString())
                                } else if (dep.exports.include.size() > 1) {
                                    xml.'include-set'() {
                                        dep.exports.include.each() { xml.'path'(name: it) }
                                    }
                                }
                            }

                            // exclude
                            if (dep.exports.exclude != null) {
                                if (dep.exports.exclude instanceof String || dep.exports.exclude instanceof GString || dep.exports.exclude.size() == 1) {
                                    xml.'exclude'(path: dep.exports.exclude.toString())
                                } else if (dep.exports.exclude.size() > 1) {
                                    xml.'exclude-set'() {
                                        dep.exports.exclude.each() { xml.'path'(name: it) }
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