package com.github.zhurlik.tag

import static com.github.zhurlik.Ver.V_1_3
import static com.github.zhurlik.Ver.V_1_5
import static com.github.zhurlik.Ver.V_1_6
import static com.github.zhurlik.Ver.V_1_7
import static com.github.zhurlik.Ver.V_1_8
import static com.github.zhurlik.Ver.V_1_9

import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

import java.util.function.Consumer

/**
 *     <xsd:complexType name="artifactType">
 *         <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *             <documentation>
 *                 A maven artifact within a deployment.
 *             </documentation>
 *         </annotation>
 *         <xsd:attribute name="name" type="xsd:string" use="required">
 *             <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                 <documentation>
 *                     URI that points to the maven artifact "group:artifact:version[:classifier]"
 *                 </documentation>
 *             </annotation>
 *         </xsd:attribute>
 *     </xsd:complexType>
 *
 * @author zhurlik@gmail.com
 */
class ArtifactTag {

    static Consumer<JBossModule> parse(final GPathResult xml) {
        return { final JBossModule jbModule ->
            ['artifact', 'native-artifact'].each { name ->
                xml."${name}".each {
                    def complexEl = [:]
                    complexEl.type = name
                    it.attributes().each {
                        complexEl.put(it.key, it.value)
                    }

                    it.filter.each { f ->
                        def filter = [:]
                        f.include.each {
                            filter.include = f.include.@path.text()
                        }
                        f.exclude.each {
                            filter.exclude = f.exclude.@path.text()
                        }
                        if (f.'exclude-set'.children().size() > 0) {
                            filter.exclude = f.'exclude-set'.path.collect { it.@name.text() }
                        }
                        if (f.'include-set'.children().size() > 0) {
                            filter.include = f.'include-set'.path.collect { it.@name.text() }
                        }
                        complexEl.filter = filter
                    }

                    it.conditions.each { c ->
                        ['property-equal', 'property-not-equal'].each { n ->
                            final String attr = c."$n".@name.text()
                            if (!(attr in ['', null])) {
                                complexEl.conditions = [:]
                                complexEl.conditions[n] = [name: attr, value: c."$n".@value.text()]
                            }
                        }
                    }

                    jbModule.resources.add(complexEl)
                }
            }
        }
    }

    /**
     *  Writes a maven artifact within this deployment.
     *
     *  <p>A maven native artifact within this deployment. This is a jar that contains a lib/ directory
     *  with corresponding platform directories and binaries. This element will cause the jar to
     *  be unzipped within the artifact's local repository directory.</p>
     *
     *  See either <xsd:element name="artifact" type="artifactType">
     *      or <xsd:element name="native-artifact" type="artifactType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        return { final MarkupBuilder xml ->
            if (!(jmodule.ver in [V_1_3, V_1_5, V_1_6, V_1_7, V_1_8, V_1_9])) {
                // do nothing
                return
            }

            jmodule.resources.findAll({
                ((it instanceof Map) && (it.type in ['artifact', 'native-artifact']))
            }).each() { res ->
                // URI that points to the maven artifact "group:artifact:version[:classifier]"
                addArtifact(xml, (Map) res)
            }
        }
    }

    /**
     * For adding either 'artifact' or 'native-artifact'.
     * @param xml
     * @param map
     */
    private static void addArtifact(final MarkupBuilder xml, final Map map) {
        xml."${map.type}"(name: map.name) {
            if (map.filter != null) {
                xml.filter() {
                    // include
                    if (map.filter.include != null) {
                        if (map.filter.include instanceof String
                                || map.filter.include instanceof GString
                                || map.filter.include.size() == 1) {
                            xml.'include'(path: map.filter.include.toString())
                        } else if (map.filter.include.size() > 1) {
                            xml.'include-set' {
                                map.filter.include.each {
                                    xml.'path'(name: it)
                                }
                            }
                        }
                    }

                    //exclude
                    if (map.filter.exclude != null) {
                        if (map.filter.exclude instanceof String
                                || map.filter.exclude instanceof GString
                                || map.filter.exclude.size() == 1) {
                            xml.'exclude'(path: map.filter.exclude.toString())
                        } else if (map.filter.exclude.size() > 1) {
                            xml.'exclude-set' {
                                map.filter.exclude.each {
                                    xml.'path'(name: it)
                                }
                            }
                        }
                    }
                }
            }

            if (map.conditions != null) {
                // TODO: is it possible to have a list of conditions?
                xml.'conditions' {
                    if (map.conditions.'property-equal' != null) {
                        xml.'property-equal'(name: map.conditions.'property-equal'.name.toString(),
                                value: map.conditions.'property-equal'.value.toString())
                    }
                    if (map.conditions.'property-not-equal' != null) {
                        xml.'property-not-equal'(name: map.conditions.'property-not-equal'.name.toString(),
                                value: map.conditions.'property-not-equal'.value.toString())
                    }
                }
            }
        }
    }
}
