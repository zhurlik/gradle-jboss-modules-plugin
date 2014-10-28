package com.zhurlik.descriptor

import com.zhurlik.JBossModule
import groovy.xml.MarkupBuilder

import static com.zhurlik.descriptor.IBuilder.Ver.V_1_1

/**
 * Generates a xml descriptor for JBoss Module ver.1.1
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_1.xsd
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_1 implements IBuilder<JBossModule> {

    @Override
    String getXmlDescriptor(final JBossModule jmodule) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        // <?xml version="1.0" encoding="UTF-8"?>
        xml.mkp.xmlDeclaration(version: '1.0', encoding: 'utf-8')

        // <module xmlns="urn:jboss:module:1.1" name="org.jboss.msc">
        xml.module([xmlns: 'urn:jboss:module:' + V_1_1.version, name: jmodule.moduleName] + ((jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot])) {

            // <main-class name="org.jboss.msc.Version"/>
            if (!(jmodule.mainClass in [null, ''])) {
                'main-class'(name: jmodule.mainClass)
            }

            //  <properties>
            //     <property name="my.property" value="foo"/>
            //  </properties>
            if (!jmodule.properties.isEmpty()) {
                delegate.properties {
                    jmodule.properties.each() {
                        property(name: it.key, value: it.value)
                    }
                }
            }

            //  <resources>
            //      <resource-root path="jboss-msc-1.0.1.GA.jar" name="bla-bla">
            //          <filter/>
            //      <resource-root/>
            //   </resources>
            if (!jmodule.resources.isEmpty()) {
                delegate.resources {
                    jmodule.resources.each() {
                        if (it instanceof String) {
                            'resource-root'(path: it)
                        } else {
                            if (it.filter != null) {
                                'resource-root'(it.findAll() { it.key in ['name', 'path'] }) {
                                    delegate.filter()
                                }
                            } else {
                                'resource-root'(it.findAll() { it.key in ['name', 'path'] })
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
            if (!jmodule.dependencies.isEmpty()) {
                delegate.dependencies {
                    jmodule.dependencies.each() { dep ->
                        // Attribute	Type	Required?	Description
                        //name:	        string	    Yes	    The name of the module upon which this module depends.
                        //slot:	        string	    No	    The version slot of the module upon which this module depends; defaults to "main".
                        //export:	    boolean	    No	    Specify whether this dependency is re-exported by default; if not specified, defaults to "false".
                        //services;	    enum	    No      Specify whether this dependency's services* are imported and/or exported. Possible values are "none", "import", or "export"; defaults to "none".
                        //optional:	    boolean	    No	    Specify whether this dependency is optional; defaults to "false".
                        if (dep.exports == null && dep.imports == null) {
                            delegate.module(dep)
                        } else {
                            delegate.module(dep.findAll() { el -> el.key in ['name', 'slot', 'export', 'optional'] }) {
                                // imports
                                if (dep.imports != null) {
                                    delegate.imports() {
                                        if (dep.imports.include instanceof String) {
                                            'include'(path: dep.imports.include)
                                        } else {
                                            if (dep.imports.include != null && !dep.imports.include.isEmpty()) {
                                                if (dep.imports.include.size() > 1) {
                                                    'include-set'() {
                                                        dep.imports.include.each() { 'path'(name: it) }
                                                    }
                                                } else if (dep.imports.include.size() == 1) {
                                                    'include'(path: dep.imports.include[0])
                                                }
                                            }
                                        }

                                        if (dep.imports.exclude instanceof String) {
                                            'exclude'(path: dep.imports.exclude)
                                        } else {
                                            if (dep.imports.exclude != null && !dep.imports.exclude.isEmpty()) {
                                                if (dep.imports.exclude.size() > 1) {
                                                    'exclude-set'() {
                                                        dep.imports.exclude.each() { 'path'(name: it) }
                                                    }
                                                } else if (dep.imports.exclude.size() == 1) {
                                                    'exclude'(path: dep.imports.exclude[0])
                                                }
                                            }
                                        }
                                    }
                                }

                                // exports
                                if (dep.exports != null) {
                                    delegate.exports() {
                                        if (dep.exports.include instanceof String) {
                                            'include'(path: dep.exports.include)
                                        } else {
                                            if (dep.exports.include != null && !dep.exports.include.isEmpty()) {
                                                if (dep.exports.include.size() > 1) {
                                                    'include-set'() {
                                                        dep.exports.include.each() { 'path'(name: it) }
                                                    }
                                                } else if (dep.exports.include.size() == 1) {
                                                    'include'(path: dep.exports.include[0])
                                                }
                                            }
                                        }

                                        if (dep.exports.exclude instanceof String) {
                                            'exclude'(path: dep.exports.exclude)
                                        } else {
                                            if (dep.exports.exclude != null && !dep.exports.exclude.isEmpty()) {
                                                if (dep.exports.exclude.size() > 1) {
                                                    'exclude-set'() {
                                                        dep.exports.exclude.each() { 'path'(name: it) }
                                                    }
                                                } else if (dep.exports.exclude.size() == 1) {
                                                    'exclude'(path: dep.export.exclude[0])
                                                }
                                            }
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
