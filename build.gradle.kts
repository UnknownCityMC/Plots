import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription


plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.8"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "de.unknowncity"
version = "0.1.0"

val mainClass = "${group}.${rootProject.name.lowercase()}.PlotsPlugin"
val shadeBasePath = "${group}.${rootProject.name.lowercase()}.libs."

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.unknowncity.de/snapshots")

    maven("https://repo.nightexpressdev.com/releases")
    maven("https://jitpack.io")

    mavenLocal()
}

dependencies {
    bukkitLibrary(libs.jackson.yaml)
    bukkitLibrary(libs.configurate.yaml)


    implementation(libs.invui)
    compileOnly(libs.coinsengine) {
        exclude(group = "*", module = "*")
    }

    compileOnly(libs.paper.api)
    compileOnly(libs.papi)
    compileOnly(libs.astralib)
    compileOnly(libs.worldguard)
    compileOnly(libs.plan)

    implementation(platform("com.intellectualsites.bom:bom-newest:1.55")) // Ref: https://github.com/IntellectualSites/bom
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit")

    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.github.mwiede:jsch:2.27.7")
    }
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

    depend = listOf("AstraLib", "WorldGuard", "CoinsEngine")

    softDepend = listOf("PlaceholderAPI")

    defaultPermission = BukkitPluginDescription.Permission.Default.OP
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        fun relocateDependency(from: String) = relocate(from, "$shadeBasePath$from")

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
        minecraftVersion("1.21.10")

        downloadPlugins {
            modrinth("WorldGuard", "7.0.14")
            modrinth("CoinsEngine", "2.5.2")
            modrinth("NightCore", "2.8.3")
            url("https://download.luckperms.net/1605/bukkit/loader/LuckPerms-Bukkit-5.5.16.jar")
            url("https://ci.athion.net/job/FastAsyncWorldEdit/1202/artifact/artifacts/FastAsyncWorldEdit-Paper-2.14.1-SNAPSHOT-1202.jar")
            url("https://ci.unknowncity.de/job/AstraLib/56/artifact/astralib-paper-plugin/build/libs/AstraLib-Paper-0.7.0-SNAPSHOT-%2356.jar")
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

    register("uploadJarToFTP") {
        dependsOn(shadowJar)
        doLast {
            val jarFile = getByName("shadowJar").outputs.files.singleFile

            val host = System.getenv("FTP_SERVER")!!
            val port = System.getenv("FTP_PORT")?.toInt() ?: 22
            val user = System.getenv("FTP_USER")!!
            val password = System.getenv("FTP_PASSWORD")!!

            val jsch = JSch()
            val session = jsch.getSession(user, host, port).apply {
                setPassword(password)
                setConfig("StrictHostKeyChecking", "no")
                connect()
            }

            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()
            channel.cd("/plugins")
            channel.put(jarFile.absolutePath, jarFile.name)
            println("Upload successful!")

            channel.disconnect()
            session.disconnect()
        }
    }

    compileJava {
        dependsOn(clean)
    }
}