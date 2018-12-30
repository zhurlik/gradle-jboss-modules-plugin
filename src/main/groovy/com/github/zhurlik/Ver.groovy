package com.github.zhurlik

import com.github.zhurlik.descriptor.Xsd
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
import groovy.util.logging.Slf4j

import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator
import java.nio.file.Path
import java.nio.file.Paths

import static java.io.File.separator
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI

/**
 * Supported versions of JBoss Server
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
enum Ver {
    V_1_0('1.0', 'xsd/module-1_0.xsd', Xsd1_0){
        @Override
        Path getModulePath(final JBossModule jbModule) {
            return Paths.get('modules',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot))
        }
    },
    V_1_1('1.1', 'xsd/module-1_1.xsd', Xsd1_1){
        @Override
        Path getModulePath(final JBossModule jbModule) {
            return Paths.get('modules',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot))
        }
    },
    V_1_2('1.2', 'xsd/module-1_2.xsd', Xsd1_2){
        @Override
        Path getModulePath(final JBossModule jbModule) {
            return Paths.get('modules', 'system', 'layers', 'base',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot))
        }
    },
    V_1_3('1.3', 'xsd/module-1_3.xsd', Xsd1_3){
        @Override
        Path getModulePath(final JBossModule jbModule) {
            return Paths.get('modules', 'system', 'layers', 'base',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot))
        }
    },
    V_1_5('1.5', 'xsd/module-1_5.xsd', Xsd1_5){
        @Override
        Path getModulePath(final JBossModule jbModule) {
            return Paths.get('modules', 'system', 'layers', 'base',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot))
        }
    },
    V_1_6('1.6', 'xsd/module-1_6.xsd', Xsd1_6){
        @Override
        Path getModulePath(final JBossModule jbModule) {
            return Paths.get('modules', 'system', 'layers', 'base',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot))
        }
    },
    V_1_7('1.7', 'xsd/module-1_7.xsd', Xsd1_7){
        @Override
        Path getModulePath(final JBossModule jbModule) {
            return Paths.get('modules', 'system', 'layers', 'base',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.version in [null, '']) ? 'main' : jbModule.version))
        }
    },
    V_1_8('1.8', 'xsd/module-1_8.xsd', Xsd1_8){
        @Override
        Path getModulePath(final JBossModule jbModule) {
            return Paths.get('modules', 'system', 'layers', 'base',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.version in [null, '']) ? 'main' : jbModule.version))
        }
    },
    V_1_9('1.9', 'xsd/module-1_9.xsd', Xsd1_9){
        @Override
        Path getModulePath(final JBossModule jbModule) {
            return Paths.get('modules', 'system', 'layers', 'base',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.version in [null, '']) ? 'main' : jbModule.version))
        }
    };

    static final FACTORY = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI)

    abstract Path getModulePath(final JBossModule jBossModule)

    private String number
    private String xsdPath
    private Xsd xsd

    Ver(final String ver, final String xsdPath, Class<Xsd> clazz) {
        this.number = ver
        this.xsdPath = xsdPath
        this.xsd = clazz.getDeclaredConstructor().newInstance()
    }

    String getNumber() {
        return number
    }

    Xsd getXsd() {
        return xsd
    }

    String getXsdPath() {
        return xsdPath
    }

    /**
     * To validate a xml files.
     *
     * @param xml descriptor
     * @return true if valid
     */
    boolean isValid(final String xml) {
        try {
            final Schema schema = FACTORY.newSchema(new StreamSource(getClass().classLoader.getResourceAsStream(xsdPath)))
            final Validator validator = schema.newValidator()
            validator.validate(new StreamSource(new StringReader(xml)))
            return true
        } catch (all) {
            log.error('>> Validation Error:', all)
            return false
        }
    }
}