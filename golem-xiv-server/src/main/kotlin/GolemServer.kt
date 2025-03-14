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

package com.xemantic.ai.golem.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

fun main() {
    val golem = Golem()
    println("[Golem]> Connecting human and human's machine to my cognition")
    println("[me]> ")
    val conversation = golem.startConversation()
    while (true) {
        val input = readln()
        if (input == "exit") break
        conversation.send()
    }


    fun output(): Flow<String> = flow {
        val agentWorker = AgentWorker()
        input.collect {
            emit("[Golem] ...reasoning...\n")
            agentWorker.prompt(it).collect { output ->
                emit("[Golem]> $output\n")
                emit("[me]> ")
            }
        }
    }
    runBlocking {
        golem.output().collect {
            print(it)
        }
    }
}
