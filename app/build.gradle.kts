plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    mavenCentral()
}

group = "ru.otus.main_patterns"
version = "1.0.4"

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

val beanUtilsVersion = "1.11.0"
val fasterxml = "2.19.2"
val guavaTableVersion = "33.0.0-jre"
val log4jVersion = "2.20.0"
val awaitilityVersion = "4.3.0"
val mockitoVersion = "4.11.0"


dependencies {
    // Import the Spring Boot BOM using the platform() function (compatible Java 8)
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.7.18"))

    // Declare dependencies without specifying versions
    implementation("org.slf4j:slf4j-api")

    // Declare dependencies without specifying versions
    implementation("com.google.guava:guava:$guavaTableVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$fasterxml")
    implementation("commons-beanutils:commons-beanutils:$beanUtilsVersion")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-core")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.awaitility:awaitility:$awaitilityVersion")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
