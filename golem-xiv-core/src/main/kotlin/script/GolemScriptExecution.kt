/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
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
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassContext
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
            dependenciesFromClassContext(
                contextClass = GolemScriptExecutor::class,
                wholeClasspath = true
            )
        }
        // Add helpful default imports
        defaultImports(
            "kotlinx.coroutines.*"
        )
    }

    suspend fun execute(
        script: String,
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
        } + "_golemScriptScope.async {\n${scriptParts.body.joinToString(separator = "\n")}\n}"

        suspend fun handle(): ExecuteGolemScript.Result {

            val compilationResult = compile()

            val result = when (compilationResult) {
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
    it.ignorable
}

private val ScriptDiagnostic.ignorable
    get() = (severity == ScriptDiagnostic.Severity.DEBUG && message.startsWithAnyOf(
        "Using JDK home inferred from java.home",
        "Loading modules:"
    )) || (severity == ScriptDiagnostic.Severity.ERROR && message.startsWith(
        "Cannot infer type for this parameter. Specify it explicitly."
    ))

private class GolemScriptErrorReporter(
    private val scriptLines: List<String>,
    private val lastImportLine: Int
) {

    fun toFailureMessage(
        phase: ExecuteGolemScript.ExecutionPhase,
        failure: ResultWithDiagnostics.Failure
    ) = buildString {
        append("<golem-script> execution failed during phase: ${phase.name}\n")
        failure.reports.filterNonIgnorable().forEach {
            appendScriptDiagnostic(it)
        }
    }

    fun toFailureMessage(
        throwable: Throwable
    ) = buildString {
        append("<golem-script> execution failed during phase: ${ExecuteGolemScript.ExecutionPhase.EVALUATION.name}\n")
        appendException(error = throwable)
        append("\n")
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
    ): Int = if (line <= lastImportLine) {
        line
    } else if (line == scriptLines.size){
        line - 2
    } else {
        line - 1
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
            val scriptLine = scriptLines[line - lineIndexOffset]
            val lineBuilder = StringBuilder(scriptLine)
            if (location.start.line == scriptLines.size) {
                lineBuilder.append("<error/>")
            } else {
                if (line == startLine) {
                    lineBuilder.insert(location.start.col - 1, "<error>")
                    if ((line == endLine) && (location.end != null)) {
                        lineBuilder.insert(location.end!!.col + 6, "</error>")
                    }
                }
                if ((line != startLine) && (line == endLine)) { // TODO should it be else without first expression?
                    lineBuilder.insert(location.end!!.col - 1, "</error>")
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
