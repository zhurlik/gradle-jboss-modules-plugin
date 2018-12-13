package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

import java.util.function.Consumer
import java.util.function.Supplier

/**
 *   <xsd:complexType name="moduleType">
 *         <xsd:annotation>
 *             <xsd:documentation>
 *                 The module declaration type; contains dependencies, resources, and the main class
 *                 specification.
 *             </xsd:documentation>
 *         </xsd:annotation>
 *   </xsd:complexType>
 */
@Slf4j
class ModuleTag {

    /**
     *  Returns a function to make JBossModule.
     *
     * @param xml see moduleType in the xsd
     * @return a function Supplier<JBossModule>
     */
    static Optional<Supplier<JBossModule>> parse(final String txt) {
        final GPathResult xml = new XmlSlurper().parseText(txt)
        // xmlns='urn:jboss:module:x.y' -> x.y
        final String xsdVersion = xml.namespaceURI().split(':').last()
        final Ver version = Ver.values().find { it.number.equals(xsdVersion) }

        if (version.isValid(txt)) {
            return Optional.of({
                final JBossModule jbModule = new JBossModule(xml.@'name'.text())
                jbModule.ver = version

                xml.attributes().each() {
                    switch (it.key) {
                        case 'slot': jbModule.slot = it.value
                            break
                        case 'name': jbModule.moduleName = it.value
                            jbModule.name = it.value
                            break
                        case 'version': jbModule.version = it.value
                            break
                    }
                }

                jbModule.mainClass = xml.'main-class'.@name

                PropertiesTag.parse(xml).accept(jbModule)
                ResourcesTag.parse(xml).accept(jbModule)
                ExportsTag.parse(xml).accept(jbModule)
                PermissionsTag.parse(xml).accept(jbModule)
                DependenciesTag.parse(xml).accept(jbModule)
                ProvidesTag.parse(xml).accept(jbModule)

                log.debug '>> Module: \'{}\' has been created', jbModule.name

                return jbModule
            })
        } else {
            return Optional.empty()
        }
    }

    /**
     *  Specifies the main class of this module; used to run the module from the command-line (optional).
     *  <br/>
     *  See <xsd:element name="main-class" type="classNameType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> writeMainClass(final JBossModule jmodule) {
        return { final MarkupBuilder xml ->
            if (!(jmodule.mainClass in [null, ''])) {
                xml.'main-class'(name: jmodule.mainClass)
            }
        }
    }
}
