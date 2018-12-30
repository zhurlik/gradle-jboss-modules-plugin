package com.github.zhurlik.tag

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

import java.util.function.Consumer
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

    /**
     * Writes a configuration for the default module loader.
     * <p>Root element for a filesystem module loader configuration.</p>
     * See <xsd:element name="configuration" type="configurationType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        final Ver version = jmodule.getVer()
        return { final MarkupBuilder xml ->
            if (jmodule.isModuleConfiguration()) {
                assert jmodule.defaultLoader != null, 'Default-Loader is null'

                xml.configuration([xmlns: 'urn:jboss:module:' + version.number, 'default-loader': jmodule.defaultLoader]) {
                    if (jmodule.loaders.empty) {
                        loader([name: jmodule.defaultLoader])
                    }

                    jmodule.loaders.each { l ->
                        if (l instanceof String) {
                            loader([name: l])
                        } else {
                            loader([name: l.name]) {
                                if (l['import'] != null) {
                                    'import'(l['import'])
                                }

                                if (l['module-path'] != null) {
                                    'module-path'([name: l['module-path']])
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
