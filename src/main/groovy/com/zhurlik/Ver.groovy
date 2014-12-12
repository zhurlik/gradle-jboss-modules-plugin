package com.zhurlik

/**
 * Supported versions of JBoss Server
 *
 * @author zhurlik@gmail.com
 */
public enum Ver {
    V_1_0('1.0', 'xsd/module-1_0.xsd'), V_1_1('1.1', 'xsd/module-1_1.xsd'), V_1_2('1.2', 'xsd/module-1_2.xsd'), V_1_3('1.3', 'xsd/module-1_3.xsd');

    private String number
    private String xsd

    Ver(final String ver, final String xsd) {
        this.number = ver
        this.xsd = xsd
    }

    String getNumber() {
        return number
    }

    String getXsd() {
        return xsd
    }
}