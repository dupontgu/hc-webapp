plugins {
    kotlin("multiplatform") version "1.6.20"
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev679"
    kotlin("plugin.serialization") version "1.6.20"
    id("com.google.cloud.tools.appengine") version "2.4.1"
    application
}

group = "com.dupontgu"
version = "1.0"

val ktorVersion = "2.0.3"

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.13.0"
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        versions.webpackCli.version = "4.10.0"
    }
}

repositories {
    jcenter()
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

appengine {  // App Engine tasks configuration
    deploy {   // deploy configuration
        projectId = "GCLOUD_CONFIG"
        version = "1"
        stopPreviousVersion = false
        promote = true
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
        withJava()
        apply(plugin = "com.google.cloud.tools.appengine")
    }
    js(IR) {
        binaries.executable()
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-network-tls-certificates:$ktorVersion")
                implementation("com.github.kokorin.jaffree:jaffree:2022.06.03")
                implementation("org.slf4j:slf4j-api:1.7.36")
                implementation("org.slf4j:slf4j-simple:1.7.36")
                implementation( "io.ktor:ktor-server-servlet:2.0.2")
                compileOnly("javax.servlet:servlet-api:2.3")
                implementation(compose.runtime)
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation(npm("file-saver", "2.0.5"))
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.foundation)
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}