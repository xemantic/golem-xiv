/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2025  Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.ai.golem.core.script

import com.xemantic.ai.golem.api.backend.script.ExecuteGolemScript
import com.xemantic.ai.golem.core.kotlin.startsWithAnyOf
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.reflect.KClass
import kotlin.script.experimental.api.CompiledScript
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.time.measureTimedValue

@OptIn(ExperimentalAtomicApi::class)
class GolemScriptExecutor {

    class Dependency<T : Any>(
        val name: String,
        val type: KClass<T>,
        val value: T
    )

    private val logger = KotlinLogging.logger {}

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val scriptIdSeq = AtomicInt(1)

    private val scriptingHost = BasicJvmScriptingHost()

    private val compilationConfigurationTemplate = ScriptCompilationConfiguration {
        jvm {
            dependenciesFromCurrentContext(
                wholeClasspath = true
            )
        }
        // Add helpful default imports
        defaultImports(
            "kotlinx.coroutines.async"
        )
        // TODO can be removed once the time API is stable?
        // "-Xmulti-dollar-interpolation" probably not needed anymore
        compilerOptions.append(
            "-opt-in=kotlin.time.ExperimentalTime",
        )
    }

    suspend fun execute(
        @Language("kotlin") script: String,
        dependencies: List<Dependency<*>> = emptyList(),
    ): ExecuteGolemScript.Result {

        val scriptId = scriptIdSeq.fetchAndIncrement()
        logger.debug { "GolemScript[$scriptId]: Executing" }

        val (result, duration) = measureTimedValue {
            GolemScriptHandler(
                scriptId,
                scope,
                dependencies,
                script
            ).handle()
        }

        logger.debug { "GolemScript[$scriptId]: Execution took $duration in total" }

        return result
    }

    fun close() {
        logger.debug { "Closing GolemScriptExecutor" }
        runBlocking {
            scope.coroutineContext.job.children.forEach {
                it.join()
            }
        }
        scope.cancel()
        runBlocking {
            scope.coroutineContext.job.join()
        }
        logger.debug { "GolemScriptExecutor closed" }
    }

    private inner class GolemScriptHandler(
        private val scriptId: Int,
        scope: CoroutineScope,
        dependencies: List<Dependency<*>>,
        script: String
    ) {

        private val allDependencies = dependencies + Dependency(
            name = "_golemScriptScope",
            type = CoroutineScope::class,
            value = scope
        )

        private val scriptParts = splitScriptImportsAndBody(script)

        private val suspendableScript = if (scriptParts.imports.isNotEmpty()) {
            scriptParts.imports.joinToString(separator = "\n") + "\n"
        } else {
            ""
        } + "_golemScriptScope.async<Any?> {\n${scriptParts.body.joinToString(separator = "\n")}\n}"

        suspend fun handle(): ExecuteGolemScript.Result {
            val result = when (val compilationResult = compile()) {
                is ResultWithDiagnostics.Success -> {
                    val compiledScript = compilationResult.value
                    evaluate(compiledScript)
                }
                is ResultWithDiagnostics.Failure -> {
                    val failureMessage = errorReporter().toFailureMessage(
                        phase = ExecuteGolemScript.ExecutionPhase.COMPILATION,
                        failure = compilationResult
                    )
                    logger.debug {
                        "GolemScript[$scriptId]: Compilation (1/2) failure message: $failureMessage"
                    }
                    ExecuteGolemScript.Result.Error(failureMessage)
                }
            }

            return result
        }

        private suspend fun compile(): ResultWithDiagnostics<CompiledScript>  {

            logger.debug { "GolemScript[$scriptId]: Compiling (1/2)" }

            val (compilationResult, duration) = measureTimedValue {
                scriptingHost.compiler(
                    script = suspendableScript.toScriptSource(),
                    scriptCompilationConfiguration = compilationConfig()
                )
            }

            when (compilationResult) {
                is ResultWithDiagnostics.Success -> logger.debug {
                    "GolemScript[$scriptId]: Compilation (1/2) succeeded after $duration"
                }
                is ResultWithDiagnostics.Failure -> logger.debug {
                    "GolemScript[$scriptId]: Compilation (1/2) failed after $duration"
                }
            }

            return compilationResult
        }

        private suspend fun evaluate(
            compiledScript: CompiledScript
        ): ExecuteGolemScript.Result {

            logger.debug { "GolemScript[$scriptId]: Evaluating (2/2)" }

            val (evaluationResult, duration) = measureTimedValue {
                scriptingHost.evaluator(
                    compiledScript = compiledScript,
                    scriptEvaluationConfiguration = evaluationConfig()
                )
            }

            val result = when (evaluationResult) {
                is ResultWithDiagnostics.Success<EvaluationResult> -> {
                    evaluationResult.value.returnValue.toGolemScriptResult()
                }
                is ResultWithDiagnostics.Failure -> {
                    // in practice, this never happens
                    val failureMessage = errorReporter().toFailureMessage(
                        phase = ExecuteGolemScript.ExecutionPhase.EVALUATION,
                        failure = evaluationResult
                    )
                    ExecuteGolemScript.Result.Error(failureMessage)
                }
            }

            when (result) {
                is ExecuteGolemScript.Result.Value -> logger.debug {
                    "GolemScript[$scriptId]: Evaluation (2/2) succeeded after $duration"
                }
                is ExecuteGolemScript.Result.Error -> logger.debug {
                    "GolemScript[$scriptId]: Evaluation (2/2) failed after $duration"
                }
            }

            return result
        }

        private suspend fun ResultValue.toGolemScriptResult() = when (this) {
            is ResultValue.Value -> {
                try {
                    ExecuteGolemScript.Result.Value(
                        (value as Deferred<Any?>).await()
                    )
                } catch (e: Exception) {
                    logger.debug(e) {
                        "GolemScript[$scriptId]: Evaluation failed with error"
                    }
                    val cause = e.cause
                    val throwable = if (
                        (cause != null)
                        && (cause::class == e::class)
                        && (cause.message == cause.message)
                    ) {
                        cause
                    } else {
                        e
                    }
                    val failureMessage = errorReporter().toFailureMessage(throwable)
                    ExecuteGolemScript.Result.Error(
                        failureMessage
                    )
                }
            }
            is ResultValue.Unit -> ExecuteGolemScript.Result.Value(Unit)
            is ResultValue.Error -> {
                val failureMessage = errorReporter().toFailureMessage(error)
                ExecuteGolemScript.Result.Error(
                    message = failureMessage
                )
            }
            is ResultValue.NotEvaluated -> throw IllegalStateException(
                "Should never happen"
            )
        }

        private fun evaluationConfig() = ScriptEvaluationConfiguration {
            providedProperties(
                *(allDependencies.map { it.name to it.value }.toTypedArray())
            )
        }

        private fun compilationConfig() = ScriptCompilationConfiguration(compilationConfigurationTemplate) {
            providedProperties(
                *(allDependencies.map { it.name to KotlinType(it.type)}.toTypedArray())
            )
        }

        private fun errorReporter() = GolemScriptErrorReporter(
            scriptLines = suspendableScript.lines(),
            lastImportLine = scriptParts.imports.size
        )

    }

}

private fun Iterable<ScriptDiagnostic>.filterNonIgnorable() = filterNot {
    it.severity == ScriptDiagnostic.Severity.DEBUG && it.message.startsWithAnyOf(
        "Using JDK home inferred from java.home",
        "Loading modules:"
    )
}

private class GolemScriptErrorReporter(
    private val scriptLines: List<String>,
    private val lastImportLine: Int
) {

    fun toFailureMessage(
        phase: ExecuteGolemScript.ExecutionPhase,
        failure: ResultWithDiagnostics.Failure
    ) = buildString {
        append("<golem:impediment phase=\"${phase.name}\">\n")
        failure.reports.filterNonIgnorable().forEach {
            appendScriptDiagnostic(it)
        }
        append("</golem:impediment>\n")
    }

    fun toFailureMessage(
        throwable: Throwable
    ) = buildString {
        append("<golem:impediment phase=\"${ExecuteGolemScript.ExecutionPhase.EVALUATION.name}\">\n")
        appendException(error = throwable)
        append("</golem:impediment>\n")
    }

    private fun StringBuilder.appendException(
        prefix: String = "",
        error: Throwable
    ) {
        append(prefix)
        append(error::class.qualifiedName)
        error.message?.let {
            append(": ")
            append(it)
        }
        append("\n")

        error.stackTrace.forEachIndexed { index, trace ->

            val isScriptTrace = trace.className.startsWith(
                "Script"
            ) && (trace.fileName == "script.kts")

            if (isScriptTrace || index == 0) {
                val className = if (isScriptTrace) {
                    "Script"
                } else {
                    trace.className
                }

                val fileName = if (isScriptTrace) {
                    "golem-script.kts"
                } else {
                    trace.fileName
                }

                val lineNumber = if (isScriptTrace) {
                    trace.lineNumber - 1
                } else {
                    trace.lineNumber
                }

                append("  at ")
                append(className)
                append(".")
                append(trace.methodName)
                append("(")
                append(fileName)
                append(":")
                append(lineNumber)
                append(")\n")
                if (isScriptTrace) {
                    append("  | ")
                    append(scriptLines[lineNumber])
                    append("\n")
                }
            }

        }

        error.cause?.let {
            appendException(
                prefix = "Caused by: ",
                error = it
            )
        }
    }

    private fun StringBuilder.appendScriptDiagnostic(
        diagnostic: ScriptDiagnostic,
    ) {
        append("[${diagnostic.severity.name}] ${diagnostic.message}\n")
        diagnostic.location?.let {
            appendLocation(location = it)
        }
        append("\n")
    }

    private fun adjustLineNumber(
        line: Int
    ): Int = when {
        line <= lastImportLine -> line
        line == scriptLines.size -> maxOf(1, line - 2)
        else -> maxOf(1, line - 1)
    }

    private fun StringBuilder.appendLocation(
        location: SourceCode.Location,
    ) {

        val startLine = adjustLineNumber(location.start.line)
        val endLine = adjustLineNumber(location.end?.line ?: startLine)

        append("  at line")
        if (startLine == endLine) {
            append(" ")
            append(startLine)
        } else {
            append("s ")
            append(startLine)
            append("-")
            append(endLine)
        }
        append("\n")

        (startLine..endLine).forEach { line ->
            append("  | ")
            val lineIndexOffset = if ((line <= lastImportLine) || (startLine == (scriptLines.size - 1))) 1 else 0
            val lineIndex = line - lineIndexOffset
            // Guard against out of bounds access
            if (lineIndex < 0 || lineIndex >= scriptLines.size) {
                append("<error>Unable to display source line</error>\n")
                return@forEach
            }
            val scriptLine = scriptLines[lineIndex]
            val lineBuilder = StringBuilder(scriptLine)
            if (location.start.line == scriptLines.size) {
                lineBuilder.append("<error></error>")
            } else {
                if (line == startLine) {
                    val startCol = (location.start.col - 1).coerceIn(0, lineBuilder.length)
                    lineBuilder.insert(startCol, "<error>")
                    if ((line == endLine) && (location.end != null)) {
                        val endCol = (location.end!!.col + 6).coerceIn(0, lineBuilder.length)
                        lineBuilder.insert(endCol, "</error>")
                    }
                }
                if ((line != startLine) && (line == endLine)) { // TODO should it be else without first expression?
                    val endCol = (location.end!!.col - 1).coerceIn(0, lineBuilder.length)
                    lineBuilder.insert(endCol, "</error>")
                }
            }

            append(lineBuilder)
            append("\n")
        }
    }

}

private fun splitScriptImportsAndBody(
    script: String
): ScriptParts {

    val lines = script.lines()

    val lastImportIndex = lines.indexOfLast {
        it.startsWith("import ")
    }

    return if (lastImportIndex == -1) {
        ScriptParts(
            imports = emptyList(),
            body = script.lines()
        )
    } else {
        ScriptParts(
            imports = lines.subList(0, lastImportIndex + 1),
            body = lines.subList(lastImportIndex + 1, lines.size)
        )
    }
}

private data class ScriptParts(
    val imports: List<String>,
    val body: List<String>
)
