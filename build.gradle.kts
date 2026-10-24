import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("net.fabricmc.fabric-loom")
    kotlin("jvm")
    kotlin("plugin.compose")
    `maven-publish`
}

group = property("maven_group") as String
version = property("mod_version") as String


repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.terraformersmc.com/")
    maven("https://api.modrinth.com/maven")
//    ivy {
//        url = uri("https://github.com/odtheking/Odin/releases/download")
//        patternLayout {
//            artifact("[revision]/[artifact]-[revision].[ext]")
//        }
//        metadataSources {
//            artifact()
//        }
//    }
}

val bundled: Configuration by configurations.creating
configurations.implementation { extendsFrom(bundled) }

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")

    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    runtimeOnly("me.djtheredstoner:DevAuth-fabric:${property("devauth_version")}")
    implementation("com.github.odtheking:Odin:${property("odin_version")}")

    compileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")

    // implementation("com.github.stivais:Commodore:${property("commodore_version")}")
    property("commodore_version").let {
        implementation("com.github.stivais:Commodore:$it")
        include("com.github.stivais:Commodore:$it")
    }

    bundled("androidx.compose.runtime:runtime-desktop:${property("compose_runtime_version")}") {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm")
    }

//    property("minecraft_lwjgl_version").let { lwjglVersion ->
//        implementation("org.lwjgl:lwjgl-nanovg:$lwjglVersion")
//
//        listOf("windows", "linux", "macos", "macos-arm64").forEach { os ->
//            implementation("org.lwjgl:lwjgl-nanovg:$lwjglVersion:natives-$os")
//        }
//    }

}

configurations.named("include") {
    dependencies.addAllLater(provider {
        bundled.resolvedConfiguration.resolvedArtifacts.map { project.dependencies.create(it.moduleVersion.id.toString()) }
    })
}

loom {
    runConfigs.named("client") {
        generateRunConfig.set(true)
        jvmArguments.addAll(
            "-Dmixin.debug.export=true",
            "-Ddevauth.enabled=true",
            "-Ddevauth.account=main",
            "-XX:+IgnoreUnrecognizedVMOptions",
            "-XX:+AllowEnhancedClassRedefinition",
        )
    }

    runConfigs.named("server") {
        generateRunConfig.set(false)
    }
}

afterEvaluate {
    loom.runs.named("client") {
        jvmArguments.add("-javaagent:${configurations.compileClasspath.get().find { it.name.contains("sponge-mixin") }}")
    }
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(getProperties())
        }
    }

    compileKotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_25
            freeCompilerArgs.add("-Xlambdas=class") //Commodore
        }
    }

    compileJava {
        sourceCompatibility = "25"
        targetCompatibility = "25"
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    }
}

base {
    archivesName.set(project.property("archives_base_name") as String)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }

    withSourcesJar()
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}