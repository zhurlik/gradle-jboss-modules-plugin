package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

import java.util.function.Consumer

import static com.github.zhurlik.Ver.V_1_7
import static com.github.zhurlik.Ver.V_1_8
import static com.github.zhurlik.Ver.V_1_9

/**
 *    <xsd:complexType name="moduleDependencyType">
 *         <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *             <documentation>
 *                 A single module dependency expression.
 *             </documentation>
 *         </annotation>
 *         <xsd:all minOccurs="0">
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
 *             <xsd:element name="imports" type="filterType" minOccurs="0">
 *                 <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                     <documentation>
 *                         A filter used to restrict what packages or directories from this dependency are visible to this
 *                         module. See also the "services" attribute. The default action of this filter list is to reject
 *                         a path if not matched.
 *                     </documentation>
 *                 </annotation>
 *             </xsd:element>
 *         </xsd:all>
 *         <xsd:attribute name="name" type="xsd:string" use="required">
 *             <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                 <documentation>
 *                     The dependency module name (required).
 *                 </documentation>
 *             </annotation>
 *         </xsd:attribute>
 *         <xsd:attribute name="export" type="xsd:boolean" default="false">
 *             <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                 <documentation>
 *                     Specifies whether this module dependency is re-exported by default (default is "false"). Setting
 *                     this attribute to true sets the default action for the export filter list to "accept"; leaving it
 *                     as false sets the default action to "reject".  Thus you can still export dependency resources even
 *                     if this attribute is false by listing explicit paths for the export list.
 *                 </documentation>
 *             </annotation>
 *         </xsd:attribute>
 *         <xsd:attribute name="services" type="serviceDispositionType" default="none">
 *             <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                 <documentation>
 *                     Specifies whether and how services found in this dependency are used (default is "none"). Specifying
 *                     a value of "import" for this attribute is equivalent to adding a filter at the end of the import
 *                     filter list which includes the META-INF/services path from the dependency module.  Setting a value
 *                     of "export" for this attribute is equivalent to the same action on the export filter list.
 *                 </documentation>
 *             </annotation>
 *         </xsd:attribute>
 *         <xsd:attribute name="optional" type="xsd:boolean" default="false">
 *             <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                 <documentation>
 *                     Specifies whether this dependency is optional (defaults to false). An optional dependency will not
 *                     cause the module to fail to load if not found; however if the module is added later, it will not be
 *                     retroactively linked into this module's dependency list.
 *                 </documentation>
 *             </annotation>
 *         </xsd:attribute>
 *     </xsd:complexType>
 */
class ModuleDependencyTag {
    /**
     * To parse <xsd:complexType name="moduleDependencyType">
     *
     * @param xml a little bit of xml
     * @param jbModule
     * @return
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        return { final JBossModule jbModule ->
            xml.module.each() { d ->
                def dep = [:]
                if (d.attributes().size() == 1) {
                    dep.name = d.@name.toString()
                } else {
                    d.attributes().each() {
                        dep[it.key] = it.value
                    }
                }

                // imports
                d.imports.each() {
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
                    dep.imports = map
                }

                // exports
                d.exports.each() {
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
                    dep.exports = map
                }

                // properties since 1.9
                if (jbModule.ver == Ver.V_1_9 && !d.properties.isEmpty()) {
                    def props = [:]
                    d.properties.property.each {
                        props.put(it.@name.text(), it.@value.text())
                    }
                    dep.properties = props
                }

                jbModule.dependencies.add(dep)
            }
        }
    }

    /**
     * Writes a specified module dependency.
     *      <xsd:element name="module" type="moduleDependencyType">
     *           <annotation xmlns="http://www.w3.org/2001/XMLSchema">
     *               <documentation>
     *                   A specified module dependency.
     *               </documentation>
     *           </annotation>
     *       </xsd:element>
     * <p>A single module dependency expression.</p>
     * See <xsd:complexType name="moduleDependencyType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        final Ver version = jmodule.getVer()
        return { final MarkupBuilder xml ->
            // by default everything is module
            jmodule.dependencies.findAll({ !(it instanceof Map && it.type == 'system') }).each { dep ->
                // Attribute	Type	Required?	Description
                //name:	        string	    Yes	    The name of the module upon which this module depends.
                //slot:	        string	    No	    The version slot of the module upon which this module depends; defaults to "main".
                //export:	    boolean	    No	    Specify whether this dependency is re-exported by default; if not specified, defaults to "false".
                //services;	    enum	    No      Specify whether this dependency's services* are imported and/or exported. Possible values are "none", "import", or "export"; defaults to "none".
                //optional:	    boolean	    No	    Specify whether this dependency is optional; defaults to "false".
                if (dep instanceof String) {
                    xml.module(name: dep)
                    // next dependency
                    return
                }

                if (dep.services != null) {
                    assert dep.services in ['none', 'import', 'export']
                }

                if (dep.export != null) {
                    assert dep.export.toString() in ['true', 'false']
                }

                if (dep.optional != null) {
                    assert dep.optional.toString() in ['true', 'false']
                }

                if (dep.exports == null && dep.imports == null && dep.properties == null) {
                    xml.module(dep.sort())
                } else {
                    xml.module(dep.findAll() { el -> el.key in ['name', 'export', 'optional', 'services'] + (!(version in [V_1_7, V_1_8, V_1_9]) ? ['slot'] : []) }.sort()) {
                        // imports
                        if (dep.imports != null) {
                            xml.imports() {
                                // include
                                if (dep.imports.include != null) {
                                    if (dep.imports.include instanceof String || dep.imports.include instanceof GString || dep.imports.include.size() == 1) {
                                        xml.'include'(path: dep.imports.include.toString())
                                    } else if (dep.imports.include.size() > 1) {
                                        xml.'include-set'() {
                                            dep.imports.include.each() { xml.'path'(name: it) }
                                        }
                                    }
                                }

                                // exclude
                                if (dep.imports.exclude != null) {
                                    if (dep.imports.exclude instanceof String || dep.imports.exclude instanceof GString || dep.imports.exclude.size() == 1) {
                                        xml.'exclude'(path: dep.imports.exclude.toString())
                                    } else if (dep.imports.exclude.size() > 1) {
                                        xml.'exclude-set'() {
                                            dep.imports.exclude.each() { xml.'path'(name: it) }
                                        }
                                    }
                                }
                            }
                        }

                        // exports
                        if (dep.exports != null) {
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

                        // properties since 1.9
                        if (dep.properties != null && version == V_1_9) {
                            xml.properties() {
                                dep.properties.findAll() {
                                    !(it.key in [null, '']) && !(it.value in [null, ''])
                                }.each { p ->
                                    xml.property(name: p.key, value: p.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
