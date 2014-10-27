package com.zhurlik.descriptor

import com.zhurlik.JBossModule
import groovy.xml.MarkupBuilder

import static com.zhurlik.descriptor.IBuilder.Ver.V_1_1

/**
 * @author zhurlik@gmail.com
 */
class Xsd1_1 implements IBuilder<JBossModule> {

    @Override
    String getXmlDescriptor(final JBossModule module) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        // <?xml version="1.0" encoding="UTF-8"?>
        xml.mkp.xmlDeclaration(version: '1.0', encoding: 'utf-8')

        // <module xmlns="urn:jboss:module:1.1" name="org.jboss.msc">
        xml.module([xmlns: 'urn:jboss:module:' + V_1_1.version, name: module.moduleName] + ((module.slot in [null, '']) ? [:] : [slot: module.slot])) {

            // <main-class name="org.jboss.msc.Version"/>
            if (!(module.mainClass in [null, ''])) {
                'main-class'(name: module.mainClass)
            }

            //  <properties>
            //     <property name="my.property" value="foo"/>
            //  </properties>
            if (!module.properties.isEmpty()) {
                delegate.properties {
                    module.properties.each() {
                        property(name: it.key, value: it.value)
                    }
                }
            }

            //  <resources>
            //      <resource-root path="jboss-msc-1.0.1.GA.jar" name="bla-bla">
            //          <filter/>
            //      <resource-root/>
            //   </resources>
            if (!module.resources.isEmpty()) {
                delegate.resources {
                    module.resources.each() {
                        if (it instanceof String) {
                            'resource-root'(path: it)
                        } else {
                            if (it.filter != null) {
                                'resource-root'(it.findAll() {it.key in ['name', 'path']}) {
                                    delegate.filter()
                                }
                            }
                        }
                    }
                }
            }

            //  <dependencies>
            //      <module name="javax.api"/>
            //      <module name="org.jboss.logging"/>
            //      <module name="org.jboss.modules"/>
            //      <!-- Optional deps -->
            //      <module name="javax.inject.api" optional="true"/>
            //      <module name="org.jboss.example">
            //         <imports>
            //            <exclude-set>
            //               <path name="org.jboss.example.tests"/>
            //            </exclude-set>
            //         </imports>
            //      </module>
            //  </dependencies>
            if (!module.dependencies.isEmpty()) {
                delegate.dependencies {
                    module.dependencies.each() {dep->
                        // Attribute	Type	Required?	Description
                        //name:	        string	    Yes	    The name of the module upon which this module depends.
                        //slot:	        string	    No	    The version slot of the module upon which this module depends; defaults to "main".
                        //export:	    boolean	    No	    Specify whether this dependency is re-exported by default; if not specified, defaults to "false".
                        //services;	    enum	    No      Specify whether this dependency's services* are imported and/or exported. Possible values are "none", "import", or "export"; defaults to "none".
                        //optional:	    boolean	    No	    Specify whether this dependency is optional; defaults to "false".
                        if (dep.exports == null && dep.imports == null) {
                            module(dep)
                        } else {
                            module(dep.findAll() {el-> el.key in ['name', 'slot', 'export', 'optional']}) {
                                // imports
                                if (dep.imports != null) {
                                    delegate.imports() {
                                        dep.imports.each() {
                                            'import'()
                                        }
                                    }
                                }

                                // exports
                                if (dep.exports != null) {
                                    delegate.exports() {
                                        dep.exports.each() {
                                            'export'()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return writer.toString()
    }
}
