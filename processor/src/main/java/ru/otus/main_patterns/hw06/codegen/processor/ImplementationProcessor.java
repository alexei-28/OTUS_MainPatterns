package ru.otus.main_patterns.hw06.codegen.processor;

import com.squareup.javapoet.*;
import ru.otus.main_patterns.hw06.codegen.annotations.GenerateImpl;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;

/*
  How debug:
     1. Run in terminal
        ./gradlew clean assemble -Dorg.gradle.debug=true --no-daemon
     2. В IntelliJ IDEA:
        -Нажмите Add Configuration (сверху рядом с кнопкой Run).
        -Выберите Remote JVM Debug.
        -Настройки оставьте по умолчанию (localhost:5005).
        -Нажмите Debug (жучок).
     3. Поставьте Breakpoint внутри метода, например ru.otus.main_patterns.hw06.codegen.processor.HelloProcessor#process.
        Как только вы нажмете Debug в IDEA, сборка продолжится и остановится на вашей точке.
 */

@SupportedAnnotationTypes("ru.otus.main_patterns.hw06.codegen.annotations.GenerateImpl")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ImplementationProcessor extends AbstractProcessor {
    private static final String PACKAGE = "ru.otus.main_patterns.hw06";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateImpl.class)) {
            // проверка что это интерфейс
            if (element.getKind() != ElementKind.INTERFACE) {
                error("Only interfaces allowed", element);
                continue;
            }

            TypeElement interfaceElement = (TypeElement) element;
            String interfaceName = interfaceElement.getSimpleName().toString();
            String implName = interfaceName + "Impl";
            MethodSpec constructor = createConstructor();
            FieldSpec fieldUObject = generateFieldUObject();

            // Class
            TypeSpec.Builder classBuilder =
                    TypeSpec.classBuilder(implName)
                            .addModifiers(Modifier.PUBLIC)
                            .addField(fieldUObject)
                            .addMethod(constructor)
                            .addSuperinterface(TypeName.get(interfaceElement.asType()));

            // все методы интерфейса
            List<ExecutableElement> methods = ElementFilter.methodsIn(interfaceElement.getEnclosedElements());
            for (ExecutableElement method : methods) {
                if ("getPosition".equals(method.getSimpleName().toString())) {
                    classBuilder.addMethod(generateMethodGetPosition(method));
                } else  if ("setPosition".equals(method.getSimpleName().toString())) {
                    classBuilder.addMethod(generateMethodSetPosition(method));
                } else if ("getVelocity".equals(method.getSimpleName().toString())) {
                    classBuilder.addMethod(generateGetVelocity(method));
                } else if ("finish".equals(method.getSimpleName().toString())) {
                    classBuilder.addMethod(generateFinishMethod());
                } else {
                    System.out.println("Unimplement method: " + method.getSimpleName());
                }
            }

            // ru.otus.main_patterns.hw06.impl.generated
            String targetPackage = getTargetPackageName(interfaceElement);
            JavaFile javaFile = JavaFile.builder(targetPackage, classBuilder.build())
                    .build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private String getTargetPackageName(TypeElement interfaceElement) {
        String originalPackage = processingEnv.getElementUtils()
                .getPackageOf(interfaceElement)
                .getQualifiedName()
                .toString();
        // Удаляем последнюю часть, если нужно (например, если интерфейс в .interfaces, а мы хотим в .impl)
        String basePackage =  originalPackage.contains(".")
                ? originalPackage.substring(0, originalPackage.lastIndexOf('.'))
                : originalPackage;
        return basePackage +  ".generated.impl";
    }

    private FieldSpec generateFieldUObject() {
        ClassName uObjectType =
                ClassName.get(PACKAGE +".interfaces", "UObject");
        return FieldSpec.builder(uObjectType, "obj")
                .addModifiers(Modifier.PRIVATE)
                .addModifiers(Modifier.FINAL)
                .build();
    }

    private MethodSpec createConstructor() {
        ClassName uObjectType =
                ClassName.get(PACKAGE + ".interfaces", "UObject");
        ParameterSpec objParam =
                ParameterSpec.builder(uObjectType, "obj")
                        .build();
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(objParam)
                .addStatement("this.$N = $N", "obj", "obj")
                .build();
    }

    private MethodSpec generateMethodGetPosition(ExecutableElement method) {
        ClassName iocClass = ClassName.get(PACKAGE, "IoC");
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder(method.getSimpleName().toString())
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.get(method.getReturnType()));
        builder.addStatement(
                "return $T.resolve($S, obj)",
                iocClass,
                "Movable:position.get");
        return builder.build();
    }

    private MethodSpec generateMethodSetPosition(ExecutableElement method) {
        ClassName iocClass = ClassName.get(PACKAGE, "IoC");
        ClassName commandClass = ClassName.get(PACKAGE + ".interfaces", "Command");
        TypeName returnType = TypeName.get(method.getReturnType());

        ParameterSpec parameter = ParameterSpec.builder(
                TypeName.get(method.getParameters().get(0).asType()),
                "newValue"
        ).build();

        return MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addParameter(parameter)
                .addStatement(
                        "$T.<$T>resolve($S, obj, newValue).execute()",
                        iocClass,
                        commandClass,
                        "Movable:position.set"
                )
                .build();
    }

    private MethodSpec generateGetVelocity(ExecutableElement method) {
        ClassName iocClass = ClassName.get(PACKAGE, "IoC");
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder(method.getSimpleName().toString())
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.get(method.getReturnType()));
        builder.addStatement(
                "return $T.resolve($S, obj)",
                iocClass,
                "Movable:velocity.get");
        return builder.build();
    }

    private MethodSpec generateFinishMethod() {
        ClassName iocClass = ClassName.get(PACKAGE, "IoC");
        ClassName commandClass = ClassName.get(PACKAGE + ".interfaces", "Command");
        return MethodSpec.methodBuilder("finish")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addStatement("$T.<$T>resolve($S, obj).execute()",
                        iocClass,
                        commandClass,
                        "Movable:finish")
                .build();
    }

    private void error(String msg, Element e) {
        processingEnv.getMessager()
                .printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}