package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult

import java.util.function.Consumer

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
class ModuleAliasTag {

    /**
     *  Returns a function to update JBossModule.
     *
     * @param xml see moduleAliasType in the xsd
     * @return a function Consumer<JBossModule>
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        // xmlns='urn:jboss:module:x.y' -> x.y
        final String version = xml.namespaceURI().split(':').last()
        final boolean isSlotSupported = version in [Ver.V_1_1, Ver.V_1_2, Ver.V_1_3, Ver.V_1_5, Ver.V_1_6].collect {
            it.number
        }

        return { jbModule ->

            if (Ver.V_1_0.number.equals(version)) {
                // supported since 1.1
                return
            }

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
        }
    }
}
