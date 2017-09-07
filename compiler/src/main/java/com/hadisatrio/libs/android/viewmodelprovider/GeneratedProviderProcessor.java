/*
 *    Copyright (C) 2017 Hadi Satrio
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.hadisatrio.libs.android.viewmodelprovider;

import com.google.auto.service.AutoService;
import com.hadisatrio.libs.android.viewmodelprovider.internal.Pair;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public final class GeneratedProviderProcessor extends AbstractProcessor {

    private static final String VIEW_MODEL_CLASS_NAME = "android.arch.lifecycle.ViewModel";
    private static final String VIEW_MODEL_FACTORY_CLASS_NAME = "android.arch.lifecycle.ViewModelProvider.Factory";
    private static final String VIEW_MODEL_PROVIDERS_CLASS_NAME = "android.arch.lifecycle.ViewModelProviders";
    private static final String FRAGMENT_ACTIVITY_CLASS_NAME = "android.support.v4.app.FragmentActivity";
    private static final String FRAGMENT_CLASS_NAME = "android.support.v4.app.Fragment";

    private static final String PROVIDER_CLASS_SUFFIX = "Provider";
    private static final String FACTORY_CLASS_SUFFIX = "Factory";
    private static final String PARAMS_PREFIX = "p";
    private static final String VARIABLE_PREFIX = "var";

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotations = new LinkedHashSet<>();
        annotations.add(GeneratedProvider.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(GeneratedProvider.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(
                        annotatedElement,
                        "Only classes can be annotated with @%s.",
                        GeneratedProvider.class.getSimpleName()
                );
                return true; // Error message printed, exit processing.
            }

            // We can cast it, because we know that it of ElementKind.CLASS.
            final TypeElement typeElement = (TypeElement) annotatedElement;

            // Check whether or not this class meets our pre-requisites.
            try {
                if (!isValidClass(typeElement)) {
                    return true; // Error message printed, exit processing.
                }
            } catch (ClassNotFoundException e) {
                error(
                        "Couldn't find the required classes. "
                                + "Have you declare Android Architecture Components libraries as "
                                + "your project's dependency?"
                );
                return true; // Error message printed, exit processing.
            }

            try {
                generateFactory(typeElement);
            } catch (IOException | ClassNotFoundException | NoPackageNameException e) {
                error("Error while generating factory for class %s. Cause: %s.", typeElement, e);
                return true; // Error message printed, exit processing.
            } catch (DuplicateMainConstructorException e) {
                error(
                        annotatedElement,
                        "Only one constructor can be annotated with @%s in a given class.",
                        Main.class.getSimpleName()
                );
                return true; // Error message printed, exit processing.
            }

            try {
                generateProvider(typeElement);
            } catch (IOException | ClassNotFoundException | NoPackageNameException e) {
                error("Error while generating provider for class %s. Cause: %s.", typeElement, e);
                return true; // Error message printed, exit processing.
            } catch (DuplicateMainConstructorException e) {
                error(
                        annotatedElement,
                        "Only one constructor can be annotated with @%s in a given class.",
                        Main.class.getSimpleName()
                );
                return true; // Error message printed, exit processing.
            }
        }

        // We have finished processing.
        return true;
    }

    private void error(Element e, String message, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(message, args),
                e
        );
    }

    private void error(String message, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(message, args)
        );
    }

    private boolean isValidClass(TypeElement classElement) throws ClassNotFoundException {

        /* Req #1: Annotated class has to be public. */
        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(classElement, "The class %s is not public.",
                    classElement.getQualifiedName().toString());
            return false;
        }

        /* Req #2: Annotated class has to be concrete (not abstract). */
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(classElement, "The class %s is abstract. You can't annotate abstract classes with @%.",
                    classElement.getQualifiedName().toString(), GeneratedProvider.class.getSimpleName());
            return false;
        }

        /* Req #3: Annotated class has to be a subclass of ViewModel. */
        TypeElement currentClass = classElement;
        while (true) {
            final TypeMirror superClassType = currentClass.getSuperclass();

            if (superClassType.getKind() == TypeKind.NONE) {
                // Basis class (java.lang.Object) reached, so exit.
                error(classElement, "The class %s annotated with @%s must inherit from %s.",
                        classElement.getQualifiedName().toString(), GeneratedProvider.class.getSimpleName(),
                        Class.forName(VIEW_MODEL_CLASS_NAME));
                return false;
            }

            if (superClassType.toString().equals(VIEW_MODEL_CLASS_NAME)) {
                // Required super class found. Break the loop so we can return.
                break;
            }

            // Moving up in inheritance tree.
            currentClass = (TypeElement) typeUtils.asElement(superClassType);
        }

        return true;
    }

    private void generateFactory(TypeElement typeElement)
            throws IOException, ClassNotFoundException, NoPackageNameException, DuplicateMainConstructorException {

        final String packageName = getPackageName(typeElement);
        final String genClassName = typeElement.getSimpleName() + FACTORY_CLASS_SUFFIX;
        final List<Pair<TypeMirror, String>> ctorParams = getConstructorParameters(typeElement);

        // Define the fields based on previously queried constructor params.
        final List<FieldSpec> fieldSpecs = new ArrayList<>();
        final StringBuilder fieldTypesCsv = new StringBuilder();
        final StringBuilder fieldNamesCsv = new StringBuilder();
        for (int i = 0; i < ctorParams.size(); i++) {
            TypeName paramType = TypeName.get(ctorParams.get(i).getLeft());

            // Type-erasure. Not doing this will break factory-generation for
            // targets with parameterized type constructor params.
            if (paramType instanceof ParameterizedTypeName) {
                paramType = ((ParameterizedTypeName) paramType).rawType;
            }

            fieldSpecs.add(
                    FieldSpec.builder(
                            paramType,
                            VARIABLE_PREFIX + i,
                            Modifier.PRIVATE,
                            Modifier.FINAL
                    ).build()
            );

            if (fieldTypesCsv.length() > 0) fieldTypesCsv.append(',');
            fieldTypesCsv.append(paramType).append(".class");

            if (fieldNamesCsv.length() > 0) fieldNamesCsv.append(',');
            fieldNamesCsv.append(VARIABLE_PREFIX).append(i);
        }

        // Define the constructor of the generated class.
        final MethodSpec.Builder ctorSpecBuilder = MethodSpec.constructorBuilder();
        for (int i = 0; i < ctorParams.size(); i++) {
            ctorSpecBuilder.addParameter(
                    TypeName.get(ctorParams.get(i).getLeft()),
                    PARAMS_PREFIX + i
            );

            // Create statement to assign parameter value to its appropriate field.
            ctorSpecBuilder.addStatement(
                    "this.$L = $L",
                    VARIABLE_PREFIX + i,
                    PARAMS_PREFIX + i
            );
        }
        final MethodSpec ctorSpec = ctorSpecBuilder.build();

        // Define the `create()` method that will be called by ViewModelProviders
        // to actually instantiate the ViewModel.
        final TypeVariableName typeVariableName = TypeVariableName.get("T", Class.forName(VIEW_MODEL_CLASS_NAME));
        final MethodSpec createSpec = MethodSpec.methodBuilder("create")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(typeVariableName)
                .returns(typeVariableName)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), typeVariableName), "modelClass")
                .beginControlFlow("if ($T.class.isAssignableFrom($L))", typeElement, "modelClass")
                .beginControlFlow("try")
                .addStatement("return $L.getConstructor($L).newInstance($L)", "modelClass", fieldTypesCsv, fieldNamesCsv)
                .nextControlFlow("catch ($T | $T | $T | $T e)", NoSuchMethodException.class, IllegalAccessException.class, InstantiationException.class, InvocationTargetException.class)
                .addStatement("throw new $T(\"Couldn't create an instance of $T\", e)", RuntimeException.class, typeElement)
                .endControlFlow()
                .nextControlFlow("else")
                .addStatement("throw new $T(\"Couldn't create an instance of $T\")", RuntimeException.class, typeElement)
                .endControlFlow()
                .build();

        // Define the class using the previously defined specs.
        final TypeSpec factory = TypeSpec.classBuilder(genClassName)
                .addSuperinterface(ClassName.bestGuess(VIEW_MODEL_FACTORY_CLASS_NAME))
                .addModifiers(Modifier.FINAL)
                .addFields(fieldSpecs)
                .addMethod(ctorSpec)
                .addMethod(createSpec)
                .build();

        // Write the class.
        JavaFile.builder(packageName, factory)
                .build().writeTo(filer);
    }

    private String getPackageName(TypeElement typeElement) throws NoPackageNameException {
        final PackageElement pkg = elementUtils.getPackageOf(typeElement);
        if (pkg.isUnnamed()) {
            throw new NoPackageNameException(typeElement);
        }
        return pkg.getQualifiedName().toString();
    }

    private List<Pair<TypeMirror, String>> getConstructorParameters(TypeElement typeElement)
            throws DuplicateMainConstructorException {
        final List<Pair<TypeMirror, String>> subjectCtorParams = new ArrayList<>();

        ExecutableElement mainCtor = null;
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                if (mainCtor == null) {
                    mainCtor = ((ExecutableElement) element);
                }
                if (element.getAnnotation(Main.class) != null) {
                    if (mainCtor.getAnnotation(Main.class) != null) {
                        throw new DuplicateMainConstructorException();
                    }
                    mainCtor = ((ExecutableElement) element);
                }
            }
        }

        if (mainCtor != null) {
            for (VariableElement ctorParameter : mainCtor.getParameters()) {
                subjectCtorParams.add(new Pair<>(ctorParameter.asType(), ctorParameter.getSimpleName().toString()));
            }
        }

        return subjectCtorParams;
    }

    private void generateProvider(TypeElement typeElement)
            throws IOException, ClassNotFoundException, NoPackageNameException, DuplicateMainConstructorException {

        final String packageName = getPackageName(typeElement);
        final TypeName typeName = TypeName.get(typeElement.asType());
        final String genClassName = typeElement.getSimpleName() + PROVIDER_CLASS_SUFFIX;
        final List<Pair<TypeMirror, String>> subjectCtorParams = getConstructorParameters(typeElement);
        final Class viewModelProviderClass = Class.forName(VIEW_MODEL_PROVIDERS_CLASS_NAME);

        final StringBuilder ctorParamNamesCsv = new StringBuilder();
        for (int i = 0; i < subjectCtorParams.size(); i++) {
            if (ctorParamNamesCsv.length() > 0) ctorParamNamesCsv.append(',');
            ctorParamNamesCsv.append(subjectCtorParams.get(i).getRight());
        }

        // Generate `get()` method to be called from activities.
        final MethodSpec.Builder activityGetBuilder = MethodSpec.methodBuilder("get")
                .returns(typeName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.bestGuess(FRAGMENT_ACTIVITY_CLASS_NAME), "activity");
        for (int i = 0; i < subjectCtorParams.size(); i++) {
            activityGetBuilder.addParameter(TypeName.get(subjectCtorParams.get(i).getLeft()), subjectCtorParams.get(i).getRight());
        }
        activityGetBuilder.addStatement("return $T.of(activity, new $TFactory($L)).get($T.class)", viewModelProviderClass, typeElement, ctorParamNamesCsv, typeElement);
        final MethodSpec activityGet = activityGetBuilder.build();

        // Generate `get()` method to be called from fragments.
        final MethodSpec.Builder fragmentGetBuilder = MethodSpec.methodBuilder("get")
                .returns(typeName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.bestGuess(FRAGMENT_CLASS_NAME), "fragment");
        for (int i = 0; i < subjectCtorParams.size(); i++) {
            fragmentGetBuilder.addParameter(TypeName.get(subjectCtorParams.get(i).getLeft()), subjectCtorParams.get(i).getRight());
        }
        fragmentGetBuilder.addStatement("return $T.of(fragment, new $TFactory($L)).get($T.class)", viewModelProviderClass, typeElement, ctorParamNamesCsv, typeElement);
        final MethodSpec fragmentGet = fragmentGetBuilder.build();

        // Define the class using the previously defined specs.
        final TypeSpec provider = TypeSpec.classBuilder(genClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(activityGet)
                .addMethod(fragmentGet)
                .build();

        // Write the class.
        JavaFile.builder(packageName, provider)
                .build().writeTo(filer);
    }
}
