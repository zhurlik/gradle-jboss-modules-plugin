# gradle-jboss-modules-plugin
***
This plugin for gradle allows to create modules to be able to use them under JBoss 7.1
***
The main idea is to have an ability to make [JBoss Modules](https://docs.jboss.org/author/display/MODULES/Defining+a+module)
## How to install



## How to use
```groovy
apply plugin: 'com.github.zhurlik.jbossmodules'

repositories {
    mavenCentral()
}

dependencies {
    jbossmodules 'org.springframework:spring-core:4.1.1.RELEASE'
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
```
gradle makeModules
or
gradle checkModules

