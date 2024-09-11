import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

group = "de.unknowncity"
version = "0.1.0"

val mainClass = "${group}.${rootProject.name.lowercase()}.PlotsPlugin"
val shadeBasePath = "${group}.${rootProject.name.lowercase()}.libs."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.unknowncity.de/snapshots")
    mavenLocal()
}

dependencies {
    bukkitLibrary("org.incendo", "cloud-paper", "2.0.0-beta.9")
    bukkitLibrary("org.incendo", "cloud-minecraft-extras", "2.0.0-beta.9")
    bukkitLibrary("de.chojo.sadu", "sadu-queries", "2.2.1")
    bukkitLibrary("de.chojo.sadu", "sadu-mysql", "2.2.1")
    bukkitLibrary("de.chojo.sadu", "sadu-datasource", "2.2.1")
    bukkitLibrary("de.chojo.sadu", "sadu-updater", "2.2.1")
    bukkitLibrary("org.spongepowered", "configurate-yaml", "4.1.2")
    bukkitLibrary("org.spongepowered", "configurate-hocon", "4.1.2")

    implementation("xyz.xenondevs.invui", "invui", "1.33")


    compileOnly("io.papermc.paper", "paper-api", "1.21-R0.1-SNAPSHOT")
    compileOnly("me.clip", "placeholderapi", "2.11.6")
    compileOnly("de.unknowncity.astralib", "astralib-paper", "0.3.0")
    compileOnly("com.sk89q.worldguard", "worldguard-bukkit", "7.0.10")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

bukkit {

    name = "UC-Plots"
    version = "${project.version}"
    description = "A simple but effective single plot plugin for paper"

    author = "UnknownCity"

    main = mainClass

    foliaSupported = false

    apiVersion = "1.21"

    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    softDepend = listOf("PlaceholderAPI")

    defaultPermission = BukkitPluginDescription.Permission.Default.OP
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        fun relocateDependency(from : String) = relocate(from, "$shadeBasePath$from")

        relocateDependency("org.incendo")
        relocateDependency("de.chojo.sadu")
        relocateDependency("org.spongepowered")
        relocateDependency("xyz.xenondevs.invui")
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        archiveBaseName.set(rootProject.name)
        archiveVersion.set(rootProject.version.toString())
    }

    runServer {
        minecraftVersion("1.21")

    }

    register<Copy>("copyToServer") {
        val path = System.getenv("SERVER_DIR")
        if (path.toString().isEmpty()) {
            println("No SERVER_DIR env variable set")
            return@register
        }
        from(shadowJar)
        destinationDir = File(path.toString())
    }

    compileJava {
        dependsOn(clean)
    }
}