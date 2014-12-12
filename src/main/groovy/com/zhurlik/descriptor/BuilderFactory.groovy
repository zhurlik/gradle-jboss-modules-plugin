package com.zhurlik.descriptor

import com.zhurlik.Ver
import com.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

import javax.xml.transform.stream.StreamSource

import static com.zhurlik.Ver.V_1_0
import static com.zhurlik.Ver.V_1_1
import static com.zhurlik.Ver.V_1_2
import static com.zhurlik.Ver.V_1_3

/**
 * A factory to get single instance of AbstractBuilder<T> for corresponded version.
 *
 * See https://github.com/jboss-modules/jboss-modules/tree/master/src/main/resources/schema
 *
 * @author zhurlik@gmail.com
 */
class BuilderFactory<T extends JBossModule> {
    private final static BUILDER_1_0 = new Xsd1_0();
    private final static BUILDER_1_1 = new Xsd1_1();
    private final static BUILDER_1_2 = new Xsd1_2();
    private final static BUILDER_1_3 = new Xsd1_3();

    /**
     * Do-nothing builder.
     */
    private static final Builder<T> NONE = new Builder<T>() {

        String getXmlDescriptor(JBossModule mod) {
            throw new UnsupportedOperationException('Version:' + mod.ver.number + ' is not implemented yet')
        }

        StreamSource getXsd() {
            return null
        }

        @Override
        String getPath(JBossModule module) {
            return null
        }

        @Override
        protected Ver getVersion() {
            return null
        }

        @Override
        protected void writeModuleType(JBossModule jmodule, MarkupBuilder xml) {
            //do nothing
        }
    }

    /**
     * Gets one of supported builder.
     *
     * @param module
     * @return builder to generate a xml descriptor
     */
    public static Builder<T> getBuilder(final Ver version) {
        switch (version) {
            case V_1_0: return BUILDER_1_0
            case V_1_1: return BUILDER_1_1
            case V_1_2: return BUILDER_1_2
            case V_1_3: return BUILDER_1_3
            default: return NONE
        }
    }
}
