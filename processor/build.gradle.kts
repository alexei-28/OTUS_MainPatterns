plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val log4jVersion = "2.20.0"

dependencies {
    implementation("com.squareup:javapoet:1.13.0")
}