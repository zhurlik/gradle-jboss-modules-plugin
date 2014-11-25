package com.zhurlik.repository

import com.zhurlik.extension.JBossModule

/**
 * @author zhurlik@gmail.com
 */
public interface Server {

    void initTree()

    List<String> getNames()

    File getModulesDir()

    void undeployModule(final module)

    JBossModule getModule(final String name)

    String getMainXml(final String name)
}