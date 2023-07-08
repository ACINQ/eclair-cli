plugins {
    kotlin("multiplatform") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
}

group = "fr.acinq.lightning"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    val nativeMain by sourceSets.getting {
        dependencies {
            implementation("io.arrow-kt:arrow-core:1.2.0-RC")
            implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.21")
            implementation("io.ktor:ktor-client-core:2.3.2")
            implementation("io.ktor:ktor-client-cio:2.3.2")
            implementation("io.ktor:ktor-client-json:2.3.2")
            implementation("io.ktor:ktor-client-serialization:2.3.2")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.2")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.2")
        }
    }
    val nativeTest by sourceSets.getting {
        dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }
    }

    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
            }
        }
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
}