package com.github.zhurlik

import com.github.zhurlik.descriptor.Builder
import com.github.zhurlik.descriptor.Xsd1_0
import com.github.zhurlik.descriptor.Xsd1_1
import com.github.zhurlik.descriptor.Xsd1_2
import com.github.zhurlik.descriptor.Xsd1_3
import com.github.zhurlik.descriptor.Xsd1_5
import com.github.zhurlik.descriptor.Xsd1_6
import com.github.zhurlik.descriptor.Xsd1_7
import com.github.zhurlik.descriptor.Xsd1_8
import com.github.zhurlik.descriptor.Xsd1_9
import com.github.zhurlik.extension.JBossModule

/**
 * Supported versions of JBoss Server
 *
 * @author zhurlik@gmail.com
 */
public enum Ver {
    V_1_0('1.0', 'xsd/module-1_0.xsd', Xsd1_0),
    V_1_1('1.1', 'xsd/module-1_1.xsd', Xsd1_1),
    V_1_2('1.2', 'xsd/module-1_2.xsd', Xsd1_2),
    V_1_3('1.3', 'xsd/module-1_3.xsd', Xsd1_3),
    V_1_5('1.5', 'xsd/module-1_5.xsd', Xsd1_5),
    V_1_6('1.6', 'xsd/module-1_6.xsd', Xsd1_6),
    V_1_7('1.7', 'xsd/module-1_7.xsd', Xsd1_7),
    V_1_8('1.8', 'xsd/module-1_8.xsd', Xsd1_8),
    V_1_9('1.9', 'xsd/module-1_9.xsd', Xsd1_9);

    private String number
    private String xsd
    private Builder<JBossModule> builder

    Ver(final String ver, final String xsd, Class<Builder<JBossModule>> clazz) {
        this.number = ver
        this.xsd = xsd
        this.builder = clazz.newInstance()
    }

    String getNumber() {
        return number
    }

    String getXsd() {
        return xsd
    }

    Builder<JBossModule> getBuilder() {
        return builder
    }
}