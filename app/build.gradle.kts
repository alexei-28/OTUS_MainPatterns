plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit)

}

application {
    // Define the main class for the application.
    mainClass.set("ru.otus.main_patterns.App")
}