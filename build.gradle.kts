
val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.8"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-host-common-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    task("deploy") {
        dependsOn("clean", "shadowJar")
        ant.withGroovyBuilder {
            doLast {
                val knownHosts = File.createTempFile("knownhosts", "txt")
                val user = "root"
                val host = "192.168.1.31"
                val key = file("keys/party-game")
                val jarFileName = "com.example.party-game.jar"
                try {
                    "scp"(
                        "file" to file("build/libs/$jarFileName"),
                        "todir" to "$user@$host:/root/party-game",
                        "keyfile" to key,
                        "trust" to true,
                        "knownhosts" to knownHosts
                    )
                    "ssh"(
                        "host" to host,
                        "username" to user,
                        "keyfile" to key,
                        "trust" to true,
                        "knownhosts" to knownHosts,
                        "command" to "mv /root/party-game/$jarFileName /root/party-game/party-game.jar"
                    )
                    "ssh"(
                        "host" to host,
                        "username" to user,
                        "keyfile" to key,
                        "trust" to true,
                        "knownhosts" to knownHosts,
                        "command" to "systemctl stop party-game"
                    )
                    "ssh"(
                        "host" to host,
                        "username" to user,
                        "keyfile" to key,
                        "trust" to true,
                        "knownhosts" to knownHosts,
                        "command" to "systemctl start party-game"
                    )
                } finally {
                    knownHosts.delete()
                }
            }
        }
    }
}
