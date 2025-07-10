plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("io.spring.dependency-management") version "1.1.4"
}

repositories {
    mavenCentral()
}

group = "ru.otus.main_patterns"
version = "1.0.0-SNAPSHOT"

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

extra["guavaVersion"] = "33.0.0-jre"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.3")
    }

}
dependencies {
    implementation("org.slf4j:slf4j-api")
    implementation("com.google.guava:guava:${property("guavaVersion")}")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl")
    runtimeOnly("org.apache.logging.log4j:log4j-core")

    testImplementation("junit:junit")
    testImplementation("org.assertj:assertj-core")
}


tasks.test {
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}