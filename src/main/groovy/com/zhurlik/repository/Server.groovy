package com.zhurlik.repository

import com.zhurlik.extension.JBossModule

/**
 * @author zhurlik@gmail.com
 */
public interface Server {

    void initTree()

    List<String> getNames()

    void deployModule(final JBossModule module)

    JBossModule getModule(final String name)

    String getMainXml(final String name)
}