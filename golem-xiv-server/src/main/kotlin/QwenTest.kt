/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
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
