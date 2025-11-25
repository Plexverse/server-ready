import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    `java-library`
    id("com.mineplex.sdk.plugin") version "1.21.9"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

group = "net.plexverse.serverready"
version = "1.0.0"

tasks {
    build {
        dependsOn(named("generatePaperPluginDescription"))
    }
}

paper {
    name = "ServerReady"
    version = project.version.toString()
    main = "net.plexverse.serverready.ServerReady"
    apiVersion = "1.21"

    serverDependencies {
        register("StudioEngine") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}