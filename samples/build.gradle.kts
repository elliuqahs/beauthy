plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

dependencies {
    implementation(projects.core)
}

application {
    mainClass.set("io.github.elliuqahs.beauthy.samples.MainKt")
}
