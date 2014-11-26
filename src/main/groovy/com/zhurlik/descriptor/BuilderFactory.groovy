package com.zhurlik.descriptor

import com.zhurlik.Ver
import com.zhurlik.extension.JBossModule

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
    /**
     * Do-nothing builder.
     */
    static final Builder<T> NONE = new Builder<T>() {

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
    }

    /**
     * Gets one of supported builder.
     *
     * @param module
     * @return builder to generate a xml descriptor
     */
    static Builder<T> getBuilder(final Ver version) {
        switch (version) {
            case V_1_0: return BUILDERS.V_1_0.getBuilder()
            case V_1_1: return BUILDERS.V_1_1.getBuilder()
            case V_1_2: return BUILDERS.V_1_2.getBuilder()
            case V_1_3: return BUILDERS.V_1_3.getBuilder()
            default: return NONE
        }
    }

    /**
     * A set of supported builders for different versions.
     */
    private static enum BUILDERS {

        V_1_0(Xsd1_0), V_1_1(Xsd1_1), V_1_2(Xsd1_2), V_1_3(Xsd1_3);

        // an instance to generate a xml descriptor
        private Builder builder;

        /**
         * Makes a builder by its Class.
         *
         * @param clazz class to know which instance must be created
         */
        BUILDERS(Class<Builder> clazz) {
            this.builder = clazz.newInstance()
        }

        /**
         * Returns an instance of IBuilder
         *
         * @return builder for xml descriptor
         */
        public Builder getBuilder() {
            this.builder
        }
    }
}
