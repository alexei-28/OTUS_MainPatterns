plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.13")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.20.0")
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<Test> {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        events("started", "skipped", "passed", "failed", "standardOut", "standardError")
        showStandardStreams = true
    }
}

application {
    // Define the main class for the application.
    mainClass.set("ru.otus.main_patterns.App")
}