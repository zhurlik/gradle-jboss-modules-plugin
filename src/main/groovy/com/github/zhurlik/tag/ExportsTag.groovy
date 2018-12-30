package com.github.zhurlik.tag

import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

import java.util.function.Consumer

/**
 *             <xsd:element name="exports" type="filterType" minOccurs="0">
 *                 <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                     <documentation>
 *                         Lists filter expressions to apply to the export filter of the local resources of this module
 *                         (optional). By default, everything is exported. If filter expressions are provided, the default
 *                         action is to accept all paths if no filters match.
 *                     </documentation>
 *                 </annotation>
 *             </xsd:element>
 *
 */
class ExportsTag {

    /**
     *  Returns a function to update JBossModule.
     *
     * @param xml see filterType in the xsd
     * @return a function Consumer<JBossModule>
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        return { jbModule ->
            xml.exports.each() {
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
                jbModule.exports = map
            }
        }
    }

    /**
     * Writes
     *  <exports>
     *      <exclude path="..."/>
     *  </exports>
     * <p>
     *   Lists filter expressions to apply to the export filter of the local resources of this module
     *   (optional). By default, everything is exported. If filter expressions are provided, the default
     *   action is to accept all paths if no filters match.
     * </p>
     * See <xsd:element name="exports" type="filterType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        return { final MarkupBuilder xml ->
            // exports
            if (!jmodule.exports.empty) {
                xml.'exports'() {

                    // include
                    if (jmodule.exports.include != null) {
                        if (jmodule.exports.include instanceof String || jmodule.exports.include instanceof GString || jmodule.exports.include.size() == 1) {
                            xml.'include'(path: jmodule.exports.include.toString())
                        } else if (jmodule.exports.include.size() > 1) {
                            xml.'include-set'() {
                                jmodule.exports.include.each() { xml.'path'(name: it) }
                            }
                        }
                    }

                    // exclude
                    if (jmodule.exports.exclude != null) {
                        if (jmodule.exports.exclude instanceof String || jmodule.exports.exclude instanceof GString || jmodule.exports.exclude.size() == 1) {
                            xml.'exclude'(path: jmodule.exports.exclude.toString())
                        } else if (jmodule.exports.exclude.size() > 1) {
                            xml.'exclude-set'() {
                                jmodule.exports.exclude.each() { xml.'path'(name: it) }
                            }
                        }
                    }
                }
            }
        }
    }
}
