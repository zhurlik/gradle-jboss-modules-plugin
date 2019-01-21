package com.github.zhurlik.tag

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

import java.util.function.Consumer

/**
 *     <xsd:complexType name="moduleAbsentType">
 *         <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *             <documentation>
 *                 An explicitly absent module.
 *             </documentation>
 *         </annotation>
 *         <xsd:attribute name="name" type="moduleNameType" use="required">
 *             <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                 <documentation>
 *                     The name of the absent module (required).
 *                 </documentation>
 *             </annotation>
 *         </xsd:attribute>
 *         <xsd:attribute name="slot" type="moduleSlotType" use="optional">
 *             <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                 <documentation>
 *                     The version slot of the absent module (optional).
 *                 </documentation>
 *             </annotation>
 *         </xsd:attribute>
 *     </xsd:complexType>
 */
class ModuleAbsentTag {

    /**
     * Writes an explicitly absent module.
     * <p>Root element for an absent module.</p>
     * See <xsd:element name="module-absent" type="moduleAbsentType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        return { final MarkupBuilder xml ->
            Objects.requireNonNull(jmodule.moduleName != null, 'Module Name is null')

            Ver version = jmodule.getVer()
            def attrs = [xmlns: 'urn:jboss:module:' + version.number, name: jmodule.moduleName]
            attrs += (jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot]

            xml.'module-absent'(attrs)
        }
    }
}
