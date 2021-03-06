package com.github.zhurlik

import static java.io.File.separator
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI

import com.github.zhurlik.extension.JBossModule
import com.github.zhurlik.tag.ConfigurationTag
import com.github.zhurlik.tag.ModuleAbsentTag
import com.github.zhurlik.tag.ModuleAliasTag
import com.github.zhurlik.tag.ModuleTag
import com.github.zhurlik.tag.XmlDeclarationTag
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Supplier
import java.util.stream.Stream

/**
 * Supported versions of JBoss Modules
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
enum Ver {
    V_1_0('1.0', 'xsd/module-1_0.xsd') {
        @Override
        Path getModulePath(final JBossModule jbModule) {
            Paths.get('modules',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot))
        }

        @Override
        String getXmlDescriptor(JBossModule jmodule) {
            Objects.requireNonNull(jmodule, 'JBossModule is null')

            Writer writer = new StringWriter()
            MarkupBuilder xml = new MarkupBuilder(writer)

            XmlDeclarationTag.write().accept(xml)

            if (jmodule.isModuleConfiguration()) {
                ConfigurationTag.write(jmodule).accept(xml)
            } else {
                Objects.requireNonNull(jmodule.moduleName, 'Module name is null')
                ModuleTag.write(jmodule).accept(xml)
            }

            writer.toString()
        }
    },
    V_1_1('1.1', 'xsd/module-1_1.xsd') {
        @Override
        Path getModulePath(final JBossModule jbModule) {
            Paths.get('modules',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot))
        }

        @Override
        String getXmlDescriptor(JBossModule jmodule) {
            Objects.requireNonNull(jmodule, 'JBossModule is null')

            Writer writer = new StringWriter()
            MarkupBuilder xml = new MarkupBuilder(writer)

            XmlDeclarationTag.write().accept(xml)

            if (jmodule.isModuleAlias()) {
                Objects.requireNonNull(jmodule.moduleName, 'Module name is null')
                ModuleAliasTag.write(jmodule).accept(xml)
            } else if (jmodule.isModuleConfiguration()) {
                ConfigurationTag.write(jmodule).accept(xml)
            } else {
                Objects.requireNonNull(jmodule.moduleName, 'Module name is null')
                ModuleTag.write(jmodule).accept(xml)
            }

            writer.toString()
        }
    },
    V_1_2('1.2', 'xsd/module-1_2.xsd') {
        @Override
        Path getModulePath(final JBossModule jbModule) {
            Paths.get('modules', 'system', 'layers', 'base',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.slot in [null, '']) ? 'main' : jbModule.slot))
        }

        @Override
        String getXmlDescriptor(final JBossModule jmodule) {
            Objects.requireNonNull(jmodule, 'JBossModule is null')
            Objects.requireNonNull(jmodule.moduleName, 'Module name is null')

            Writer writer = new StringWriter()
            MarkupBuilder xml = new MarkupBuilder(writer)

            XmlDeclarationTag.write().accept(xml)

            if (jmodule.isModuleAlias()) {
                ModuleAliasTag.write(jmodule).accept(xml)
            } else if (jmodule.isModuleAbsent()) {
                ModuleAbsentTag.write(jmodule).accept(xml)
            } else {
                ModuleTag.write(jmodule).accept(xml)
            }

            writer.toString()
        }
    },
    V_1_3('1.3', 'xsd/module-1_3.xsd') {
        @Override
        Path getModulePath(final JBossModule jbModule) {
            V_1_2.getModulePath(jbModule)
        }

        @Override
        String getXmlDescriptor(JBossModule jmodule) {
            V_1_2.getXmlDescriptor(jmodule)
        }
    },
    V_1_5('1.5', 'xsd/module-1_5.xsd') {
        @Override
        Path getModulePath(final JBossModule jbModule) {
            V_1_2.getModulePath(jbModule)
        }

        @Override
        String getXmlDescriptor(JBossModule jmodule) {
            V_1_2.getXmlDescriptor(jmodule)
        }
    },
    V_1_6('1.6', 'xsd/module-1_6.xsd') {
        @Override
        Path getModulePath(final JBossModule jbModule) {
            V_1_2.getModulePath(jbModule)
        }

        @Override
        String getXmlDescriptor(JBossModule jmodule) {
            V_1_2.getXmlDescriptor(jmodule)
        }
    },
    V_1_7('1.7', 'xsd/module-1_7.xsd') {
        @Override
        Path getModulePath(final JBossModule jbModule) {
            Paths.get('modules', 'system', 'layers', 'base',
                    jbModule.moduleName.split('\\.').join(separator),
                    ((jbModule.version in [null, '']) ? 'main' : jbModule.version))
        }

        @Override
        String getXmlDescriptor(JBossModule jmodule) {
            V_1_2.getXmlDescriptor(jmodule)
        }
    },
    V_1_8('1.8', 'xsd/module-1_8.xsd') {
        @Override
        Path getModulePath(final JBossModule jbModule) {
            V_1_7.getModulePath(jbModule)
        }

        @Override
        String getXmlDescriptor(JBossModule jmodule) {
            V_1_2.getXmlDescriptor(jmodule)
        }
    },
    V_1_9('1.9', 'xsd/module-1_9.xsd') {
        @Override
        Path getModulePath(final JBossModule jbModule) {
            V_1_7.getModulePath(jbModule)
        }

        @Override
        String getXmlDescriptor(JBossModule jmodule) {
            V_1_2.getXmlDescriptor(jmodule)
        }
    }

    /**
     * Makes JBoss Module from xml file.
     *
     * @param xml
     * @return new instance of JBossModule
     */
    static JBossModule makeModule(final String xml) {
        Stream.of(
                ConfigurationTag.parse(xml),
                ModuleAliasTag.parse(xml),
                ModuleTag.parse(xml))
                .filter { final Optional it -> it.isPresent() }
                .findFirst()
                .map { final Optional it -> it.get() }
                .map { final Supplier<JBossModule> it -> it.get() }
                .orElseThrow { new IllegalArgumentException('There is a problem with parsing') }
    }

    private static final SchemaFactory FACTORY = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI)

    abstract Path getModulePath(final JBossModule jBossModule)

    abstract String getXmlDescriptor(final JBossModule jBossModule)

    private final String number
    private final String xsdPath

    protected Ver(final String ver, final String xsdPath) {
        this.number = ver
        this.xsdPath = xsdPath
    }

    String getNumber() {
        number
    }

    String getXsdPath() {
        xsdPath
    }

    /**
     * To validate a xml files.
     *
     * @param xml descriptor
     * @return true if valid
     */
    boolean isValid(final String xml) {
        try {
            Schema schema = FACTORY.newSchema(new StreamSource(
                    getClass().classLoader.getResourceAsStream(xsdPath)))
            Validator validator = schema.newValidator()
            validator.validate(new StreamSource(new StringReader(xml)))
            return true
        } catch (all) {
            log.error('>> Validation Error:', all)
            return false
        }
    }
}
