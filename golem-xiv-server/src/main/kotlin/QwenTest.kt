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

import com.alibaba.dashscope.aigc.generation.Generation
import com.alibaba.dashscope.aigc.generation.GenerationParam
import com.alibaba.dashscope.aigc.generation.GenerationResult
import com.alibaba.dashscope.common.Message
import com.alibaba.dashscope.common.Role
import com.alibaba.dashscope.protocol.Protocol

fun main() {
    val result = callWithMessage()
    println(result.output.choices[0].message.getContent())
}

fun callWithMessage(): GenerationResult {
    val gen = Generation(Protocol.HTTP.value, "https://dashscope-intl.aliyuncs.com/api/v1")
    val systemMsg = Message.builder()
        .role(Role.SYSTEM.value)
        .content("You are a helpful assistant.")
        .build()
    val userMsg = Message.builder()
        .role(Role.USER.value)
        .content("Who are you?")
        .build()
    val param = GenerationParam.builder() // If the environment variable is not configured, replace the following line with your API key: .apiKey("sk-xxx")
        .apiKey(System.getenv("DASHSCOPE_API_KEY")) // Model list: https://www.alibabacloud.com/help/en/model-studio/getting-started/models
        .model("qwen-plus")
        .messages(listOf(systemMsg, userMsg))
        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
        .build()
    return gen.call(param)
}
