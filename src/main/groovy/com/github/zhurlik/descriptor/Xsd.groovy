package com.github.zhurlik.descriptor

import com.github.zhurlik.descriptor.parser.ConfigurationTag
import com.github.zhurlik.descriptor.parser.ModuleAliasTag
import com.github.zhurlik.descriptor.parser.ModuleTag
import com.github.zhurlik.extension.JBossModule
import groovy.util.logging.Slf4j

import java.util.function.Supplier
import java.util.stream.Stream

/**
 * Core class to generate and to check xml descriptors for JBoss Modules
 *
 * @author zhurlik@gmail.com
 */
@Slf4j
abstract class Xsd {

    abstract String getXmlDescriptor(final JBossModule module)

    JBossModule makeModule(final String txt) {
        Stream.of(
                ConfigurationTag.parse(txt),
                ModuleAliasTag.parse(txt),
                ModuleTag.parse(txt))
                .filter({ final Optional it -> it.isPresent() })
                .findFirst()
                .map({ final Optional it -> it.get() })
                .map({ final Supplier<JBossModule> it -> it.get() })
                .orElseThrow { new IllegalArgumentException("There is a problem with parsing") }
    }
}