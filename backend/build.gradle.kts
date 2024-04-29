import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "app.vatov.idserver"
version = "1.0.0"

val targetJVM = "11"
val ktorVersion = "2.3.9"

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJVM))
    }
}

repositories {
    mavenCentral()
}

val environment: String by project

sourceSets {
    val main by getting {
        when (environment) {
            "dev" -> {
                java.srcDirs("src/main/kotlin", "src/main/dev")
                resources.srcDirs("src/main/kotlin/resources")
            }

            "prod" -> {
                java.srcDirs("src/main/kotlin", "src/main/prod")
                resources.srcDirs("src/main/kotlin/resources")
            }

            else -> {
                throw Exception("Please provide 'Environment' variable prod or dev")
            }
        }
    }
}

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-velocity:$ktorVersion")

    implementation("com.sun.mail:jakarta.mail:2.0.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")

    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation("org.slf4j:slf4j-api:2.0.12")


    // Test dependencies
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("app.vatov.idserver.MainKt")
    tasks.run.get().workingDir = File("./workingDir")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = targetJVM
}

distributions {
    create("release") {

        contents {
            from(getTasksByName("shadowJar", false)) {
                into("lib")
            }
            from("workingDir/config/Settings.conf.default") {
                into("config").rename {
                    "Settings.conf"
                }
            }
            from("workingDir/static") {
                into("static")
            }
            from("workingDir/templates/default") {
                into("templates/default")
            }
            from("dist/startScripts")
            from("../frontend/build/web") {
                into("admin")
            }
        }
    }
}