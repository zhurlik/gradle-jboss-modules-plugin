package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

import java.util.function.Consumer

import static com.github.zhurlik.Ver.V_1_8
import static com.github.zhurlik.Ver.V_1_9

/**
 *            <xsd:element name="dependencies" type="dependenciesType" minOccurs="0">
 *                 <annotation xmlns="http://www.w3.org/2001/XMLSchema">
 *                     <documentation>
 *                         Lists the dependencies of this module (optional).
 *                     </documentation>
 *                 </annotation>
 *             </xsd:element>
 */
class DependenciesTag {

    /**
     *  Returns a function to update JBossModule.
     *
     * @param xml see dependenciesType in the xsd
     * @return a function Consumer<JBossModule>
     */
    static Consumer<JBossModule> parse(final GPathResult xml) {
        // xmlns='urn:jboss:module:x.y' -> x.y
        final String version = xml.namespaceURI().split(':').last()

        return { jbModule ->
            xml.dependencies.each() {
                // modules
                ModuleDependencyTag.parse(it).accept(jbModule)

                // systems
                if (!(version in [Ver.V_1_8.number])) {
                    SystemDependencyTag.parse(it).accept(jbModule)
                }
            }

        }
    }

    /**
     * Writes a list of zero or more module dependencies.
     *
     *  <dependencies>
     *      <module name="javax.api"/>
     *      <module name="org.jboss.logging"/>
     *      <module name="org.jboss.modules"/>
     *      <!-- Optional deps -->
     *      <module name="javax.inject.api" optional="true"/>
     *      <module name="org.jboss.example">
     *         <imports>
     *            <exclude-set>
     *               <path name="org.jboss.example.tests"/>
     *            </exclude-set>
     *         </imports>
     *      </module>
     *      <system>
     *          ...
     *      </system>
     *  </dependencies>
     *  <p>Lists the dependencies of this module (optional).</p>
     *  See <xsd:element name="dependencies" type="dependenciesType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    static Consumer<MarkupBuilder> write(final JBossModule jmodule) {
        return { final MarkupBuilder xml ->
            final Ver version = jmodule.getVer()
            if (!jmodule.dependencies.isEmpty()) {
                xml.dependencies {
                    // modules
                    ModuleDependencyTag.write(jmodule).accept(xml)

                    // systems
                    if (!(version in [V_1_8, V_1_9])) {
                        SystemDependencyTag.write(jmodule).accept(xml)
                    }
                }
            }
        }
    }
}
