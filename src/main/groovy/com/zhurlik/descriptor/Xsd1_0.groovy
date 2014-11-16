package com.zhurlik.descriptor

import com.zhurlik.extension.JBossModule

import javax.xml.transform.stream.StreamSource

import static com.zhurlik.Ver.V_1_0

/**
 * Generates a xml descriptor for JBoss Module ver.1.0
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_0.xsd
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_0 extends AbstractBuilder<JBossModule> {

    @Override
    String getXmlDescriptor(final JBossModule module) {
        throw new UnsupportedOperationException("Not implemented yet")
    }

    @Override
    StreamSource getXsd() {
        return new StreamSource(getClass().classLoader.getResourceAsStream(V_1_0.xsd))
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
