plugins {
    `java-library`
}

apply { plugin<AlignPlugin>() }

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.9.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.7.9") {
      isForce = true
    }
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.4.1")
}
