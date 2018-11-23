package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult

import java.util.function.Supplier

/**
 * This class implements a logic to parse moduleAliasType tag.
 *
 *    <!-- Root element -->
 *     <xsd:element name="module-alias" type="moduleAliasType">
 *         <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *             <documentation>
 *                 Root element for a module alias declaration.
 *             </documentation>
 *         </annotation>
 *     </xsd:element>
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class ModuleAliasTag {

    /**
     *  Returns a function to update JBossModule.
     *
     * @param txt see moduleAliasType in the xsd
     * @return a function Supplier<JBossModule>
     */
    static Optional<Supplier<JBossModule>> parse(final String txt) {
        final GPathResult xml = new XmlSlurper().parseText(txt)
        // xmlns='urn:jboss:module:x.y' -> x.y
        final String xsdVersion = xml.namespaceURI().split(':').last()
        final Ver version = Ver.values().find { it.number.equals(xsdVersion) }
        final boolean isSlotSupported = version in [Ver.V_1_1, Ver.V_1_2, Ver.V_1_3, Ver.V_1_5, Ver.V_1_6]

        if (version.isValid(txt) && 'module-alias'.equals(xml.name())) {
            return Optional.of(new Supplier<JBossModule>(){

                @Override
                JBossModule get() {
                    //result
                    final JBossModule jbModule = new JBossModule('empty')
                    jbModule.ver = version
                    jbModule.moduleAlias = true
                    xml.attributes().each() {
                        switch (it.key) {
                            case 'slot':
                                if (isSlotSupported) {
                                    jbModule.slot = it.value
                                }
                                break
                            case 'name': jbModule.moduleName = it.value
                                jbModule.name = it.value
                                break
                            case 'target-name': jbModule.targetName = it.value
                                break
                        }
                    }

                    log.debug '>> Module: \'{}\' has been created', jbModule.name
                    return jbModule
                }
            })
        } else {
            return Optional.empty()
        }
    }
}