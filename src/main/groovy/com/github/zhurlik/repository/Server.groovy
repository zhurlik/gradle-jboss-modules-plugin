package com.github.zhurlik.repository

import com.github.zhurlik.extension.JBossModule

/**
 * To have a public methods to communicate with JBoss Server.
 *
 * @author zhurlik@gmail.com
 */
public interface Server {

    /**
     * Loading all available modules from JBoss Server to a map.
     *
     */
    void initTree()

    /**
     * List of available modules.
     *
     * @return List of names
     */
    List<String> getNames()

    /**
     * Returns a directory with modules under JBoss Server.
     *
     * @return File a directory
     */
    File getModulesDir()

    /**
     * Erases a folder with module.
     *
     * @param module String or {@link JBossModule}
     */
    void undeployModule(final module)

    /**
     * Returns a JBossModule by its name that will be generated from the module.xml.
     *
     * @param name a module name
     * @return an instance of {@link JBossModule}
     */
    JBossModule getModule(final String name)

    /**
     * Retruns a xml descriptor as a string.
     *
     * @param name a module name
     * @return a xml as string
     */
    String getMainXml(final String name)
}