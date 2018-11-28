package com.github.zhurlik.descriptor.parser

import groovy.xml.MarkupBuilder

import java.util.function.Consumer

/**
 *  Writes <?xml version="1.0" encoding="UTF-8"?>
 *
 * @author zhurlik@gmail.com
 */
class XmlDeclarationTag {

    /**
     * Writes <?xml version="1.0" encoding="UTF-8"?>
     *
     * @return a function for updating a xml
     */
    static Consumer<MarkupBuilder> write() {
        return { final MarkupBuilder xml ->
            xml.mkp.xmlDeclaration(version: '1.0', encoding: 'utf-8')
        }
    }
}
