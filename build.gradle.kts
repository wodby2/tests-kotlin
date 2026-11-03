import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(kotlin("test"))
}

// Compile for Java 21 so the application runs on every JDK the Wodby Kotlin
// service offers, whichever JDK builds it.
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

application {
    mainClass = "com.example.ApplicationKt"
}

// The Wodby Java image runs one self-contained jar with `java -jar`, so the
// build packages the application and its dependencies together in
// build/libs/kotlin-boilerplate.jar. The jar without dependencies is marked
// `plain`, which the image leaves out when it looks for the jar to run.
tasks.shadowJar {
    archiveClassifier = ""
    mergeServiceFiles()
}

tasks.jar {
    archiveClassifier = "plain"
}

// Only the jar is deployed: skip the zip and tar distributions the
// application plugin would add to every build.
tasks.named { it in setOf("distZip", "distTar", "shadowDistZip", "shadowDistTar") }.configureEach {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}
