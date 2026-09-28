import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import java.util.Properties

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.binaryCompatibilityValidator)
}

apiValidation {
    ignoredProjects += listOf("samples")
}

// Maven Central credentials and signing keys live in local.properties, which is never committed.
// Keys on the left are read from local.properties and exposed under the names on the right.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val publishingPropertyNames = listOf(
    "signing.keyId" to "signing.keyId",
    "signing.keyName" to "signing.keyId",
    "signing.password" to "signing.password",
    "signing.passphrase" to "signing.password",
    "signing.secretKeyRingFile" to "signing.secretKeyRingFile",
    "mavenCentralUsername" to "mavenCentralUsername",
    "mavenCentralPassword" to "mavenCentralPassword",
)

// Settings shared by every published module. Each module only declares its
// artifactId, name and description.
subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        publishingPropertyNames.forEach { (from, to) ->
            localProperties.getProperty(from)?.let { extra.set(to, it) }
        }

        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
            signAllPublications()

            pom {
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
    }
}
