plugins {
    `kotlin-dsl`
}

dependencyLocking {
    lockAllConfigurations()
}

repositories {
    gradlePluginPortal()
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}
