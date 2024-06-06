package com.pujh.router.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pujh.router.annotations.Destination
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import kotlin.reflect.KClass


class DestinationProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    private val options = environment.options
    private val logger = environment.logger
    private val codeGenerator = environment.codeGenerator

    private fun Resolver.findAnnotations(
        kClass: KClass<*>,
    ) = getSymbolsWithAnnotation(
        kClass.qualifiedName.toString()
    ).filterIsInstance<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val moduleName = options["route_module_name"]!!
        environment.logger.warn("moduleName=$moduleName")

        val rootDir = options["root_project_dir"]!!
        environment.logger.warn("rootDir=$rootDir")

        val destinationClasses = resolver.findAnnotations(Destination::class)
        if (!destinationClasses.iterator().hasNext()) {
            return emptyList()
        }

        generateMappingFile(moduleName, destinationClasses)

        generateJsonFile(rootDir, destinationClasses)

        return emptyList()
    }

    private fun generateMappingFile(
        moduleName: String,
        destinationClasses: Sequence<KSClassDeclaration>
    ) {
        val sourceFiles = destinationClasses.mapNotNull { it.containingFile }

        val classPackage = "com.pujh.router.mapping"
        val className = "RouterMapping_$moduleName"

        val fileSpec = buildMappingFile(
            classPackage,
            className,
            destinationClasses
        )

        val dependencies = Dependencies(
            aggregating = false,
            *sourceFiles.toList().toTypedArray(),
        )
        fileSpec.writeTo(codeGenerator, dependencies)
    }

    private fun buildMappingFile(
        classPackage: String,
        className: String,
        destinationClasses: Sequence<KSClassDeclaration>,
    ): FileSpec = FileSpec
        .builder(classPackage, className)
        .addType(buildMappingClass(className, destinationClasses))
        .build()

    private fun buildMappingClass(
        className: String,
        destinationClasses: Sequence<KSClassDeclaration>
    ): TypeSpec = TypeSpec.objectBuilder(className)
        .addFunction(buildGetMethod(destinationClasses))
        .build()

    @OptIn(KspExperimental::class)
    private fun buildGetMethod(
        destinationClasses: Sequence<KSClassDeclaration>
    ): FunSpec = FunSpec.builder("get")
        .returns(
            Map::class.asClassName().parameterizedBy(
                String::class.asClassName(),
                String::class.asClassName()
            )
        ).apply {
            addStatement("val mapping = mutableMapOf<String, String>()")
            destinationClasses.forEach { destinationClass ->
                val className = destinationClass.qualifiedName?.asString().orEmpty()
                val destination = destinationClass.getAnnotationsByType(Destination::class).single()

                val url = destination.url
                val description = destination.description

                logger.warn("name=$className")
                logger.warn("url=$url")
                logger.warn("description=$description")

                addStatement("mapping[%S] = %S", url, className)
            }
            addStatement("return mapping")
        }.build()


    @OptIn(KspExperimental::class)
    private fun generateJsonFile(
        rootDir: String,
        destinationClasses: Sequence<KSClassDeclaration>
    ) {
        val destinationJsonArray = JsonArray()

        destinationClasses.forEach { destinationClass ->
            val className = destinationClass.qualifiedName?.asString().orEmpty()
            val destination = destinationClass.getAnnotationsByType(Destination::class).single()

            val url = destination.url
            val description = destination.description

            val item = JsonObject()
            item.addProperty("url", url)
            item.addProperty("description", description)
            item.addProperty("realPath", className)

            destinationJsonArray.add(item)
        }

        // 写入JSON到本地文件中

        // 检测父目录是否存在
        val rootDirFile = File(rootDir)
        if (!rootDirFile.exists()) {
            throw RuntimeException("root_project_dir not exist!")
        }

        // 创建 router_mapping 子目录
        val routerFileDir = File(rootDirFile, "router_mapping")
        if (!routerFileDir.exists()) {
            routerFileDir.mkdir()
        }

        val mappingFile = File(
            routerFileDir,
            "mapping_" + System.currentTimeMillis() + ".json"
        )

        // 写入json内容
        BufferedWriter(FileWriter(mappingFile, Charsets.UTF_8)).use {
            val jsonStr = destinationJsonArray.toString()
            it.write(jsonStr)
            it.flush()
        }
    }
}
