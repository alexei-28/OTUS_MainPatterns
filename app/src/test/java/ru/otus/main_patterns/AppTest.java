package ru.otus.main_patterns;


import org.junit.Test;

import static org.junit.Assert.*;

/*-
Run a specific test class:

  ./gradlew test --tests "ru.otus.main_patterns.AppTest"

*/
public class AppTest {

    @Test
    public void should_java_version_is_8() {
        String javaVersion = System.getProperty("java.version");
        assertNotNull("java.version system property should not be null", javaVersion);
        assertTrue("Java version should be 8", javaVersion.startsWith("1.8"));
    }
}
