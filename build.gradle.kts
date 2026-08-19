plugins {
    kotlin("jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.3"
}

group = "com.agniops.subtracker"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.portswigger.burp.extensions:montoya-api:2024.7")
    implementation("com.google.code.gson:gson:2.11.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.shadowJar {
    archiveBaseName.set("AgniOps-SubTracker")
    archiveClassifier.set("")
    archiveVersion.set("1.0.0")
    manifest {
        attributes["Burp-Extension-Name"] = "AgniOps SubTracker"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
