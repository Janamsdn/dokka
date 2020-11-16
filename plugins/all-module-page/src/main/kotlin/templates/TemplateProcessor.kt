package org.jetbrains.dokka.allModulesPage.templates

import kotlinx.coroutines.*
import org.jetbrains.dokka.allModulesPage.AllModulesPagePlugin
import org.jetbrains.dokka.base.templating.Command
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.query
import org.jsoup.nodes.Element
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.coroutines.coroutineContext

interface TemplateProcessor {
    fun process()
}

interface TemplateProcessingStrategy {
    suspend fun process(input: File, output: File): Boolean
    suspend fun finish(output: File) {}
}

class DefaultTemplateProcessor(
    private val context: DokkaContext,
): TemplateProcessor {

    private val strategies: List<TemplateProcessingStrategy> = context.plugin<AllModulesPagePlugin>().query { templateProcessingStrategy }

    override fun process() = runBlocking(Dispatchers.Default) {
        coroutineScope {
            context.configuration.modules.forEach {
                launch {
                    it.sourceOutputDirectory.visit(context.configuration.outputDir.resolve(it.relativePathToOutputDirectory))
                }
            }
        }
        strategies.map { it.finish(context.configuration.outputDir) }
        Unit
    }

    private suspend fun File.visit(target: File): Unit = coroutineScope {
        val source = this@visit
        if (source.isDirectory) {
           target.mkdir()
           source.list()?.forEach {
               launch { source.resolve(it).visit(target.resolve(it)) }
           }
        } else {
            strategies.asSequence().first { it.process(source, target) }
        }
    }
}

data class TemplatingContext<out T: Command>(
    val input: File,
    val output: File,
    val element: Element,
    val command: T,
)