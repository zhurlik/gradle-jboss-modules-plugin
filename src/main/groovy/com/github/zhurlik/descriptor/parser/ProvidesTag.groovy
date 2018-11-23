package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild

import java.util.function.Consumer

/**
 *            <xsd:element name="provides" type="providesType" minOccurs="0">
 *                 <xsd:annotation>
 *                     <xsd:documentation>
 *                         Lists items that are statically provided by this module.
 *                     </xsd:documentation>
 *                 </xsd:annotation>
 *             </xsd:element>
 */
class ProvidesTag {
    /**
     *  Returns a function to update JBossModule.
     *
     * @param xml see providesType in the xsd
     * @return a function Consumer<JBossModule>
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        // xmlns='urn:jboss:module:x.y' -> x.y
        final String version = xml.namespaceURI().split(':').last()

        return { jbModule ->
            xml.provides.each {
                // services
                parseServices(it, jbModule)
            }
        }
    }

    /**
     * New feature since version 1.8
     *
     * @param tag xml tag
     * @param jbModule current module
     */
    private static void parseServices(final NodeChild tag, final JBossModule jbModule) {
        tag.children().each {
            // simple
            if (it.'with-class'.size() == 0) {
                jbModule.provides.add(it.@name.text())
            }
            // complex
            if (it.'with-class'.size() > 0) {
                def service = [:]
                service.name = it.@name.text()
                service['with-class'] = it.'with-class'.collect { it.@name.text() }

                jbModule.provides.add(service)
            }
        }
    }
}
