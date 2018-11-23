package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult

import java.util.function.Consumer

/**
 *             <xsd:element name="permissions" type="permissionsType" minOccurs="0">
 *                 <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                     <documentation>
 *                         Lists the requested permission set for this module. If the requested permissions cannot
 *                         be assigned, the module cannot be loaded.
 *                     </documentation>
 *                 </annotation>
 *             </xsd:element>
 */
class PermissionsTag {
    /**
     *  Returns a function to update JBossModule.
     *
     * @param xml see permissionsType in the xsd
     * @return a function Consumer<JBossModule>
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        return { jbModule ->
            xml.permissions.each { p ->
                p.grant.each {
                    def g = [:]
                    it.attributes().each({ g.putAt(it.key, it.value) })
                    jbModule.permissions.add(g)
                }
            }
        }
    }
}