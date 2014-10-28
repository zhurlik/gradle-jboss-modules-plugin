package com.zhurlik

import com.zhurlik.descriptor.BuilderFactory
import com.zhurlik.descriptor.AbstractBuilder

import static com.zhurlik.descriptor.AbstractBuilder.Ver.V_1_1

/**
 * To make JBoss Module.
 * https://docs.jboss.org/author/display/MODULES/Home
 *
 * @author zhurlik@gmail.com
 */
class JBossModule {
    def String name, moduleName, slot, mainClass
    def properties = [:]
    def resources = []
    def dependencies = []
    def exports
    def AbstractBuilder.Ver ver = V_1_1

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
        def builder = BuilderFactory.getBuilder(this.ver)
        return builder.getXmlDescriptor(this)
    }

    boolean isValid(){
        def builder = BuilderFactory.getBuilder(this.ver)
        return builder.isValid(builder.getXmlDescriptor(this))
    }
}
