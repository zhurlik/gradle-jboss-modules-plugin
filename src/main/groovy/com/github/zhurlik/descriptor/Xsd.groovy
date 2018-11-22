package com.github.zhurlik.descriptor

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

import static com.github.zhurlik.Ver.*

/**
 * This class contains main methods to generate and to write tags of a xml descriptor using different xsd files.
 * <p> See
 *     <ul>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_0.xsd">module-1_0.xsd</a></li>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_1.xsd">module-1_1.xsd</a></li>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_2.xsd">module-1_2.xsd</a></li>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_3.xsd">module-1_3.xsd</a></li>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_5.xsd">module-1_5.xsd</a></li>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_6.xsd">module-1_6.xsd</a></li>
 *     </ul>
 * </p>
 *
 * @author zhurlik@gmail.com
 */
abstract class Xsd {

    abstract protected Ver getVersion()

    /**
     * Writes the module declaration type; contains dependencies, resources, and the main class specification.
     * <p>
     * Root element for a module declaration.
     * </p>
     * See <xsd:element name="module" type="moduleType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    abstract protected void writeModuleType(final JBossModule jmodule, final MarkupBuilder xml)

    /**
     * Writes <?xml version="1.0" encoding="UTF-8"?>
     *
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeXmlDeclaration(final MarkupBuilder xml) {
        xml.mkp.xmlDeclaration(version: '1.0', encoding: 'utf-8')
    }

    /**
     * Writes a module alias type, which defines the target for a module alias.
     * <p>Root element for a module alias declaration.</p>
     * See <xsd:element name="module-alias" type="moduleAliasType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeModuleAliasType(final JBossModule jmodule, final MarkupBuilder xml) {
        // <module-alias xmlns="urn:jboss:module:1.{1|3}" name="javax.json.api" target-name="org.glassfish.javax.json"/>
        assert jmodule.targetName != null, 'Target Name is null'

        def attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName]
        attrs += (jmodule.slot in [null, ''] || version in [V_1_7]) ? [:] : [slot: jmodule.slot]
        attrs.put('target-name', jmodule.targetName)
        xml.'module-alias'(attrs)
    }

    /**
     * Writes an explicitly absent module.
     * <p>Root element for an absent module.</p>
     * See <xsd:element name="module-absent" type="moduleAbsentType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeModuleAbsentType(final JBossModule jmodule, final MarkupBuilder xml) {
        assert jmodule.moduleName != null, 'Module Name is null'

        def attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName]
        attrs += (jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot]

        xml.'module-absent'(attrs)
    }

    /**
     *  Specifies the main class of this module; used to run the module from the command-line (optional).
     *  <br/>
     *  See <xsd:element name="main-class" type="classNameType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
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
     * <p>
     *   Lists filter expressions to apply to the export filter of the local resources of this module
     *   (optional). By default, everything is exported. If filter expressions are provided, the default
     *   action is to accept all paths if no filters match.
     * </p>
     * See <xsd:element name="exports" type="filterType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeExports(final JBossModule jmodule, final MarkupBuilder xml) {
        // exports
        if (!jmodule.exports.empty) {
            xml.'exports'() {

                // include
                if (jmodule.exports.include != null) {
                    if (jmodule.exports.include instanceof String || jmodule.exports.include instanceof GString || jmodule.exports.include.size() == 1) {
                        xml.'include'(path: jmodule.exports.include.toString())
                    } else if (jmodule.exports.include.size() > 1) {
                        xml.'include-set'() {
                            jmodule.exports.include.each() { xml.'path'(name: it) }
                        }
                    }
                }

                // exclude
                if (jmodule.exports.exclude != null) {
                    if (jmodule.exports.exclude instanceof String || jmodule.exports.exclude instanceof GString || jmodule.exports.exclude.size() == 1) {
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
     * Writes a configuration for the default module loader.
     * <p>Root element for a filesystem module loader configuration.</p>
     * See <xsd:element name="configuration" type="configurationType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeConfigurationType(final JBossModule jmodule, final MarkupBuilder xml) {
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
     * Writes lists the user-defined properties to be associated with this module (optional).
     *  <properties>
     *     <property name="my.property" value="foo"/>
     *  </properties>
     *
     *  <br/>
     *  See <xsd:element name="properties" type="propertyListType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
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
     * Writes a list of permissions that this module requires.
     *  <permissions>
     *    <grant .../>
     *  </permissions>
     * <p>Lists the requested permission set for this module. If the requested permissions cannot be assigned, the module cannot be loaded.</p>
     *
     * See <xsd:element name="permissions" type="permissionsType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writePermissionsType(final JBossModule jmodule, final MarkupBuilder xml) {
        if (!jmodule.permissions.isEmpty()) {
            xml.permissions {
                jmodule.permissions.each {
                    if (it instanceof String) {
                        grant([permission: it])
                    } else {
                        grant(it.sort())
                    }
                }
            }
        }
    }

    /**
     *  Writes a resource root within a deployment.
     *
     *  <p>A resource root within this deployment.</p>
     *
     *  See <xsd:element name="resource-root" type="resourceType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeResourceType(final JBossModule jmodule, final MarkupBuilder xml) {
        jmodule.resources.findAll({
            !((it instanceof Map) && (it.type in ['artifact', 'native-artifact']))
        }).each() { res ->
            if (res instanceof String || res instanceof GString) {
                xml.'resource-root'(path: res.toString())
                // next resource
                return
            }
            if (res.filter != null) {
                xml.'resource-root'(res.findAll() { it.key in ['name', 'path'] }) {
                    xml.filter() {
                        // include
                        if (res.filter.include != null) {
                            if (res.filter.include instanceof String || res.filter.include instanceof GString || res.filter.include.size() == 1) {
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
                            if (res.filter.exclude instanceof String || res.filter.exclude instanceof GString || res.filter.exclude.size() == 1) {
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
    protected void writeResourcesType(final JBossModule jmodule, final MarkupBuilder xml) {
        if (!jmodule.resources.isEmpty()) {
            xml.resources {
                // <resource-root>
                writeResourceType(jmodule, xml)

                if (jmodule.ver in [V_1_3, V_1_5, V_1_6, V_1_7]) {
                    // either <artifact> or <native-artifact>
                    writeArtifacts(jmodule, xml)
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
    protected void writeDependenciesType(final JBossModule jmodule, final MarkupBuilder xml) {
        if (!jmodule.dependencies.isEmpty()) {
            xml.dependencies {
                // modules
                writeModuleDependencyType(jmodule, xml)

                // systems
                writeSystemDependencyType(jmodule, xml)
            }
        }
    }

    /**
     * Writes a specified module dependency.
     *      <xsd:element name="module" type="moduleDependencyType">
     *           <annotation xmlns="http://www.w3.org/2001/XMLSchema">
     *               <documentation>
     *                   A specified module dependency.
     *               </documentation>
     *           </annotation>
     *       </xsd:element>
     * <p>A single module dependency expression.</p>
     * See <xsd:complexType name="moduleDependencyType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeModuleDependencyType(final JBossModule jmodule, final MarkupBuilder xml) {
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
                xml.module(dep.findAll() { el -> el.key in ['name', 'export', 'optional', 'services'] + (!(version in [V_1_7]) ? ['slot'] : [])}.sort()) {
                    // imports
                    if (dep.imports != null) {
                        xml.imports() {
                            // include
                            if (dep.imports.include != null) {
                                if (dep.imports.include instanceof String || dep.imports.include instanceof GString || dep.imports.include.size() == 1) {
                                    xml.'include'(path: dep.imports.include.toString())
                                } else if (dep.imports.include.size() > 1) {
                                    xml.'include-set'() {
                                        dep.imports.include.each() { xml.'path'(name: it) }
                                    }
                                }
                            }

                            // exclude
                            if (dep.imports.exclude != null) {
                                if (dep.imports.exclude instanceof String || dep.imports.exclude instanceof GString || dep.imports.exclude.size() == 1) {
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
                                if (dep.exports.include instanceof String || dep.exports.include instanceof GString || dep.exports.include.size() == 1) {
                                    xml.'include'(path: dep.exports.include.toString())
                                } else if (dep.exports.include.size() > 1) {
                                    xml.'include-set'() {
                                        dep.exports.include.each() { xml.'path'(name: it) }
                                    }
                                }
                            }

                            // exclude
                            if (dep.exports.exclude != null) {
                                if (dep.exports.exclude instanceof String || dep.exports.exclude instanceof GString || dep.exports.exclude.size() == 1) {
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
     *  Writes the list of paths which are applicable for this system dependency.
     *      <xsd:element name="system" type="systemDependencyType">
     *            <annotation xmlns="http://www.w3.org/2001/XMLSchema">
     *               <documentation>
     *                   A dependency on the system (or embedding) class loader.
     *               </documentation>
     *           </annotation>
     *       </xsd:element>
     * <p>A dependency on the system (or embedding) class loader.</p>
     *
     * See <xsd:element name="system" type="systemDependencyType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeSystemDependencyType(final JBossModule jmodule, final MarkupBuilder xml) {
        jmodule.dependencies.findAll({ it instanceof Map && it.type == 'system' }).each { dep ->

            // Specifies whether this module dependency is re-exported by default (default is "false")
            xml.system((dep.export in [null, ''] || !dep.export) ? null : [export: dep.export]) {

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
                            if (dep.exports.include instanceof String || dep.exports.include instanceof GString || dep.exports.include.size() == 1) {
                                xml.'include'(path: dep.exports.include.toString())
                            } else if (dep.exports.include.size() > 1) {
                                xml.'include-set'() {
                                    dep.exports.include.each() { xml.'path'(name: it) }
                                }
                            }
                        }

                        // exclude
                        if (dep.exports.exclude != null) {
                            if (dep.exports.exclude instanceof String || dep.exports.exclude instanceof GString || dep.exports.exclude.size() == 1) {
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

    /**
     *  Writes a maven artifact within this deployment.
     *
     *  <p>A maven native artifact within this deployment. This is a jar that contains a lib/ directory
     *  with corresponding platform directories and binaries. This element will cause the jar to
     *  be unzipped within the artifact's local repository directory.</p>
     *
     *  See either <xsd:element name="artifact" type="artifactType"> or <xsd:element name="native-artifact" type="artifactType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeArtifacts(final JBossModule jmodule, final MarkupBuilder xml) {
        if (!(jmodule.ver in [V_1_5, V_1_6, V_1_7])) {
            // do nothing
            return
        }

        jmodule.resources.findAll({
            ((it instanceof Map) && (it.type in ['artifact', 'native-artifact']))
        }).each() { res ->
            // URI that points to the maven artifact "group:artifact:version[:classifier]"
            addArtifact(xml, (Map) res)
        }
    }

    /**
     * For adding either 'artifact' or 'native-artifact'.
     * @param xml
     * @param map
     */
    static void addArtifact(final MarkupBuilder xml, final Map map){
        xml."${map.type}"(name: map.name) {
            if (map.filter != null) {
                xml.filter() {
                    // include
                    if (map.filter.include != null) {
                        if (map.filter.include instanceof String || map.filter.include instanceof GString || map.filter.include.size() == 1) {
                            xml.'include'(path: map.filter.include.toString())
                        } else if (map.filter.include.size() > 1) {
                            xml.'include-set'() {
                                map.filter.include.each() {
                                    xml.'path'(name: it)
                                }
                            }
                        }
                    }

                    //exclude
                    if (map.filter.exclude != null) {
                        if (map.filter.exclude instanceof String || map.filter.exclude instanceof GString || map.filter.exclude.size() == 1) {
                            xml.'exclude'(path: map.filter.exclude.toString())
                        } else if (map.filter.exclude.size() > 1) {
                            xml.'exclude-set'() {
                                map.filter.exclude.each() {
                                    xml.'path'(name: it)
                                }
                            }
                        }
                    }
                }
            }

            if (map.conditions != null) {
                // TODO: is it possible to have a list of conditions?
                xml.'conditions'() {
                    if (map.conditions.'property-equal' != null) {
                        xml.'property-equal'(name: map.conditions.'property-equal'.name.toString(), value: map.conditions.'property-equal'.value.toString())
                    }
                    if (map.conditions.'property-not-equal' != null) {
                        xml.'property-not-equal'(name: map.conditions.'property-not-equal'.name.toString(), value: map.conditions.'property-not-equal'.value.toString())
                    }
                }
            }
        }
    }
}
