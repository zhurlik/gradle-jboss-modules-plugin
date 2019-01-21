package com.github.zhurlik.tag

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

import java.util.function.Consumer
import java.util.function.Supplier

import static com.github.zhurlik.Ver.V_1_7
import static com.github.zhurlik.Ver.V_1_8
import static com.github.zhurlik.Ver.V_1_9

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
        GPathResult xml = new XmlSlurper().parseText(txt)
        // xmlns='urn:jboss:module:x.y' -> x.y
        String xsdVersion = xml.namespaceURI().split(':').last()
        Ver version = Ver.values().find { it.number.equals(xsdVersion) }
        boolean isSlotSupported = version in [Ver.V_1_1, Ver.V_1_2, Ver.V_1_3, Ver.V_1_5, Ver.V_1_6]

        if (version.isValid(txt) && 'module-alias'.equals(xml.name())) {
            return Optional.of(new Supplier<JBossModule>(){

                @Override
                JBossModule get() {
                    //result
                    JBossModule jbModule = new JBossModule('empty')
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

    /**
     * Writes a module alias type, which defines the target for a module alias.
     * <p>Root element for a module alias declaration.</p>
     * See <xsd:element name="module-alias" type="moduleAliasType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        return { final MarkupBuilder xml ->
            // <module-alias xmlns="urn:jboss:module:1.{1|3}" name="javax.json.api" target-name="org.glassfish.javax.json"/>
            Objects.requireNonNull(jmodule.targetName, 'Target Name is null')

            Ver version = jmodule.getVer()
            def attrs = [xmlns: 'urn:jboss:module:' + version.number, name: jmodule.moduleName]
            attrs += (jmodule.slot in [null, ''] || version in [V_1_7, V_1_8, V_1_9]) ? [:] : [slot: jmodule.slot]
            attrs.put('target-name', jmodule.targetName)
            xml.'module-alias'(attrs)
        }
    }
}