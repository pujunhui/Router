package com.pujh.router.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.pujh.router.annotations.GenerateInterface
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class GenerateInterfaceProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getSymbolsWithAnnotation(
                GenerateInterface::class.qualifiedName!!
            )
            .filterIsInstance<KSClassDeclaration>()
            .forEach(::generateInterface)

        return emptyList()
    }

    @OptIn(KspExperimental::class)
    private fun generateInterface(annotatedClass: KSClassDeclaration) {
        val interfaceName = annotatedClass
            .getAnnotationsByType(GenerateInterface::class)
            .single()
            .name
        val interfacePackage = annotatedClass
            .qualifiedName
            ?.getQualifier()
            .orEmpty()
        val publicMethods = annotatedClass
            .getDeclaredFunctions()
            .filter { it.isPublic() && !it.isConstructor() }

        val fileSpec = buildInterfaceFile(
            interfacePackage,
            interfaceName,
            publicMethods
        )
        val dependencies = Dependencies(
            aggregating = false,
            annotatedClass.containingFile!!
        )
        fileSpec.writeTo(codeGenerator, dependencies)
    }

    private fun buildInterfaceFile(
        interfacePackage: String,
        interfaceName: String,
        publicMethods: Sequence<KSFunctionDeclaration>,
    ): FileSpec = FileSpec
        .builder(interfacePackage, interfaceName)
        .addType(buildInterface(interfaceName, publicMethods))
        .build()

    private fun buildInterface(
        interfaceName: String,
        publicMethods: Sequence<KSFunctionDeclaration>,
    ): TypeSpec = TypeSpec
        .interfaceBuilder(interfaceName)
        .addFunctions(
            publicMethods
                .map(::buildInterfaceMethod).toList()
        )
        .build()

    private fun buildInterfaceMethod(
        function: KSFunctionDeclaration,
    ): FunSpec = FunSpec
        .builder(function.simpleName.getShortName())
        .addModifiers(buildFunctionModifiers(function.modifiers))
        .addParameters(
            function.parameters.map(::buildInterfaceMethodParameter)
        )
        .returns(function.returnType!!.toTypeName())
        .addAnnotations(
            function.annotations
                .map { it.toAnnotationSpec() }
                .toList()
        )
        .build()

    private fun buildInterfaceMethodParameter(
        variableElement: KSValueParameter,
    ): ParameterSpec = ParameterSpec
        .builder(
            variableElement.name!!.getShortName(),
            variableElement.type.toTypeName(),
        )
        .addAnnotations(
            variableElement.annotations
                .map { it.toAnnotationSpec() }.toList()
        )
        .build()

    private fun buildFunctionModifiers(
        modifiers: Set<Modifier>
    ) = modifiers
        .filterNot { it in IGNORED_MODIFIERS }
        .plus(Modifier.ABSTRACT)
        .mapNotNull { it.toKModifier() }

    companion object {
        val IGNORED_MODIFIERS =
            listOf(Modifier.OPEN, Modifier.OVERRIDE)
    }


    fun classWithParents(
        classDeclaration: KSClassDeclaration
    ): List<KSClassDeclaration> =
        classDeclaration.superTypes
            .map { it.resolve().declaration }
            .filterIsInstance<KSClassDeclaration>()
            .flatMap { classWithParents(it) }
            .toList()
            .plus(classDeclaration)
}