plugins {
    id("java")
    id("application")
    id("com.diffplug.spotless") version "6.13.0"
}

group = "ru.otus.main_patterns.hw10.authserver"
version = "1.10.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("ru.otus.main_patterns.hw10.authserver.Main")
}

val awaitilityVersion = "4.3.0"
val fasterxml = "2.19.2"
val log4jVersion = "2.20.0"
val mockitoVersion = "4.11.0"
val datafakerVersion = "1.9.0"
val freemarkerVersion = "1.8.2"

dependencies {
    // Import the Spring Boot BOM using the platform() function (compatible Java 8)
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.7.18"))

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-databind:$fasterxml")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$fasterxml")
    implementation("org.eclipse.jetty:jetty-server")
    implementation("org.eclipse.jetty:jetty-servlet")
    implementation("org.slf4j:slf4j-api")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-core")

    testImplementation("net.datafaker:datafaker:$datafakerVersion")
    testImplementation("org.freemarker:freemarker:$freemarkerVersion")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.awaitility:awaitility:$awaitilityVersion")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
}

// FAT jar
tasks.jar {
    manifest {
        attributes["Main-Class"] = "ru.otus.main_patterns.hw10.authserver.Main"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from({
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    })
}

tasks.test {
    useJUnitPlatform()
}

// Format code
spotless {
    java {
        target("src/**/*.java")
        // Версия 1.7 — последняя с поддержкой Java 8
        googleJavaFormat("1.7")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}
