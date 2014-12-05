package com.zhurlik.descriptor

import com.zhurlik.Ver
import com.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

/**
 * To extract common things to write tags to main.xml based on different xsd files.
 *
 * @author zhurlik@gmail.com
 */
abstract class Xsd {

    abstract protected Ver getVersion()

    /**
     * Writes <?xml version="1.0" encoding="UTF-8"?>
     *
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeXmlDeclaration(final MarkupBuilder xml) {
        xml.mkp.xmlDeclaration(version: '1.0', encoding: 'utf-8')
    }

    /**
     * Writes <module-alias xmlns="urn:jboss:module:1.{1|3}" name="javax.json.api" target-name="org.glassfish.javax.json"/>
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeModuleAlias(final JBossModule jmodule, final MarkupBuilder xml) {
        assert jmodule.targetName != null, 'Target Name is null'

        def attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName]
        attrs += (jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot]
        attrs.put('target-name', jmodule.targetName)
        xml.'module-alias'(attrs)
    }

    /**
     * Writes a root element for an absent module.
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeModuleAbsent(final JBossModule jmodule, final MarkupBuilder xml) {
        assert jmodule.moduleName != null, 'Module Name is null'

        def attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName]
        attrs += (jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot]

        xml.'module-absent'(attrs)
    }

    /**
     * Writes <main-class name="org.jboss.msc.Version"/>
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeMainClass(final JBossModule jmodule, final MarkupBuilder xml) {
        if (!(jmodule.mainClass in [null, ''])) {
            xml.'main-class'(name: jmodule.mainClass)
        }
    }

    /**
     * Writes
     *  <exports>
     *      <exclude path="..."/>
     *  </exports>
     *
     *   Lists filter expressions to apply to the export filter of the local resources of this module
     *   (optional). By default, everything is exported. If filter expressions are provided, the default
     *   action is to accept all paths if no filters match.
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeExports(final JBossModule jmodule, final MarkupBuilder xml) {
        // exports
        if (!jmodule.exports.empty) {
            xml.'exports'() {

                // include
                if (jmodule.exports.include != null) {
                    if (jmodule.exports.include instanceof String || jmodule.exports.include.size() == 1) {
                        xml.'include'(path: jmodule.exports.include.toString())
                    } else if (jmodule.exports.include.size() > 1) {
                        xml.'include-set'() {
                            jmodule.exports.include.each() { xml.'path'(name: it) }
                        }
                    }
                }

                // exclude
                if (jmodule.exports.exclude != null) {
                    if (jmodule.exports.exclude instanceof String || jmodule.exports.exclude.size() == 1) {
                        xml.'exclude'(path: jmodule.exports.exclude.toString())
                    } else if (jmodule.exports.exclude.size() > 1) {
                        xml.'exclude-set'() {
                            jmodule.exports.exclude.each() { xml.'path'(name: it) }
                        }
                    }
                }
            }
        }
    }

    /**
     * Writes a root element for a filesystem module loader configuration.
     *  <configuration/>
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeConfiguration(final JBossModule jmodule, final MarkupBuilder xml) {
        if (jmodule.isModuleConfiguration()) {
            assert jmodule.defaultLoader != null, 'Default-Loader is null'

            xml.configuration([xmlns: 'urn:jboss:module:' + getVersion().number, 'default-loader': jmodule.defaultLoader]) {
                if (jmodule.loaders.empty) {
                    loader([name: jmodule.defaultLoader])
                }

                jmodule.loaders.each { l ->
                    if (l instanceof String) {
                        loader([name: l])
                    } else {
                        loader([name: l.name]) {
                            if (l['import'] != null) {
                                'import'(l['import'])
                            }

                            if (l['module-path'] != null) {
                                'module-path'([name: l['module-path']])
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Writes the following tags into a xml
     *  <properties>
     *     <property name="my.property" value="foo"/>
     *  </properties>
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeProperties(final JBossModule jmodule, final MarkupBuilder xml) {
        if (!jmodule.properties.isEmpty()) {
            xml.properties {
                jmodule.properties.findAll() { !(it.key in [null, '']) && !(it.value in [null, '']) }.each() {
                    xml.property(name: it.key, value: it.value)
                }
            }
        }
    }

    /**
     * Writes the following tags into a xml
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
     *  </resources>
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeResources(final JBossModule jmodule, final MarkupBuilder xml) {
        if (!jmodule.resources.isEmpty()) {
            xml.resources {
                jmodule.resources.each() { res ->
                    if (res instanceof String) {
                        xml.'resource-root'(path: res)
                        // next resource
                        return
                    }
                    if (res.filter != null) {
                        xml.'resource-root'(res.findAll() { it.key in ['name', 'path'] }) {
                            xml.filter() {
                                // include
                                if (res.filter.include != null) {
                                    if (res.filter.include instanceof String || res.filter.include.size() == 1) {
                                        xml.'include'(path: res.filter.include.toString())
                                    } else if (res.filter.include.size() > 1) {
                                        xml.'include-set'() {
                                            res.filter.include.each() {
                                                xml.'path'(name: it)
                                            }
                                        }
                                    }
                                }

                                //exclude
                                if (res.filter.exclude != null) {
                                    if (res.filter.exclude instanceof String || res.filter.exclude.size() == 1) {
                                        xml.'exclude'(path: res.filter.exclude.toString())
                                    } else if (res.filter.exclude.size() > 1) {
                                        xml.'exclude-set'() {
                                            res.filter.exclude.each() {
                                                xml.'path'(name: it)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        xml.'resource-root'(res.findAll() { it.key in ['name', 'path'] })
                    }
                }
            }
        }
    }

    /**
     * Writes the following tags into a xml
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
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeDependencies(final JBossModule jmodule, final MarkupBuilder xml) {
        if (!jmodule.dependencies.isEmpty()) {
            xml.dependencies {
                // modules
                writeModulesUnderDeps(jmodule, xml)

                // systems
                writeSystemsUnderDeps(jmodule, xml)
            }
        }
    }

    /**
     * To generate a tag for
     *      <xsd:element name="module" type="moduleDependencyType">
     *           <annotation xmlns="http://www.w3.org/2001/XMLSchema">
     *               <documentation>
     *                   A specified module dependency.
     *               </documentation>
     *           </annotation>
     *       </xsd:element>
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    def void writeModulesUnderDeps(final JBossModule jmodule, final MarkupBuilder xml) {
        // by default everything is module
        jmodule.dependencies.findAll({ !(it instanceof Map && it.type == 'system') }).each { dep ->
            // Attribute	Type	Required?	Description
            //name:	        string	    Yes	    The name of the module upon which this module depends.
            //slot:	        string	    No	    The version slot of the module upon which this module depends; defaults to "main".
            //export:	    boolean	    No	    Specify whether this dependency is re-exported by default; if not specified, defaults to "false".
            //services;	    enum	    No      Specify whether this dependency's services* are imported and/or exported. Possible values are "none", "import", or "export"; defaults to "none".
            //optional:	    boolean	    No	    Specify whether this dependency is optional; defaults to "false".
            if (dep instanceof String) {
                xml.module(name: dep)
                // next dependency
                return
            }

            if (dep.services != null) {
                assert dep.services in ['none', 'import', 'export']
            }

            if (dep.export != null) {
                assert dep.export.toString() in ['true', 'false']
            }

            if (dep.optional != null) {
                assert dep.optional.toString() in ['true', 'false']
            }

            if (dep.exports == null && dep.imports == null) {
                xml.module(dep.sort())
            } else {
                xml.module(dep.findAll() { el -> el.key in ['name', 'slot', 'export', 'optional', 'services'] }.sort()) {
                    // imports
                    if (dep.imports != null) {
                        xml.imports() {
                            // include
                            if (dep.imports.include != null) {
                                if (dep.imports.include instanceof String || dep.imports.include.size() == 1) {
                                    xml.'include'(path: dep.imports.include.toString())
                                } else if (dep.imports.include.size() > 1) {
                                    xml.'include-set'() {
                                        dep.imports.include.each() { xml.'path'(name: it) }
                                    }
                                }
                            }

                            // exclude
                            if (dep.imports.exclude != null) {
                                if (dep.imports.exclude instanceof String || dep.imports.exclude.size() == 1) {
                                    xml.'exclude'(path: dep.imports.exclude.toString())
                                } else if (dep.imports.exclude.size() > 1) {
                                    xml.'exclude-set'() {
                                        dep.imports.exclude.each() { xml.'path'(name: it) }
                                    }
                                }
                            }
                        }
                    }

                    // exports
                    if (dep.exports != null) {
                        xml.exports() {
                            // include
                            if (dep.exports.include != null) {
                                if (dep.exports.include instanceof String || dep.exports.include.size() == 1) {
                                    xml.'include'(path: dep.exports.include.toString())
                                } else if (dep.exports.include.size() > 1) {
                                    xml.'include-set'() {
                                        dep.exports.include.each() { xml.'path'(name: it) }
                                    }
                                }
                            }

                            // exclude
                            if (dep.exports.exclude != null) {
                                if (dep.exports.exclude instanceof String || dep.exports.exclude.size() == 1) {
                                    xml.'exclude'(path: dep.exports.exclude.toString())
                                } else if (dep.exports.exclude.size() > 1) {
                                    xml.'exclude-set'() {
                                        dep.exports.exclude.each() { xml.'path'(name: it) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *  To generate a tag for
     *      <xsd:element name="system" type="systemDependencyType">
     *            <annotation xmlns="http://www.w3.org/2001/XMLSchema">
     *               <documentation>
     *                   A dependency on the system (or embedding) class loader.
     *               </documentation>
     *           </annotation>
     *       </xsd:element>
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    def void writeSystemsUnderDeps(final JBossModule jmodule, final MarkupBuilder xml) {
        jmodule.dependencies.findAll({ it instanceof Map && it.type == 'system' }).each {dep ->

            // Specifies whether this module dependency is re-exported by default (default is "false")
            xml.system((dep.export in [null, ''] || !dep.export) ? null : [export: dep.export]){

                // paths: the list of paths which are applicable for this system dependency.
                if (dep.paths instanceof String) {
                    xml.paths {
                        xml.path([name: dep.paths])
                    }
                } else if (dep.paths instanceof List) {
                    xml.paths {
                        dep.paths.each {
                            xml.path([name: it])
                        }
                    }
                }

                // exports: a filter used to restrict what packages or directories from this dependency are re-exported by this module
                if (!(dep.exports in ['', null])) {
                    xml.exports() {
                        // include
                        if (dep.exports.include != null) {
                            if (dep.exports.include instanceof String || dep.exports.include.size() == 1) {
                                xml.'include'(path: dep.exports.include.toString())
                            } else if (dep.exports.include.size() > 1) {
                                xml.'include-set'() {
                                    dep.exports.include.each() { xml.'path'(name: it) }
                                }
                            }
                        }

                        // exclude
                        if (dep.exports.exclude != null) {
                            if (dep.exports.exclude instanceof String || dep.exports.exclude.size() == 1) {
                                xml.'exclude'(path: dep.exports.exclude.toString())
                            } else if (dep.exports.exclude.size() > 1) {
                                xml.'exclude-set'() {
                                    dep.exports.exclude.each() { xml.'path'(name: it) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
