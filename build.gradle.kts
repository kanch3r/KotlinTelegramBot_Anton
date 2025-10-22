plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}

application {
    mainClass.set("learningbot.telegram.TelegramKt")
}
tasks.test {
    useJUnitPlatform()
}
tasks.jar {
    manifest {
        attributes("Main-Class" to "learningbot.telegram.TelegramKt")
    }
}
kotlin {
    jvmToolchain(21)
}