package com.github.zhurlik.tag

import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import groovy.xml.MarkupBuilder

import java.util.function.Consumer

import static com.github.zhurlik.Ver.V_1_8
import static com.github.zhurlik.Ver.V_1_9

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

    /**
     * Writes lists items that are statically provided by this module.
     *  <providers>
     *    <service name=''>
     *        <with-class name='class'/>
     *    <service/>
     *  </providers>
     * <p>Lists items that are statically provided by this module.</p>
     *
     * See <xsd:element name="provides" type="providesType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        return { final MarkupBuilder xml ->
            // supported in 1.8 and 1.9
            if (!(jmodule.ver in [V_1_8, V_1_9])) {
                return
            }

            if (!jmodule.provides.isEmpty()) {
                xml.provides {
                    jmodule.provides.each { s ->
                        // simple
                        if (s instanceof String || s instanceof GString) {
                            service(name: s.toString())
                        }

                        // complex
                        if (s instanceof Map) {
                            service(name: s.name) {
                                def withClass = s['with-class']
                                if (withClass instanceof String || s instanceof GString) {
                                    'with-class'(name: withClass.toString())
                                }
                                if (withClass instanceof Collection) {
                                    withClass.each {
                                        'with-class'(name: it)
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
