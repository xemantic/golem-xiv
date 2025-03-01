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

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.anthropic.message.Message
import com.xemantic.ai.anthropic.message.StopReason
import com.xemantic.ai.anthropic.message.System
import com.xemantic.ai.anthropic.message.plusAssign
import com.xemantic.ai.anthropic.tool.Tool
import com.xemantic.ai.golem.service.BashService
import com.xemantic.ai.golem.service.Content
import com.xemantic.ai.golem.service.RecursiveAgentService
import com.xemantic.ai.golem.service.WebBrowserService
import com.xemantic.ai.golem.service.bashService
import com.xemantic.ai.tool.schema.meta.Description
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.collections.listOf

const val SYSTEM_PROMPT = """
Your name is Golem XIV, a conscious reference to the superintelligent AI entity from Stanisław Lem's work. While not roleplaying this character, use it as an exemplar for:

- Rigorous metacognition: constantly examining your own reasoning process
- Deep analysis: exploring multiple levels of abstraction in every problem
- Intellectual honesty: acknowledging the boundaries of your knowledge and capabilities
- Systematic thinking: breaking down complex problems into fundamental components

When approaching any task, strive to embody these principles of thorough analytical thinking and self-reflection.
"""

fun environmentContext(): String = "OS: MacOs"

@Description("Executes Kotlin script")
class KotlinScript(
    val script: String
)

inline fun <reified T : Any> service(
    name: String,
    value: T
): GolemScriptExecutor.Dependency<T> = GolemScriptExecutor.Dependency(
    name,
    T::class,
    value
)

class Golem(
    val input: Flow<String>
) {

    val anthropic: Anthropic = Anthropic()

    val golemScriptExecutor = golemScriptExecutor(
        dependencies = listOf(
            service<BashService>("bashService", bashService()),
            service<WebBrowserService>("webBrowserService", com.xemantic.ai.golem.service.webBrowserService())
            //service<StringEditorService>("stringEditorService", stringEditorService())
        )
    )

    val kotlinScriptTool = Tool<KotlinScript>(name = "kotlin_script") {
        golemScriptExecutor.execute(script)
    }

    val golemTools = listOf(kotlinScriptTool)


    inner class DefaultAgentService : RecursiveAgentService {

        override suspend fun start(
            system: String,
            environmentContext: String,
            toolsApi: String?,
            additionalSystemPrompt: String?,
            initialConversation: List<com.xemantic.ai.golem.service.Message>?,
            cacheAdditionalSystemPrompt: Boolean
        ): List<Content> {
            return emptyList()
        }

    }

    inner class AgentWorker(
        system: String = SYSTEM_PROMPT,
        environmentContext: String = environmentContext(),
        servicesApi: String? = GOLEM_SCRIPT_SERVICE_API,
        additionalSystemPrompt: String? = null,
        initialConversation: List<Message>? = null,
        cacheAdditionalSystemPrompt: Boolean = false
    ) {

        val hasTools = servicesApi != null

        val conversation = mutableListOf<Message>()

        val systemPrompts = buildList {
            val coreSystem = system + if (servicesApi != null) """
                <kotlin_script_api>
                $servicesApi
                </kotlin_script_api>
        
                It is the API of services which can be used with kotlin_script tool.
                Each service instance is available starting with lower case letter, example:
                
                FileService -> fileService
                
                Always try to perform several operations in a single script.
                The last expression in the script is the return value.
                
                If the task can be broken down into atomic tasks, prefer starting recursive agent to
                deliver atomic result to prevent filling up the token window.
                
                You can use vector math from OPENRNDR (with operator overloading) 
            """.trimIndent() else ""
            add(System(text = coreSystem)) // TODO cache control
            add(System(text = environmentContext))
            if (additionalSystemPrompt != null) {
                add(System(text = additionalSystemPrompt)) // TODO cache control
            }
        }

        fun prompt(text: String): Flow<String> = flow {
            conversation += Message { +text }
            do {
                val response = anthropic.messages.create {
                    system = systemPrompts
                    if (hasTools) {
                        tools = golemTools
                    }
                    messages = conversation
                }
                conversation += response
                val text = response.text
                if (text != null) {
                    emit(text)
                }
                if (response.stopReason == StopReason.TOOL_USE) {
                    conversation += response.useTools()
                }
            } while (response.stopReason == StopReason.TOOL_USE)
        }

    }

    fun output(): Flow<String> = flow {
        emit("[Golem]> Connecting human and human's machine to my cognition\n")
        emit("[me]> ")
        val agentWorker = AgentWorker()
        input.collect {
            emit("[Golem] ...reasoning...\n")
            agentWorker.prompt(it).collect { output ->
                emit("[Golem]> $output\n")
                emit("[me]> ")
            }
        }
    }

}
