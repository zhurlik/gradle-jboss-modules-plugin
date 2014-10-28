package com.zhurlik.descriptor

import com.zhurlik.JBossModule

import javax.xml.transform.stream.StreamSource

import static com.zhurlik.descriptor.AbstractBuilder.Ver.V_1_3

/**
 * Generates a xml descriptor for JBoss Module ver.1.3
 * https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_3.xsd
 *
 * @author zhurlik@gmail.com
 */
class Xsd1_3 extends AbstractBuilder<JBossModule> {

    @Override
    String getXmlDescriptor(JBossModule module) {
        throw new RuntimeException("Not supported yet")
    }

    @Override
    StreamSource getXsd() {
        return new StreamSource(getClass().classLoader.getResourceAsStream(V_1_3.xsd))
    }
}
