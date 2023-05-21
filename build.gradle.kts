import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "io.github.vbe0201"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.3.2-SNAPSHOT"
val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!,
).let { (name, arch) ->
    // @formatter:off
    val isArm64 = arrayOf("arm64", "armv8", "aarch64").any { arch.startsWith(it) }
    val isArm32 = arch.startsWith("arm") && !isArm64

    val unixFlag = if (isArm64) "-arm64" else if (isArm32) "-arm32" else ""
    val macosFlag = if (isArm64) "-arm64" else ""
    val winFlag = if (isArm64) "-arm64" else if (arch.contains("64")) "" else "-x86"
    // @formatter:on

    val unix = arrayOf("Linux", "FreeBSD", "SunOS", "Unit")
    val macos = arrayOf("Mac OS X", "Darwin")
    val windows = arrayOf("Windows")

    when {
        unix.any { name.startsWith(it) } -> "natives-linux$unixFlag"
        macos.any { name.startsWith(it) } -> "natives-macos$macosFlag"
        windows.any { name.startsWith(it) } -> "natives-windows$winFlag"

        else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)

    runtimeOnly("ch.qos.logback:logback-classic:1.4.6")
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-23")
    implementation("org.slf4j:slf4j-api:2.0.7")

    implementation("org.ow2.asm:asm:9.4")
    implementation("org.ow2.asm:asm-commons:9.4")

    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile>().all {
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.ExperimentalUnsignedTypes"
        kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
    }
}

kotlin {
    jvmToolchain(19)
     sourceSets.all {
         languageSettings {
             languageVersion = "2.0"
         }
     }
}

application {
    mainClass.set("io.github.vbe0201.jiffy.ApplicationKt")
}
