package com.zhurlik.descriptor
import com.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import javax.xml.transform.stream.StreamSource

import static com.zhurlik.Ver.V_1_3
import static java.io.File.separator
/**
 * Generates a xml descriptor for JBoss Module ver.1.3
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_3.xsd
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class Xsd1_3 extends Builder<JBossModule> {

    @Override
    String getXmlDescriptor(JBossModule jmodule) {
        assert jmodule != null, 'JBossModule is null'
        assert jmodule.moduleName != null, 'Module name is null'

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        writeXmlDeclaration(xml)

        if (jmodule.isModuleAlias()) {
            writeModuleAlias(jmodule, xml)
        } else {
            // <module xmlns="urn:jboss:module:1.3" name="org.jboss.msc">
            final attrs = [xmlns: 'urn:jboss:module:' + V_1_3.number, name: jmodule.moduleName] + ((jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot])
            xml.module(attrs) {
                writeMainClass(jmodule, xml)
                writeProperties(jmodule, xml)
                writeResources(jmodule, xml)
                writeDependencies(jmodule, xml)
            }
        }

        return writer.toString()
    }

    @Override
    StreamSource getXsd() {
        return new StreamSource(getClass().classLoader.getResourceAsStream(V_1_3.xsd))
    }

    @Override
    String getPath(JBossModule jbModule) {
        return ['modules', 'system', 'layers', 'base', jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot)].join(separator)
    }

    @Override
    JBossModule makeModule(final String txt) {
        //result
        JBossModule jbModule = new JBossModule('empty')
        jbModule.ver = V_1_3

        def xml = new XmlSlurper().parseText(txt)

        xml.attributes().each() {
            switch (it.key) {
                case 'slot': jbModule.slot = it.value
                    break
                case 'name': jbModule.moduleName = it.value
                    jbModule.name = it.value
                    break
            }
        }

        jbModule.mainClass = xml.'main-class'.@name

        xml.properties.each() {
            it.property.each() { p ->
                jbModule.properties.put(p.@name.toString(), p.@value.toString())
            }
        }

        xml.resources.each() {
            it.'resource-root'.each() { r ->

                def complexEl = [:]

                if (r.attributes().size() == 1) {
                    complexEl.path = r.@path.toString()
                } else {
                    complexEl.name = r.@name.toString()
                    complexEl.path = r.@path.toString()
                }

                r.filter.each() { f ->
                    def filter = [:]
                    f.include.each() {
                        filter.include = f.include.@path.toString()
                    }
                    f.exclude.each() {
                        filter.exclude = f.exclude.@path.toString()
                    }
                    if (f.'exclude-set'.children().size() > 0) {
                        filter.exclude = f.'exclude-set'.path.collect() { it.@name.toString() }
                    }
                    if (f.'include-set'.children().size() > 0) {
                        filter.include = f.'include-set'.path.collect() { it.@name.toString() }
                    }
                    complexEl.filter = filter
                }

                jbModule.resources.add(complexEl)
            }
        }

        xml.dependencies.each() {
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
                        map.include = it.@path.toString()
                    }
                    it.exclude.each() {
                        map.exclude = it.@path.toString()
                    }
                    if (it.'exclude-set'.children().size() > 0) {
                        map.exclude = it.'exclude-set'.path.collect() { it.@name.toString() }
                    }
                    if (it.'include-set'.children().size() > 0) {
                        map.include = it.'include-set'.path.collect() { it.@name.toString() }
                    }
                    dep.imports = map
                }

                // exports
                d.exports.each() {
                    def map = [:]
                    it.include.each() {
                        map.include = it.@path.toString()
                    }
                    it.exclude.each() {
                        map.exclude = it.@path.toString()
                    }
                    if (it.'exclude-set'.children().size() > 0) {
                        map.exclude = it.'exclude-set'.path.collect() { it.@name.toString() }
                    }
                    if (it.'include-set'.children().size() > 0) {
                        map.include = it.'include-set'.path.collect() { it.@name.toString() }
                    }
                    dep.exports = map
                }

                jbModule.dependencies.add(dep)
            }
        }

        log.debug '>> Module: \'{}\' has been created', jbModule.name
        return jbModule
    }

    /**
     * Writes <module-alias xmlns="urn:jboss:module:1.3" name="javax.json.api" target-name="org.glassfish.javax.json"/>
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference for xml
     */
    protected void writeModuleAlias(final JBossModule jmodule, final MarkupBuilder xml) {
        assert jmodule.targetName != null, 'Target Name is null'

        def attrs = [xmlns: 'urn:jboss:module:' + V_1_3.number, name: jmodule.moduleName]
        attrs += (jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot]
        attrs.put('target-name', jmodule.targetName)
        xml.'module-alias'(attrs)
    }
}
