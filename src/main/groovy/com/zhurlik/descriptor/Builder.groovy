package com.zhurlik.descriptor

import com.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j

import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI

/**
 * Core class to generate and to check xml descriptors for JBoss Modules
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
abstract class Builder<T extends JBossModule>  extends Xsd {

    static final factory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI)

    abstract String getXmlDescriptor(final T module)

    abstract StreamSource getXsd()

    /**
     * Returns a path where will be stored JBoss Module under server.
     *
     * @return string like org/apache/log4j
     */
    abstract String getPath(JBossModule module)

    JBossModule makeModule(final String txt) {
        //result
        JBossModule jbModule = new JBossModule('empty')
        jbModule.ver = getVersion()

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

        // exports
        xml.exports.each() {
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
            jbModule.exports = map
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
     * To validate a xml descriptors.
     *
     * @param xml xml descriptor
     * @return true if valid
     */
    boolean isValid(final String xml) {
        try {
            def schema = factory.newSchema(getXsd())
            def validator = schema.newValidator()
            validator.validate(new StreamSource(new StringReader(xml)))
            return true
        } catch (all) {
            log.error '>> ERROR: ' + all
            return false
        }
    }
}


