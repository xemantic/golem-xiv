/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.ai.golem.server.script

import com.xemantic.ai.golem.server.kotlin.startsWithAnyOf
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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

// TODO implement script cache
@OptIn(ExperimentalAtomicApi::class)
class GolemScriptExecutor(
    private val scope: CoroutineScope
) {

    private val scriptIdSeq = AtomicInt(1)

    class Dependency<T : Any>(
        val name: String,
        val type: KClass<T>,
        val value: T
    )

    private val logger = KotlinLogging.logger {}

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

    @Throws(GolemScriptException::class)
    suspend fun execute(
        dependencies: List<Dependency<*>>,
        script: String
    ): Any? {

        val scriptId = scriptIdSeq.fetchAndIncrement()
        logger.debug { "GolemScript[$scriptId]: Executing" }

        val (result, duration) = measureTimedValue {
            scope.async {// TODO is this scope needed?
                GolemScriptHandler(
                    scriptId,
                    scope = this,
                    dependencies,
                    script
                ).handle()
            }.await()
        }

        logger.debug { "GolemScript[$scriptId]: Execution took $duration" }

        return when (result) {
            is GolemScript.Result.Value -> result.value
            is GolemScript.Result.Error -> throw GolemScriptException(result.message)
        }

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

        private val suspendableScript = scriptParts.imports.joinToString(separator = "", postfix = "\n") +
                "_golemScriptScope.async {\n${scriptParts.body.joinToString(separator = "\n")}\n}\n"

        suspend fun handle(): GolemScript.Result {
            val compilationResult = compile()
            val result = when (compilationResult) {
                is ResultWithDiagnostics.Success -> {
                    logger.debug { "GolemScript[$scriptId]: Compilation succeeded" }
                    val compiledScript = compilationResult.value
                    evaluate(compiledScript)
                }
                is ResultWithDiagnostics.Failure -> {
                    val failureMessage = errorReporter().toFailureMessage(
                        phase = GolemScript.ExecutionPhase.COMPILATION,
                        failure = compilationResult
                    )
                    logger.debug { "GolemScript[$scriptId]: Compilation failed: $failureMessage" }
                    GolemScript.Result.Error(failureMessage)
                }
            }
            return result
        }

        private suspend fun compile(): ResultWithDiagnostics<CompiledScript>  {
            logger.debug { "GolemScript[$scriptId]: Compiling" }
            val (compileResult, duration) = measureTimedValue {
                scriptingHost.compiler(
                    script = suspendableScript.toScriptSource(),
                    scriptCompilationConfiguration = compilationConfig()
                )
            }
            logger.debug { "GolemScript[$scriptId]: Compilation took $duration" }
            return compileResult
        }

        private suspend fun evaluate(
            compiledScript: CompiledScript
        ): GolemScript.Result {
            logger.debug { "GolemScript[$scriptId]: Evaluating" }
            val (evaluationResult, duration) = measureTimedValue {
                scriptingHost.evaluator(
                    compiledScript = compiledScript,
                    scriptEvaluationConfiguration = evaluationConfig()
                )
            }
            val result = when (evaluationResult) {
                is ResultWithDiagnostics.Success<EvaluationResult> -> {
                    logger.debug { "GolemScript[$scriptId]: Evaluation succeeded" }
                    evaluationResult.value.returnValue.toGolemScriptResult()
                }
                is ResultWithDiagnostics.Failure -> {
                    val failureMessage = errorReporter().toFailureMessage(
                        phase = GolemScript.ExecutionPhase.EVALUATION,
                        failure = evaluationResult
                    )
                    logger.debug { "GolemScript[$scriptId]: Compilation failed: $failureMessage" }
                    GolemScript.Result.Error(failureMessage)
                }
            }
            logger.debug { "GolemScript[$scriptId]: Evaluation took $duration" }
            return result
        }

        private suspend fun ResultValue.toGolemScriptResult() = when (this) {
            is ResultValue.Value -> GolemScript.Result.Value(
                (value as Deferred<Any?>).await()
            )
            is ResultValue.Unit -> GolemScript.Result.Value(Unit)
            is ResultValue.Error -> {
                val failureMessage = errorReporter().toFailureMessage(
                    errorResultValue = this
                )
                GolemScript.Result.Error(
                    message = failureMessage
                )
            }
            is ResultValue.NotEvaluated -> throw IllegalStateException("Should never happen")
        }

        private fun evaluationConfig() = ScriptEvaluationConfiguration {
            providedProperties(*(allDependencies.map { it.name to it.value }.toTypedArray()))
        }

        private fun compilationConfig() = ScriptCompilationConfiguration(compilationConfigurationTemplate) {
            providedProperties(*(allDependencies.map { it.name to KotlinType(it.type) }.toTypedArray()))
        }

        private fun errorReporter() = GolemScriptErrorReporter(
            scriptLines = suspendableScript.lines(),
            lastImportLine = scriptParts.imports.size + 1
        )

    }

}

class GolemScriptException(
    msg: String,
) : RuntimeException(msg)

private fun Iterable<ScriptDiagnostic>.filterNonIgnorable() = filter {
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
        phase: GolemScript.ExecutionPhase,
        failure: ResultWithDiagnostics.Failure
    ) = buildString {
        append("<golem-script> execution failed during phase: ${phase.name}\n")
        failure.reports.filterNonIgnorable().forEach {
            appendScriptDiagnostic(it)
        }
    }

    fun toFailureMessage(
        errorResultValue: ResultValue.Error
    ) = buildString {
        append("<golem-script> execution failed during phase: ${GolemScript.ExecutionPhase.EVALUATION.name}\n")
        appendException(error = errorResultValue.error)
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
        error.stackTrace.filter {
            it?.fileName?.startsWith("script.kts") ?: false
        }.forEach {
            append("  at ")
            append(it.className)
            append(".")
            append(it.methodName)
            append("(")
            append(it.fileName)
            append(":")
            append(it.lineNumber)
            append(")\n  | ")
            append(scriptLines[it.lineNumber - 1])
            append("\n")
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
    } else {
        line + 1
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
            val scriptLine = scriptLines[line - 1]
            val lineBuilder = StringBuilder(line)
            if (line == startLine) {
                lineBuilder.insert(location.start.col - 1, "<error>")
                if ((line == endLine) && (location.end != null)) {
                    lineBuilder.insert(location.end!!.col + 6, "</error>")
                }
            }
            if ((line != startLine) && (line == endLine)) { // TODO should it be else without first expression?
                lineBuilder.insert(location.end!!.col - 1, "</error>")
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
            body = lines.subList(lastImportIndex + 1, lines.size) // TODO it will fail if script consists only out ouf imports
        )
    }
}

private data class ScriptParts(
    val imports: List<String>,
    val body: List<String>
)
