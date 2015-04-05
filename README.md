# gradle-jboss-modules-plugin
[![Build Status](https://travis-ci.org/zhurlik/gradle-jboss-modules-plugin.svg?branch=master)](https://travis-ci.org/zhurlik/gradle-jboss-modules-plugin)
[![Coverage Status](https://coveralls.io/repos/zhurlik/gradle-jboss-modules-plugin/badge.png)](https://coveralls.io/r/zhurlik/gradle-jboss-modules-plugin)
[ ![Download](https://api.bintray.com/packages/zhurlik/mvn/gradle-jboss-modules-plugin/images/download.svg) ](https://bintray.com/zhurlik/mvn/gradle-jboss-modules-plugin/_latestVersion)
***
This plugin for gradle allows to create modules to be able to use them under JBoss 7.x/8.x   
The standard plugin 'distribution' generates archives for every servers which were defined in the project.
***
The main idea is to have an ability to make [JBoss Modules](https://docs.jboss.org/author/display/MODULES/Defining+a+module)
## How to install

```
buildscript {
    repositories {
        maven {
            url uri('http://dl.bintray.com/zhurlik/mvn')
        }
    }

    dependencies {
        classpath 'com.github.zhurlik:gradle-jboss-modules:0.6'
    }
}
```

## How to use
```groovy
apply plugin: 'com.github.zhurlik.jbossmodules'

repositories {
    mavenCentral()
}

dependencies {
    ['aop', 'beans', 'core', 'context'].each {
        jbossmodules "org.springframework:spring-${it}:${springVersion}"
    }
}

jbossrepos {
    serverA {
        home = '/home/zhurlik/programs/jboss-as-7.1.1.Final'
        version = V_1_1
    }
}

modules {

    // springframework
    springCore {
        moduleName = 'org.springframework.core'
        resources = ["spring-core-${springVersion}.jar"]
        dependencies = ['javax.api',
                        'org.jboss.vfs',
                        'org.apache.commons.logging'
        ]
    }

    springBeans {
        moduleName = 'org.springframework.beans'
        resources = ["spring-beans-${springVersion}.jar"]
        dependencies = ['org.springframework.spring-core',
                        'javax.api',
                        'org.apache.commons.logging'
        ]
    }

    springAop {
        moduleName = 'org.springframework.aop'
        resources = ["spring-aop-${springVersion}.jar", 'aopalliance-1.0.jar']
        dependencies = ['org.springframework.spring-beans',
                        'org.springframework.spring-core',
                        'javax.api', 'org.apache.commons.logging'
        ]
    }
    // springframework


    moduleA {
        // to define on which servers this module will be available, by default - all
        servers = ['serverA']
        moduleName = 'com.github.zhurlik.a'
        mainClass = 'zh'
        slot = '3.3.3'
        properties = ['ver' : '1.0', 'test' : 'zhurlik']
        resources = ['test1.jar', "spring-core-${springVersion}.jar",
                     [name: 'name', path: 'path1', filter: [include:'**']]
        ]
        dependencies = [
                [name: 'module1'],
                [name: 'module2', export: 'true'],
                [name: 'module3', export: 'false', exports: [
                        include: ['mine'],
                        exclude: ['*not*a', '*not*b']
                    ]
                ]
        ]
    }

    moduleB {
        moduleName = 'com.github.zhurlik.b'
        mainClass = 'zh'
        slot = '3.3.3'
        properties = ['ver' : '1.0', 'test' : 'zhurlik']
        resources = ['test1.jar', 'test2.jar',
                     [name: 'name', path: 'path1', filter: [exclude: '**']]
        ]
        dependencies = [
                [name: 'module1'],
                [name: 'module2', export: 'true'],
                [name: 'module3', export: 'false', imports: [
                        include: 'mine',
                        exclude: ['*not*a', '*not*b']
                    ]
                ]
        ]
    }
}

jbossrepos.each() {com.github.zhurlik.extension.JBossServer it->
    println '>> Server:' + it.home + ' modules:\n'

    it.initTree()
    it.names.each {
        println it
    }

    println it.getModule('org.jboss.jts.integration').moduleDescriptor
    assert it.getModule('org.jboss.jts.integration').isValid()
    println it.getMainXml('org.jboss.jts.integration')
}
```
```gradle makeModules```   
```gradle checkModules```   
```gradle serverADistTar```   
