package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult

import java.util.function.Consumer

/**
 *  <xsd:element name="properties" type="propertyListType" minOccurs="0">
 *      <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *          <documentation>
 *              Lists the user-defined properties to be associated with this module (optional).
 *          </documentation>
 *      </annotation>
 *  </xsd:element>
 */
class PropertiesTag {

    /**
     *  Returns a function to update JBossModule.
     *
     * @param xml see propertyListType in the xsd
     * @return a function Consumer<JBossModule>
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        return { jbModule ->
            xml.properties.each() {
                it.property.each() { p ->
                    jbModule.properties.put(p.@name.toString(), p.@value.toString())
                }
            }
        }
    }
}
