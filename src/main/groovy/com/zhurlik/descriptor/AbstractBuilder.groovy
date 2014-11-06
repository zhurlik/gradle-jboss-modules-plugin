package com.zhurlik.descriptor

import com.zhurlik.extension.JBossModule

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

    abstract JBossModule makeModule(final String txt)

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
}


