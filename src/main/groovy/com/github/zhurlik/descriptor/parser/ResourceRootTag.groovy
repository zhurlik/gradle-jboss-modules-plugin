package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

import java.util.function.Consumer

import static com.github.zhurlik.Ver.V_1_8
import static com.github.zhurlik.Ver.V_1_9

/**
 *  <xsd:complexType name="resourceType">
 *         <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *             <documentation>
 *                 A resource root within a deployment.
 *             </documentation>
 *         </annotation>
 *         <xsd:all>
 *             <xsd:element name="filter" type="filterType" minOccurs="0">
 *                 <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                     <documentation>
 *                         A path filter specification for this resource root (optional). By default all paths are
 *                         accepted.
 *                     </documentation>
 *                 </annotation>
 *             </xsd:element>
 *         </xsd:all>
 *         <xsd:attribute name="name" type="xsd:string" use="optional">
 *             <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                 <documentation>
 *                     The name of this resource root (optional). If not specified, defaults to the value of the path
 *                     attribute.
 *                 </documentation>
 *             </annotation>
 *         </xsd:attribute>
 *         <xsd:attribute name="path" type="xsd:string" use="required">
 *             <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                 <documentation>
 *                     The path of this resource root, either absolute or relative. Relative paths are resolved with
 *                     respect to the location at which the module.xml file is found.
 *                 </documentation>
 *             </annotation>
 *         </xsd:attribute>
 *     </xsd:complexType>
 *
 */
class ResourceRootTag {

    /**
     *  Returns a function to update JBossModule.
     *
     * @param xml see resourceType in the xsd
     * @return a function Consumer<JBossModule>
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        return { jbModule ->
            xml.'resource-root'.each { r ->

                def complexEl = [:]

                if (r.attributes().size() == 1) {
                    complexEl.path = r.@path.text()
                } else {
                    complexEl.name = r.@name.text()
                    complexEl.path = r.@path.text()
                }

                r.filter.each() { f ->
                    def filter = [:]
                    f.include.each() {
                        filter.include = f.include.@path.text()
                    }
                    f.exclude.each() {
                        filter.exclude = f.exclude.@path.text()
                    }
                    if (f.'exclude-set'.children().size() > 0) {
                        filter.exclude = f.'exclude-set'.path.collect() { it.@name.text() }
                    }
                    if (f.'include-set'.children().size() > 0) {
                        filter.include = f.'include-set'.path.collect() { it.@name.text() }
                    }
                    complexEl.filter = filter
                }

                jbModule.resources.add(complexEl)
            }
        }
    }

    /**
     *  Writes a resource root within a deployment.
     *
     *  <p>A resource root within this deployment.</p>
     *
     *  See <xsd:element name="resource-root" type="resourceType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        return { final MarkupBuilder xml ->
            final Ver version = jmodule.getVer()
            jmodule.resources.findAll({
                !((it instanceof Map) && (it.type in ['artifact', 'native-artifact']))
            }).each() { res ->
                if (res instanceof String || res instanceof GString) {
                    xml.'resource-root'(path: res.toString())
                    // next resource
                    return
                }
                if (res.filter != null) {
                    xml.'resource-root'(res.findAll() { it.key in ['name', 'path'] }) {
                        xml.filter() {
                            // include
                            if (res.filter.include != null) {
                                if (res.filter.include instanceof String || res.filter.include instanceof GString || res.filter.include.size() == 1) {
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
                                if (res.filter.exclude instanceof String || res.filter.exclude instanceof GString || res.filter.exclude.size() == 1) {
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
                } else {
                    xml.'resource-root'(res.findAll() {
                        it.key in ['path'] + (!(version in [V_1_8, V_1_9]) ? ['name'] : [])
                    })
                }
            }
        }
    }
}
