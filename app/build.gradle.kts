plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    mavenCentral()
}

group = "ru.otus.main_patterns"
version = "1.0.1"

application {
    mainClass.set("ru.otus.main_patterns.App")
}

tasks.jar {
    archiveBaseName.set("main_patterns")
}

distributions {
    main {
        distributionBaseName.set("main_patterns")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val junitVersion = "5.10.0"
val slf4jVersion = "2.0.7"
val log4jVersion = "2.20.0"
val assertJVersion = "3.27.3"

dependencies {
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-core:$log4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
