package com.zhurlik.descriptor

import com.zhurlik.JBossModule

/**
 * @author
 */

interface IBuilder <T extends JBossModule> {

    String getXmlDescriptor(final T module)

    enum Ver {
        V_1_0('1.0'), V_1_1('1.1'), V_1_2('1.2'), V_1_3('1.3');

        def String version

        Ver(final String ver) {
            this.version = ver
        }
    }
}


