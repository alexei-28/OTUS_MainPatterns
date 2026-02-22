package ru.otus.main_patterns.hw06.codegen;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes("com.example.processor.GenerateHello")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class HelloProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (roundEnv.processingOver()) return false;

        MethodSpec method = MethodSpec.methodBuilder("sayHello")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addStatement("$T.out.println($S)", System.class, "Hello from generated class!")
                .build();

        TypeSpec clazz = TypeSpec.classBuilder("HelloGenerated")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(method)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.generated", clazz).build();

        try {
            javaFile.writeTo(processingEnv.getFiler()); // ← КЛЮЧЕВОЙ МОМЕНТ
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}