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

import kotlin.reflect.KClass
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class GolemScriptExecutor {

    class Dependency<T : Any>(
        val name: String,
        val type: KClass<T>,
        val value: T
    )

    private val scriptingHost = BasicJvmScriptingHost()

    fun execute(
        dependencies: List<Dependency<*>>,
        script: String
    ): Any? {
        val imports = StringBuilder()
        val code = StringBuilder()
        val lineNumberingShift = 10
        script.lines().forEach {
            if (it.trimStart().startsWith("import")) imports.appendLine(it)
            else code.appendLine(it)
        }
//        val imports = script.lines()
//            .takeWhile { it.trimStart().startsWith("import") }
//            .joinToString("\n")
        val scriptWithoutImports = script.lines()
            .dropWhile { it.trimStart().startsWith("import") }
            .joinToString("\n")
        val effectiveScript = """
            import kotlinx.coroutines.runBlocking
            $imports

            runBlocking {
                $code
            }            
        """.trimIndent()
        val compilationConfiguration = ScriptCompilationConfiguration {
            //defaultImports(DependsOn::class, Repository::class)
            jvm {
                dependenciesFromClassContext(contextClass = GolemScriptExecutor::class, wholeClasspath = true)
            }
            providedProperties(*(dependencies.map { it.name to KotlinType(it.type) }.toTypedArray()))
        }

        val evaluationConfiguration = ScriptEvaluationConfiguration {
            providedProperties(*(dependencies.map { it.name to it.value }.toTypedArray()))
            //implicitReceivers(*serviceMap.values.toTypedArray())
            //implicitReceivers(Unit)
        }

        val result = scriptingHost.eval(effectiveScript.toScriptSource(), compilationConfiguration, evaluationConfiguration)
        return when (result) {
            is ResultWithDiagnostics.Success -> {
                val returnValue = result.value.returnValue
                when (returnValue) {
                    is ResultValue.Value -> returnValue.value
                    is ResultValue.Unit -> Unit
                    is ResultValue.Error -> TODO()
                    is ResultValue.NotEvaluated -> TODO()
                }
            }
            is ResultWithDiagnostics.Failure -> throw ScriptExecutionException(result.reports.joinToString("\n") { it.message })
        }

//        return runBlocking {
//            val result = scriptingHost.eval(script.toScriptSource(), compilationConfiguration, evaluationConfiguration)
//
//        }
    }

}

class ScriptExecutionException(
    msg: String
) : RuntimeException(msg)


