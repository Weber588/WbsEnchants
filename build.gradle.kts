import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("xyz.jpenilla.run-paper") version "3.0.0-beta.1" // Adds runServer and runMojangMappedServer tasks for testing
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.0" // Generates plugin.yml based on the Gradle config
}

group = "io.papermc.paperweight"
version = "1.0.0-SNAPSHOT"
description = "Test plugin for paperweight-userdev"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

// For 1.20.4 or below, or when you care about supporting Spigot on >=1.20.5:
/*
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

tasks.assemble {
  dependsOn(tasks.reobfJar)
}
 */

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
    // paperweight.foliaDevBundle("1.21.8-R0.1-SNAPSHOT")
    // paperweight.devBundle("com.example.paperfork", "1.21.8-R0.1-SNAPSHOT")
    implementation("io.github.Weber588:WbsUtils:1.0-SNAPSHOT")
    implementation("net.kyori:adventure-text-serializer-ansi:4.24.0")
}

tasks {
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release = 21
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    // Only relevant for 1.20.4 or below, or when you care about supporting Spigot on >=1.20.5:
    /*
    reobfJar {
      // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
      // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
      outputJar = layout.buildDirectory.file("libs/PaperweightTestPlugin-${project.version}.jar")
    }
     */
}

// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.
paperPluginYaml {
    main = "wbs.enchants.WbsEnchants"
    bootstrapper = "wbs.enchants.WbsEnchantsBootstrap"
    authors.add("Weber588")
    apiVersion = "1.21.8"
    dependencies {
        bootstrap.create("WbsUtils", {
            load = PaperPluginYaml.Load.BEFORE
            required = true
        })
        server.create("WbsUtils", {
            load = PaperPluginYaml.Load.BEFORE
            required = true
        })
    }
}
