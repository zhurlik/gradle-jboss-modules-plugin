package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

import java.util.function.Consumer

import static com.github.zhurlik.Ver.V_1_3
import static com.github.zhurlik.Ver.V_1_5
import static com.github.zhurlik.Ver.V_1_6
import static com.github.zhurlik.Ver.V_1_7
import static com.github.zhurlik.Ver.V_1_8
import static com.github.zhurlik.Ver.V_1_9

/**
 *
 *             <xsd:element name="resources" type="resourcesType" minOccurs="0">
 *                 <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                     <documentation>
 *                         Lists the resource roots of this module (optional).
 *                     </documentation>
 *                 </annotation>
 *             </xsd:element>
 */
class ResourcesTag {

    /**
     *  Returns a function to update JBossModule.
     *
     * @param xml see resourcesType in the xsd
     * @return a function Consumer<JBossModule>
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        return { jbModule ->
            xml.resources.each() {
                ResourceRootTag.parse(it).accept(jbModule)
                ArtifactTag.parse(it).accept(jbModule)
            }
        }
    }

    /**
     * Writes a list of zero or more resource roots for this deployment.
     *  <resources>
     *      <resource-root path="jboss-msc-1.0.1.GA.jar" name="bla-bla">
     *          <filter>
     *              <include path=''/>
     *              ...
     *              <exclude-set>
     *              ...
     *              <exclude-set/>
     *          <filter>
     *      <resource-root/>
     *      <artifact .../>
     *      <native-artifact .../>
     *  </resources>
     *  <p>Lists the resource roots of this module (optional).</p>
     *
     *  See <xsd:element name="resources" type="resourcesType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        return { final MarkupBuilder xml ->
            if (!jmodule.resources.isEmpty()) {
                xml.resources {
                    // <resource-root>
                    ResourceRootTag.write(jmodule).accept(xml)

                    if (jmodule.ver in [V_1_3, V_1_5, V_1_6, V_1_7, V_1_8, V_1_9]) {
                        // either <artifact> or <native-artifact>
                        ArtifactTag.write(jmodule).accept(xml)
                    }
                }
            }
        }
    }
}
