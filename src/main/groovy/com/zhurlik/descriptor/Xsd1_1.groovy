package com.zhurlik.descriptor

import com.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import javax.xml.transform.stream.StreamSource

import static com.zhurlik.Ver.V_1_1
import static java.io.File.separator
import static java.io.File.separator

/**
 * Generates a xml descriptor for JBoss Module ver.1.1
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_1.xsd
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
class Xsd1_1 extends AbstractBuilder<JBossModule> {

    @Override
    String getXmlDescriptor(final JBossModule jmodule) {
        assert jmodule != null, 'JBossModule is null'
        assert jmodule.moduleName != null, 'Module name is null'

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        // <?xml version="1.0" encoding="UTF-8"?>
        xml.mkp.xmlDeclaration(version: '1.0', encoding: 'utf-8')

        // <module xmlns="urn:jboss:module:1.1" name="org.jboss.msc">
        xml.module([xmlns: 'urn:jboss:module:' + V_1_1.number, name: jmodule.moduleName] + ((jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot])) {

            // <main-class name="org.jboss.msc.Version"/>
            if (!(jmodule.mainClass in [null, ''])) {
                'main-class'(name: jmodule.mainClass)
            }

            //  <properties>
            //     <property name="my.property" value="foo"/>
            //  </properties>
            if (!jmodule.properties.isEmpty()) {
                delegate.properties {
                    jmodule.properties.findAll() { !(it.key in [null, '']) && !(it.value in [null, '']) }.each() {
                        property(name: it.key, value: it.value)
                    }
                }
            }

            //  <resources>
            //      <resource-root path="jboss-msc-1.0.1.GA.jar" name="bla-bla">
            //          <filter>
            //             <include path=''/>
            //             ...
            //             <exclude-set>
            //               ...
            //             <exclude-set/>
            //          <filter>
            //      <resource-root/>
            //   </resources>
            if (!jmodule.resources.isEmpty()) {
                delegate.resources {
                    jmodule.resources.each() { res ->
                        if (res instanceof String) {
                            'resource-root'(path: res)
                            // next resource
                            return
                        }
                        if (res.filter != null) {
                            'resource-root'(res.findAll() { it.key in ['name', 'path'] }) {
                                delegate.filter() {
                                    // include
                                    if (res.filter.include != null) {
                                        if (res.filter.include instanceof String || res.filter.include.size() == 1) {
                                            'include'(path: res.filter.include.toString())
                                        } else if (res.filter.include.size() > 1) {
                                            'include-set'() {
                                                res.filter.include.each() {
                                                    'path'(name: it)
                                                }
                                            }
                                        }
                                    }

                                    //exclude
                                    if (res.filter.exclude != null) {
                                        if (res.filter.exclude instanceof String || res.filter.exclude.size() == 1) {
                                            'exclude'(path: res.filter.exclude.toString())
                                        } else if (res.filter.exclude.size() > 1) {
                                            'exclude-set'() {
                                                res.filter.exclude.each() {
                                                    'path'(name: it)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            'resource-root'(res.findAll() { it.key in ['name', 'path'] })
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
                        if (dep instanceof String) {
                            delegate.module(name: dep)
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
                            delegate.module(dep)
                        } else {
                            delegate.module(dep.findAll() { el -> el.key in ['name', 'slot', 'export', 'optional', 'services'] }) {
                                // imports
                                if (dep.imports != null) {
                                    delegate.imports() {
                                        // include
                                        if (dep.imports.include != null) {
                                            if (dep.imports.include instanceof String || dep.imports.include.size() == 1) {
                                                'include'(path: dep.imports.include.toString())
                                            } else if (dep.imports.include.size() > 1) {
                                                'include-set'() {
                                                    dep.imports.include.each() { 'path'(name: it) }
                                                }
                                            }
                                        }

                                        // exclude
                                        if (dep.imports.exclude != null) {
                                            if (dep.imports.exclude instanceof String || dep.imports.exclude.size() == 1) {
                                                'exclude'(path: dep.imports.exclude.toString())
                                            } else if (dep.imports.exclude.size() > 1) {
                                                'exclude-set'() {
                                                    dep.imports.exclude.each() { 'path'(name: it) }
                                                }
                                            }
                                        }
                                    }
                                }

                                // exports
                                if (dep.exports != null) {
                                    delegate.exports() {
                                        // include
                                        if (dep.exports.include != null) {
                                            if (dep.exports.include instanceof String || dep.exports.include.size() == 1) {
                                                'include'(path: dep.exports.include.toString())
                                            } else if (dep.exports.include.size() > 1) {
                                                'include-set'() {
                                                    dep.exports.include.each() { 'path'(name: it) }
                                                }
                                            }
                                        }

                                        // exclude
                                        if (dep.exports.exclude != null) {
                                            if (dep.exports.exclude instanceof String || dep.exports.exclude.size() == 1) {
                                                'exclude'(path: dep.exports.exclude.toString())
                                            } else if (dep.exports.exclude.size() > 1) {
                                                'exclude-set'() {
                                                    dep.exports.exclude.each() { 'path'(name: it) }
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

    @Override
    StreamSource getXsd() {
        return new StreamSource(getClass().classLoader.getResourceAsStream(V_1_1.xsd))
    }

    @Override
    JBossModule makeModule(final String txt) {
        //result
        JBossModule jbModule = new JBossModule('empty')

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
            it.property.each() {p->
                jbModule.properties.put(p.@name.toString(), p.@value.toString())
            }
        }

        xml.resources.each() {
            it.'resource-root'.each() {r->

                def complexEl = [:]

                if (r.attributes().size() == 1) {
                    complexEl.path = r.@path.toString()
                } else {
                    complexEl.name = r.@name.toString()
                    complexEl.path =  r.@path.toString()
                }

                r.filter.each() {f->
                    def filter = [:]
                    f.include.each() {
                        filter.include = f.include.@path.toString()
                    }
                    f.exclude.each() {
                        filter.exclude = f.exclude.@path.toString()
                    }
                    if (f.'exclude-set'.children().size() > 0) {
                        filter.exclude = f.'exclude-set'.path.collect(){it.@name.toString()}
                    }
                    if (f.'include-set'.children().size() > 0){
                        filter.include = f.'include-set'.path.collect(){it.@name.toString()}
                    }
                    complexEl.filter = filter
                }

                jbModule.resources.add(complexEl)
            }
        }

        xml.dependencies.each() {
            it.module.each() {d->
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
                        map.exclude =it.'exclude-set'.path.collect(){it.@name.toString()}
                    }
                    if (it.'include-set'.children().size() > 0) {
                        map.include = it.'include-set'.path.collect(){it.@name.toString()}
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
                        map.exclude = it.'exclude-set'.path.collect(){it.@name.toString()}
                    }
                    if (it.'include-set'.children().size() > 0) {
                        map.include = it.'include-set'.path.collect(){it.@name.toString()}
                    }
                    dep.exports = map
                }

                jbModule.dependencies.add(dep)
            }
        }

        log.debug '>> Module: \'{}\' has been created', jbModule.name
        return jbModule
    }

    @Override
    String getPath(final JBossModule jbModule) {
        return [jbModule.moduleName.replaceAll('\\.', separator), ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot)].join(separator)
    }
}
