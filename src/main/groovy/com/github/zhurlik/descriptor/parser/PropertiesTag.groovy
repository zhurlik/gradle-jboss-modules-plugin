package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

import java.util.function.Consumer

import static com.github.zhurlik.Ver.V_1_0

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

    /**
     * Writes lists the user-defined properties to be associated with this module (optional).
     *  <properties>
     *     <property name="my.property" value="foo"/>
     *  </properties>
     *
     *  <br/>
     *  See <xsd:element name="properties" type="propertyListType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        return { final MarkupBuilder xml ->
            if (!(jmodule.ver in V_1_0) && !jmodule.properties.isEmpty()) {
                xml.properties {
                    jmodule.properties.findAll() { !(it.key in [null, '']) && !(it.value in [null, '']) }.each() {
                        xml.property(name: it.key, value: it.value)
                    }
                }
            }
        }
    }
}
