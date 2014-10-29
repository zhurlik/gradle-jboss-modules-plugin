package com.zhurlik.descriptor

import com.zhurlik.JBossModule

import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI

/**
 * Core class to generate and to check xml descriptors for JBoss Modules
 *
 * @author zhurlik@gmail.com
 */

abstract class AbstractBuilder<T extends JBossModule> {

    static final factory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI)

    abstract String getXmlDescriptor(final T module)

    abstract StreamSource getXsd()

    /**
     * To validate a xml descriptors.
     *
     * @param xml xml descriptor
     * @return true if valid
     */
    boolean isValid(final String xml){
        try {
            def schema = factory.newSchema(getXsd())
            def validator = schema.newValidator()
            validator.validate(new StreamSource(new StringReader(xml)))
            return true
        } catch (all) {
            println '>> ERROR: ' + all
            return false
        }
    }

    enum Ver {
        V_1_0('1.0', 'xsd/module-1_0.xsd'), V_1_1('1.1', 'xsd/module-1_1.xsd'), V_1_2('1.2', 'xsd/module-1_2.xsd'), V_1_3('1.3', 'xsd/module-1_3.xsd');

        def String version
        def String xsd

        Ver(final String ver, final String xsd) {
            this.version = ver
            this.xsd = xsd
        }
    }
}


