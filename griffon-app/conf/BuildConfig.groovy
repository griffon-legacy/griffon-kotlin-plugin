griffon.project.dependency.resolution = {
    // implicit variables
    // pluginName:     plugin's name
    // pluginVersion:  plugin's version
    // pluginDirPath:  plugin's install path
    // griffonVersion: current Griffon version
    // groovyVersion:  bundled groovy
    // springVersion:  bundled Spring
    // antVertsion:    bundled Ant
    // slf4jVersion:   bundled Slf4j

    // inherit Griffon' default dependencies
    inherits("global") {
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        griffonHome()
        mavenCentral()
        mavenRepo 'http://repository.sonatype.org/content/groups/public'

        // pluginDirPath is only available when installed
        String basePath = pluginDirPath? "${pluginDirPath}/" : ''
        flatDir name: "kotlinLibDir", dirs: ["${basePath}lib"]
    }
    dependencies {
        String asmVersion = '3.3.1'
        String kotlinVersion = '280212-cf73c1f3'
        build "com.jetbrains.kotlin:kotlin-annotations:$kotlinVersion",
              "com.jetbrains.kotlin:kotlin-cli:$kotlinVersion",
              "com.jetbrains.kotlin:kotlin-intellij-core:$kotlinVersion",
              "com.jetbrains.kotlin:kotlin-picocontainer:$kotlinVersion",
              "com.jetbrains.kotlin:kotlin-trove4j:$kotlinVersion",
              "com.jetbrains.kotlin:kotlin-build-tools:$kotlinVersion",
              "com.jetbrains.kotlin:kotlin-compiler:$kotlinVersion",
              "com.jetbrains.kotlin:kotlin-runtime:$kotlinVersion",
              "asm:asm-commons:$asmVersion",
              "asm:asm-util:$asmVersion",
              "asm:asm:$asmVersion",
              "com.google.guava:guava:11.0.1"
        compile "com.jetbrains.kotlin:kotlin-runtime:$kotlinVersion"
    }
}

griffon {
    doc {
        logo = '<a href="http://griffon.codehaus.org" target="_blank"><img alt="The Griffon Framework" src="../img/griffon.png" border="0"/></a>'
        sponsorLogo = "<br/>"
        footer = "<br/><br/>Made with Griffon (@griffon.version@)"
    }
}

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error 'org.codehaus.griffon',
          'org.springframework',
          'org.apache.karaf',
          'groovyx.net'
    warn  'griffon'
}