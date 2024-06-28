import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "dev.tieseler"
version = "1.0.0-Alpha"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    flatDir {
        dirs("dependencies")
    }
}
dependencies {
    //https://github.com/PaperMC/folia
    implementation("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")

    implementation("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    //https://mvnrepository.com/artifact/org.postgresql/postgresql
    implementation("org.postgresql:postgresql:42.7.3")

    compileOnly("me.clip:placeholderapi:2.11.7-DEV-Folia")

    //acf
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    //hikari
    implementation("com.zaxxer:HikariCP:5.1.0")

    implementation("org.hibernate.orm:hibernate-core:6.5.2.Final")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.5.2.Final")
}

bukkit {
    name = "Factions"
    author = "mi3zekater"
    main = "dev.tieseler.factions.Factions"
    apiVersion = "1.20.4"
    foliaSupported = true
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP

    softDepend = listOf(
        "PlaceholderAPI"
    )

    permissions {
        register("factions.admin") {
            description = "Use all admin commands like /chunk bypass"
            default = Permission.Default.OP
        }
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    minimize()
    archiveFileName.set("${project.name}-${project.version}.jar")
    relocate("co.aikar.commands", "dev.tieseler.factions.shaded.acf")
}

kotlin {
    jvmToolchain(17)
}
