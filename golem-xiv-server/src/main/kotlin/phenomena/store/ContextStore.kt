/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.context.store

import com.xemantic.ai.golem.api.Expression
import kotlinx.coroutines.flow.Flow

interface ContextStore {

    fun listMessages(): List<Expression>

    fun messageStores(): Flow<MessageStore>

    fun newMessage(): MessageStore

    fun commit()

}

interface MessageStore {

    //fun listContent(): Flow<Con>

    fun newTextContent(): ContentAppender

    fun newToolUseContent(): ContentAppender

    fun newBinaryContent(data: ByteArray)

    fun newGolemScriptCallContent()

    fun storeMessageMetadata()

}

interface ContentAppender : AutoCloseable {

    fun append(text: String)

}
