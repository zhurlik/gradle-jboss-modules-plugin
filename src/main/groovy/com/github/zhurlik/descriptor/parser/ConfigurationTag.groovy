package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult

import java.util.function.Consumer

/**
 * This class implements a logic to parse configurationType tag.
 *
 *     <!-- Root element -->
 *     <xsd:element name="configuration" type="configurationType">
 *         <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *             <documentation>
 *                 Root element for a filesystem module loader configuration.
 *             </documentation>
 *         </annotation>
 *     </xsd:element>
 *
 * @author zhurlik@gmail.com
 */
class ConfigurationTag {
    private static final String[] SUPPORTED = [Ver.V_1_0, Ver.V_1_1].collect { it.number }

    /**
     *  Returns a function to update JBossModule.
     *
     * @param xml see configurationType in the xsd
     * @return a function Consumer<JBossModule>
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        // xmlns='urn:jboss:module:x.y' -> x.y
        final String version = xml.namespaceURI().split(':').last()

        return { jbModule ->

            if (!(version in SUPPORTED)) {
                // unsupported since 1.2
                return
            }

            jbModule.moduleConfiguration = true
            jbModule.defaultLoader = xml.@'default-loader'.text()

            xml.loader.each { l ->
                def el = [:]
                el.name = l.@name.text()

                l.import.each {
                    el.import = it.text()
                }

                l.'module-path'.each {
                    el['module-path'] = it.@name.text()
                }

                jbModule.loaders.add(el)
            }

            if (jbModule.loaders.empty) {
                jbModule.loaders.add(xml.@'default-loader'.text())
            }
        }
    }
}
