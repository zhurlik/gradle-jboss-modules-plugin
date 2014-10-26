package com.zhurlik

/**
 * To make JBoss Module.
 * https://docs.jboss.org/author/display/MODULES/Home
 *
 * @author zhurlik@gmail.com
 */
class JBossModule {
    def String name, moduleName, slot, mainClass
    def properties = [:]
    def resources, dependencies = []
    def exports

    /**
     * The special constructor to be able to use in the gradle script
     *
     * modules {
     *     moduleA {
     *          moduleName = 'com.moduleA'
     *         slot = '1.0'
     *     }
     * }
     *
     * @param name
     */
    JBossModule(final String name) {
        this.name = name
    }

    /**
     * A module name, which consists of one or more dot (.)-separated segments. Each segment must begin and end
     * with an alphanumeric or underscore (_), and may otherwise contain alphanumerics, underscores, and hyphens (-).
     *
     * @param name
     */
    void setModuleName(final String name) {
        assert name ==~ /[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?(\.[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?)*/,
                'Module Name must be: [a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?(\\.[a-zA-Z0-9_]([-a-zA-Z0-9_]*[a-zA-Z0-9_])?)*'
        this.moduleName = name
    }

    /**
     * A module version slot. A slot may consist of one or more alphanumerics, hyphens (-), underscores (_),
     * plus signs (+), asterisks (*), or dots (.).
     *
     * @param slot
     */
    void setSlot(final String slot) {
        assert slot ==~ /[-a-zA-Z0-9_+*.]+/,
                'Slot must be: [-a-zA-Z0-9_+*.]+'
        this.slot = slot
    }

    /**
     * Makes a module descriptor is an XML file which describes
     * the structure, content, dependencies, filtering, and other attributes of a module.
     *
     * @return a xml as string
     */
    String getModuleDescriptor() {
        def writer = new StringWriter()
        def xml = new  groovy.xml.MarkupBuilder(writer)

        // <?xml version="1.0" encoding="UTF-8"?>
        xml.mkp.xmlDeclaration(version: '1.0', encoding: 'utf-8')

        // <module xmlns="urn:jboss:module:1.1" name="org.jboss.msc">
        xml.module([xmlns: 'urn:jboss:module:1.1', name: this.moduleName] + ((this.slot in [null, '']) ? [:] : [slot: this.slot])) {

            // <main-class name="org.jboss.msc.Version"/>
            if (!(this.mainClass in [null, ''])) {
                'main-class'(name: this.mainClass)
            }

            //  <properties>
            //     <property name="my.property" value="foo"/>
            //  </properties>
            if (!this.properties.isEmpty()) {
                delegate.properties {
                    this.properties.each() {
                        property(name: it.key, value: it.value)
                    }
                }
            }

            //  <resources>
            //      <resource-root path="jboss-msc-1.0.1.GA.jar" name="bla-bla">
            //          <filter/>
            //      <resource-root/>
            //   </resources>
            if (!this.resources.isEmpty()) {
                delegate.resources {
                    this.resources.each() {
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
            if (!this.dependencies.isEmpty()) {
                delegate.dependencies {
                    this.dependencies.each() {dep->
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
