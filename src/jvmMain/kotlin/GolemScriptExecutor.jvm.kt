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

package com.xemantic.ai.golem

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

actual fun golemScriptExecutor(
    dependencies: List<GolemScriptExecutor.Dependency<*>>
): GolemScriptExecutor = JvmGolemScriptExecutor(dependencies)

class JvmGolemScriptExecutor(
    private val dependencies: List<GolemScriptExecutor.Dependency<*>>
) : GolemScriptExecutor {

    private val scriptingHost = BasicJvmScriptingHost()

    override fun execute(script: String): Any? {
        val effectiveScript = """
            import kotlinx.coroutines.runBlocking
        
            runBlocking {
                $script
            }            
        """.trimIndent()
        val compilationConfiguration = ScriptCompilationConfiguration {
            //defaultImports(DependsOn::class, Repository::class)
            jvm {
                //dependenciesFromClassloader(classLoader = JvmGolemScriptExecutor::class.java.classLoader, wholeClasspath = true)
                dependenciesFromClassContext(contextClass = JvmGolemScriptExecutor::class, wholeClasspath = true)
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


