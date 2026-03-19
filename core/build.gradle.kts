import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProps = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localProps.load(localFile.inputStream())
    // Map local.properties keys to Gradle signing plugin expected keys
    localProps.getProperty("signing.keyId")?.let { project.ext.set("signing.keyId", it) }
    localProps.getProperty("signing.keyName")?.let { project.ext.set("signing.keyId", it) }
    localProps.getProperty("signing.password")?.let { project.ext.set("signing.password", it) }
    localProps.getProperty("signing.passphrase")?.let { project.ext.set("signing.password", it) }
    localProps.getProperty("signing.secretKeyRingFile")?.let { project.ext.set("signing.secretKeyRingFile", it) }
    localProps.getProperty("mavenCentralUsername")?.let { project.ext.set("mavenCentralUsername", it) }
    localProps.getProperty("mavenCentralPassword")?.let { project.ext.set("mavenCentralPassword", it) }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.elliuqahs"
version = "0.1.0"

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
    namespace = "io.github.elliuqahs.beauthy.sdk"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("io.github.elliuqahs", "beauthy-sdk", "0.1.0")

    configure(KotlinMultiplatform(javadocJar = com.vanniktech.maven.publish.JavadocJar.Empty()))

    pom {
        name.set("Beauthy SDK")
        description.set("Lightweight Kotlin Multiplatform library for generating TOTP and HOTP one-time passwords")
        url.set("https://github.com/elliuqahs/beauthy")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("elliuqahs")
                name.set("elliuqahs")
                email.set("shaquillerizkirn@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/elliuqahs/beauthy")
            connection.set("scm:git:git://github.com/elliuqahs/beauthy.git")
            developerConnection.set("scm:git:ssh://github.com:elliuqahs/beauthy.git")
        }
    }
}
