package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild

import java.util.function.Consumer

/**
 *
 *             <xsd:element name="resources" type="resourcesType" minOccurs="0">
 *                 <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                     <documentation>
 *                         Lists the resource roots of this module (optional).
 *                     </documentation>
 *                 </annotation>
 *             </xsd:element>
 */
class ResourcesTag {

    /**
     *  Returns a function to update JBossModule.
     *
     * @param xml see resourcesType in the xsd
     * @return a function Consumer<JBossModule>
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        return { jbModule ->
            xml.resources.each() {
                it.'resource-root'.each() { r ->

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

                makeArtifacts((NodeChild) it, jbModule)
            }
        }
    }

    private static void makeArtifacts(final NodeChild tag, final JBossModule jbModule) {
        ['artifact', 'native-artifact'].each { name ->
            tag."${name}".each {
                def complexEl = [:]
                complexEl.type = name
                it.attributes().each {
                    complexEl.put(it.key, it.value)
                }

                it.filter.each() { f ->
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
