plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.0.0"
    kotlin("plugin.serialization") version "2.1.10"
}

group = "com.github.vivekkothari.kafka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:3.9.0")
    implementation("org.apache.kafka:kafka-streams:3.9.0")
    implementation("io.ktor:ktor-server-netty")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-core:1.5.17")
    implementation("ch.qos.logback:logback-classic:1.5.17")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}