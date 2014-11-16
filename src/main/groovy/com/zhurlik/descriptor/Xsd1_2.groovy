package com.zhurlik.descriptor

import com.zhurlik.extension.JBossModule

import javax.xml.transform.stream.StreamSource

import static com.zhurlik.Ver.V_1_2

/**
 * Generates a xml descriptor for JBoss Module ver.1.2
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_2.xsd
 *
 * @author zhurlik@gmail.com
 */class Xsd1_2 extends AbstractBuilder<JBossModule> {

    @Override
    String getXmlDescriptor(JBossModule module) {
        throw new RuntimeException("Not supported yet")
    }

    @Override
    StreamSource getXsd() {
        return new StreamSource(getClass().classLoader.getResourceAsStream(V_1_2.xsd))
    }

    @Override
    String getPath(JBossModule module) {
        return null
    }

    @Override
    JBossModule makeModule(final String txt) {
        throw new UnsupportedOperationException("Not implemented yet")
    }
}
