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
    bukkitLibrary(libs.sadu.queries)
    bukkitLibrary(libs.sadu.mariadb)
    bukkitLibrary(libs.sadu.datasource)
    bukkitLibrary(libs.sadu.mapper)
    bukkitLibrary(libs.jackson.yaml)
    bukkitLibrary(libs.configurate.yaml)


    implementation("xyz.xenondevs.invui", "invui", "1.37")


    compileOnly(libs.paper.api)
    compileOnly(libs.papi)
    compileOnly("de.unknowncity.astralib", "astralib-paper-api", "0.5.0-SNAPSHOT")
    compileOnly("com.sk89q.worldguard", "worldguard-bukkit", "7.0.10")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))

    testImplementation("org.junit.jupiter:junit-jupiter")
}

bukkit {

    name = "UC-Plots"
    version = "${project.version}"
    description = "A simple but effective single mariadb plugin for paper"

    author = "UnknownCity"

    main = mainClass

    foliaSupported = false

    apiVersion = "1.21"

    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    depend = listOf("AstraLib")

    softDepend = listOf("PlaceholderAPI")

    defaultPermission = BukkitPluginDescription.Permission.Default.OP
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        fun relocateDependency(from : String) = relocate(from, "$shadeBasePath$from")

        relocateDependency("xyz.xenondevs.invui")
        relocateDependency("org.incendo")
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

        downloadPlugins {
            url("https://ci.unknowncity.de/job/AstraLib/lastSuccessfulBuild/artifact/astralib-paper/build/libs/AstraLib-Paper-0.3.0-SNAPSHOT-%2326.jar")
        }
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