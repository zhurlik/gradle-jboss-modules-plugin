package com.zhurlik.repository

import com.zhurlik.extension.JBossModule

/**
 * @author zhurlik@gmail.com
 */
public interface Server {

    Map getAvailableModules()

    void updateModule(final JBossModule module)

}