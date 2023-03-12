import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    application
}

group = "io.github.vbe0201"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-23")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.5")

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
}

application {
    mainClass.set("MainKt")
}
