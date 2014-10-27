package com.zhurlik.descriptor

import com.zhurlik.JBossModule

/**
 * Created by zhurlik on 10/27/14.
 */
class Xsd1_3 implements IBuilder<JBossModule> {
    @Override
    String getXmlDescriptor(JBossModule module) {
        throw new RuntimeException("Not supported")
    }
}
