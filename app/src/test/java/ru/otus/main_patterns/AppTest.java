package ru.otus.main_patterns;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {

    @Test
    public void should_java_version_is_8() {
        String actualJavaVersion = System.getProperty("java.version");
        assertThat(actualJavaVersion).startsWith("1.8");
    }

}