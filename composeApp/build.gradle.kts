import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val hostOs = System.getProperty("os.name").lowercase()
val isMacOs = hostOs.contains("mac")

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    androidTarget()
    jvm("desktop")

    /*
     * Compose Multiplatform 1.11.x no longer supports Apple x86_64 targets.
     * Also, iOS native targets should only be registered on macOS hosts.
     *
     * Windows/Linux developers can still build Android + Desktop normally.
     * On Apple Silicon macOS, this enables:
     * - iosArm64: real iPhone/iPad devices
     * - iosSimulatorArm64: Apple Silicon simulator
     */
    if (isMacOs) {
        val iosTargets = listOf(
            iosArm64(),
            iosSimulatorArm64()
        )

        iosTargets.forEach { target ->
            target.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)

                implementation(libs.coroutines.core)
                implementation(libs.serialization.json)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)

                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundled)

                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)

                implementation(libs.uuid)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.coroutines.test)
            }
        }

        val jvmSharedMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.hivemq.client)
                implementation(libs.coroutines.jdk8)
            }
        }

        val androidMain by getting {
            dependsOn(jvmSharedMain)
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.room.sqlite.wrapper)
            }
        }

        val desktopMain by getting {
            dependsOn(jvmSharedMain)
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.coroutines.swing)
                implementation(libs.ktor.client.cio)
            }
        }

        if (isMacOs) {
            val iosMain by creating {
                dependsOn(commonMain)
                dependencies {
                    implementation(libs.ktor.client.darwin)
                }
            }

            val iosArm64Main by getting {
                dependsOn(iosMain)
            }

            val iosSimulatorArm64Main by getting {
                dependsOn(iosMain)
            }
        }
    }
}

android {
    namespace = "com.msa.iotlab"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.msa.iotlab"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspDesktop", libs.room.compiler)

    if (isMacOs) {
        add("kspIosArm64", libs.room.compiler)
        add("kspIosSimulatorArm64", libs.room.compiler)
    }
}

compose.desktop {
    application {
        mainClass = "com.msa.iotlab.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MSA IoT Lab"
            packageVersion = "1.0.0"
        }
    }
}
