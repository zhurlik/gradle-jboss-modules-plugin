package com.github.zhurlik.descriptor

import com.github.zhurlik.Ver
import com.github.zhurlik.extension.JBossModule
import groovy.xml.MarkupBuilder

/**
 * This class contains main methods to generate and to write tags of a xml descriptor using different xsd files.
 * <p> See
 *     <ul>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_0.xsd">module-1_0.xsd</a></li>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_1.xsd">module-1_1.xsd</a></li>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_2.xsd">module-1_2.xsd</a></li>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_3.xsd">module-1_3.xsd</a></li>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_5.xsd">module-1_5.xsd</a></li>
 *     <li><a href="https://github.com/jboss-modules/jboss-modules/blob/master/src/main/resources/schema/module-1_6.xsd">module-1_6.xsd</a></li>
 *     </ul>
 * </p>
 *
 * @author zhurlik@gmail.com
 */
abstract class Xsd {

    abstract protected Ver getVersion()

    /**
     * Writes the module declaration type; contains dependencies, resources, and the main class specification.
     * <p>
     * Root element for a module declaration.
     * </p>
     * See <xsd:element name="module" type="moduleType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    abstract protected void writeModuleType(final JBossModule jmodule, final MarkupBuilder xml)

    /**
     * Writes an explicitly absent module.
     * <p>Root element for an absent module.</p>
     * See <xsd:element name="module-absent" type="moduleAbsentType">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeModuleAbsentType(final JBossModule jmodule, final MarkupBuilder xml) {
        assert jmodule.moduleName != null, 'Module Name is null'

        def attrs = [xmlns: 'urn:jboss:module:' + getVersion().number, name: jmodule.moduleName]
        attrs += (jmodule.slot in [null, '']) ? [:] : [slot: jmodule.slot]

        xml.'module-absent'(attrs)
    }

    /**
     *  Specifies the main class of this module; used to run the module from the command-line (optional).
     *  <br/>
     *  See <xsd:element name="main-class" type="classNameType" minOccurs="0">
     *
     * @param jmodule current module
     * @param xml MarkupBuilder to have a reference to xml
     */
    protected void writeMainClass(final JBossModule jmodule, final MarkupBuilder xml) {
        if (!(jmodule.mainClass in [null, ''])) {
            xml.'main-class'(name: jmodule.mainClass)
        }
    }
}
