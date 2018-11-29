package com.github.zhurlik.descriptor

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

import static com.github.zhurlik.Ver.V_1_3
import static com.github.zhurlik.Ver.V_1_5
import static com.github.zhurlik.Ver.V_1_6
import static com.github.zhurlik.Ver.V_1_7
import static com.github.zhurlik.Ver.V_1_8
import static com.github.zhurlik.Ver.V_1_9

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
                xml.'resource-root'(res.findAll() {
                    it.key in ['path'] + (!(version in [V_1_8, V_1_9]) ? ['name'] : [])
                })
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

                if (jmodule.ver in [V_1_3, V_1_5, V_1_6, V_1_7, V_1_8, V_1_9]) {
                    // either <artifact> or <native-artifact>
                    writeArtifacts(jmodule, xml)
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
        if (!(jmodule.ver in [V_1_5, V_1_6, V_1_7, V_1_8, V_1_9])) {
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
