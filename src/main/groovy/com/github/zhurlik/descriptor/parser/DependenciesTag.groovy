package com.github.zhurlik.descriptor.parser

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild

import java.util.function.Consumer

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
                parseModules(it, jbModule)

                // systems
                if (!(version in [Ver.V_1_8.number])) {
                    parseSystems(it, jbModule)
                }
            }

        }
    }

    /**
     * To parse <xsd:complexType name="systemDependencyType">
     *
     * @param it a little bit of xml
     * @param jbModule
     * @return
     */
    private static parseSystems(final NodeChild it, final JBossModule jbModule) {
        it.system.each() { s ->
            def system = [:]

            if (s.@export.toBoolean() == true) {
                system.export = true
            }

            // paths
            def paths = []
            system.type = 'system'

            s.paths.each {
                paths += it.path.collect({ it.@name.text() })
            }

            if (!paths.empty) {
                system.paths = paths
            }

            // exports
            s.exports.each() {
                def map = [:]
                it.include.each() {
                    map.include = it.@path.text()
                }
                it.exclude.each() {
                    map.exclude = it.@path.text()
                }
                if (it.'exclude-set'.children().size() > 0) {
                    map.exclude = it.'exclude-set'.path.collect() { it.@name.text() }
                }
                if (it.'include-set'.children().size() > 0) {
                    map.include = it.'include-set'.path.collect() { it.@name.text() }
                }
                system.exports = map
            }

            jbModule.dependencies.add(system)
        }
    }

    /**
     * To parse <xsd:complexType name="moduleDependencyType">
     *
     * @param it a little bit of xml
     * @param jbModule
     * @return
     */
    private static void parseModules(final NodeChild it, final JBossModule jbModule) {
        it.module.each() { d ->
            def dep = [:]
            if (d.attributes().size() == 1) {
                dep.name = d.@name.toString()
            } else {
                d.attributes().each() {
                    dep[it.key] = it.value
                }
            }

            // imports
            d.imports.each() {
                def map = [:]
                it.include.each() {
                    map.include = it.@path.text()
                }
                it.exclude.each() {
                    map.exclude = it.@path.text()
                }
                if (it.'exclude-set'.children().size() > 0) {
                    map.exclude = it.'exclude-set'.path.collect() { it.@name.text() }
                }
                if (it.'include-set'.children().size() > 0) {
                    map.include = it.'include-set'.path.collect() { it.@name.text() }
                }
                dep.imports = map
            }

            // exports
            d.exports.each() {
                def map = [:]
                it.include.each() {
                    map.include = it.@path.text()
                }
                it.exclude.each() {
                    map.exclude = it.@path.text()
                }
                if (it.'exclude-set'.children().size() > 0) {
                    map.exclude = it.'exclude-set'.path.collect() { it.@name.text() }
                }
                if (it.'include-set'.children().size() > 0) {
                    map.include = it.'include-set'.path.collect() { it.@name.text() }
                }
                dep.exports = map
            }

            // properties since 1.9
            if (jbModule.ver == Ver.V_1_9 && !d.properties.isEmpty()) {
                def props = [:]
                d.properties.property.each {
                    props.put(it.@name.text(), it.@value.text())
                }
                dep.properties = props
            }

            jbModule.dependencies.add(dep)
        }
    }
}
