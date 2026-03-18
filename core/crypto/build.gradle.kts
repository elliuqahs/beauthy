import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    `maven-publish`
    signing
}

group = "com.beauthy"
version = "1.0.0"

kotlin {
    explicitApi()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreCrypto"
            isStatic = true
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.maoungedev.beauthy.core.crypto"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

publishing {
    publications.withType<MavenPublication> {
        artifactId = if (artifactId == "crypto") "otp-sdk" else artifactId.replace("crypto", "otp-sdk")

        pom {
            name.set("Beauthy OTP SDK")
            description.set("Kotlin Multiplatform TOTP/HOTP library implementing RFC 6238 and RFC 4226")
            url.set("https://github.com/AhmadShaqworworworworworworwor/Beauthy")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("maoungedev")
                    name.set("Maoungedev")
                }
            }

            scm {
                url.set("https://github.com/AhmadShaqworworworworworworwor/Beauthy")
                connection.set("scm:git:git://github.com/AhmadShaqworworworworworworwor/Beauthy.git")
                developerConnection.set("scm:git:ssh://github.com:AhmadShaqworworworworworworwor/Beauthy.git")
            }
        }
    }
}

signing {
    // Only sign when publishing to Maven Central
    isRequired = gradle.taskGraph.hasTask("publishToMavenCentral")
    sign(publishing.publications)
}
