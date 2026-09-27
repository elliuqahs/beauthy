plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

dependencies {
    implementation(projects.core)
    implementation(projects.coroutines)
}

application {
    mainClass.set("io.github.elliuqahs.beauthy.samples.MainKt")
}
