# gradle-jboss-modules-plugin
![Build Status](https://travis-ci.org/zhurlik/gradle-jboss-modules-plugin.svg)
***
This plugin for gradle allows to create modules to be able to use them under JBoss 7.x/8.x
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
        classpath 'com.zhurlik:gradle-jboss-modules:0.4'
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
    jbossmodules 'org.springframework:spring-core:4.1.1.RELEASE'
}

jbossrepos {
    serverA {
        home = '/home/zhurlik/programs/jboss-as-7.1.1.Final'
        version = V_1_1
    }
}

modules {
    moduleA {
        moduleName = 'com.zhurlik.a'
        mainClass = 'zh'
        slot = '3.3.3'
        properties = ['ver' : '1.0', 'test' : 'zhurlik']
        resources = ['test1.jar', 'spring-core-4.1.1.RELEASE.jar',
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
}

jbossrepos.each() {com.zhurlik.extension.JBossServer it->
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
or
```gradle checkModules```

