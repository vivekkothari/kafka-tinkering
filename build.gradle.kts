plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.3"
    kotlin("plugin.serialization") version "2.1.21"
    application
}

group = "com.github.vivekkothari.kafka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:4.0.0")
    implementation("org.apache.kafka:kafka-streams:4.0.0")
    implementation("io.ktor:ktor-server-netty")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-core:1.5.18")
    implementation("ch.qos.logback:logback-classic:1.5.17")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.github.vivekkothari.kafka.KafkaProducerExample")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(23)
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get()
        )
    }
}
