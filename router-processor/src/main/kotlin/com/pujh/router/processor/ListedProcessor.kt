package com.pujh.router.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import com.pujh.router.annotations.Listed
import kotlin.reflect.KClass

class ListedProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    private fun Resolver.findAnnotations(
        kClass: KClass<*>,
    ) = getSymbolsWithAnnotation(
        kClass.qualifiedName.toString()
    ).filterIsInstance<KSFunctionDeclaration>().filter {
        it.parameters.isEmpty()
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val listedFunctions: Sequence<KSFunctionDeclaration> =
            resolver.findAnnotations(Listed::class)
        if (!listedFunctions.iterator().hasNext()) return emptyList()
        // gathering the required imports
        val imports = listedFunctions.mapNotNull { it.qualifiedName?.asString() }.toSet()

        // group functions based on their given list-name
        val lists = listedFunctions.groupBy {
            it.annotations.first {
                it.shortName.asString() == "Listed"
            }.arguments.first().value.toString()
        }

        val sourceFiles = listedFunctions.mapNotNull { it.containingFile }

        val fileText = buildString {
            append("package your.desired.packagename")
            newLine()
            newLine()
            imports.forEach {
                append("import $it")
                newLine()
            }
            newLine()
            lists.forEach { (listName, functions) ->
                val functionNames = functions.joinToString(", ") {
                    it.simpleName.asString() + "()"
                }

                append("val $listName = listOf($functionNames)")
                newLine()
            }
        }

        createFileWithText(sourceFiles, fileText)
        return (listedFunctions).filterNot { it.validate() }.toList()
    }

    private fun createFileWithText(
        sourceFiles: Sequence<KSFile>,
        fileText: String,
    ) {
        val file = environment.codeGenerator.createNewFile(
            Dependencies(
                false,
                *sourceFiles.toList().toTypedArray(),
            ),
            "your.generated.file.package",
            "GeneratedLists"
        )

        file.write(fileText.toByteArray())
    }

    private fun StringBuilder.newLine(count: Int = 1) {
        repeat(count) {
            append("\n")
        }
    }
}