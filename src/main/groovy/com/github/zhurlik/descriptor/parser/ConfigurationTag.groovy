package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult

import java.util.function.Supplier

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
@Slf4j
class ConfigurationTag {

    /**
     *  Returns a function to make JBossModule.
     *
     * @param txt see configurationType in the xsd
     * @return a function Supplier<JBossModule>
     */
    static Optional<Supplier<JBossModule>> parse(final String txt) {
        final GPathResult xml = new XmlSlurper().parseText(txt)
        // xmlns='urn:jboss:module:x.y' -> x.y
        final String xsdVersion = xml.namespaceURI().split(':').last()
        final Ver version = Ver.values().find { it.number.equals(xsdVersion) }

        if (version.isValid(txt) && 'configuration'.equals(xml.name())) {
            return Optional.of({
                //result
                final JBossModule jbModule = new JBossModule('empty')
                jbModule.ver = version
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

                log.debug '>> Module: \'{}\' has been created', jbModule.name
                return jbModule
            })
        } else {
            Optional.empty()
        }
    }
}
