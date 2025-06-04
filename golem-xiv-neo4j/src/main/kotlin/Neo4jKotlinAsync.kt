/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.neo4j

import kotlinx.coroutines.suspendCancellableCoroutine
import org.neo4j.driver.Driver
import org.neo4j.driver.async.AsyncSession
import org.neo4j.driver.async.ResultCursor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// TODO this code is not used, it needs a serious review before applying

private suspend fun <T> Driver.read(
    query: String,
    params: Map<String, Any> = emptyMap(),
    block: (ResultCursor) -> T
): T = suspendCancellableCoroutine { cont ->
    val session = session(AsyncSession::class.java)

    // Handle cancellation
    cont.invokeOnCancellation {
        try {
            session.closeAsync()
        } catch (e: Exception) {
            // Log but don't throw in cancellation handler
        }
    }

    session.executeReadAsync { tx ->
        tx.runAsync(query, params)
    }.whenCompleteAsync { result, throwable ->
        try {
            if (throwable != null) {
                cont.resumeWithException(throwable)
            } else {
                val transformedResult = block(result)
                cont.resume(transformedResult)
            }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        } finally {
            session.closeAsync()
        }
    }

}


//suspend fun <T> Driver.readFlow(
//    query: String,
//    params: Map<String, Any> = emptyMap(),
//    bufferSize: Int = Channel.BUFFERED,
//    transform: (ResultCursor) -> T
//): Flow<T> = callbackFlow {
//    val session = session(AsyncSession::class.java)
//
//    val cleanup = {
//        try {
//            session.closeAsync()
//        } catch (e: Exception) {
//            // Log cleanup errors
//        }
//    }
//
//    awaitClose { cleanup() }
//
//    try {
//        session.executeReadAsync { tx ->
//            tx.runAsync(query, params)
//        }.whenCompleteAsync { result, throwable ->
//            if (throwable != null) {
//                close(throwable)
//                return@whenCompleteAsync
//            }
//
//            try {
//                // Get cursor for streaming
//                val cursor = result.consumeAsync()
//
//                fun processNext() {
//                    if (cursor.hasNext()) {
//                        val record = cursor.next()
//                        try {
//                            val transformed = transform(record)
//                            val sendResult = trySend(transformed)
//
//                            when {
//                                sendResult.isSuccess -> processNext() // Continue
//                                sendResult.isFailure -> {
//                                    close(sendResult.exceptionOrNull())
//                                }
//                                // Channel is full, will try again when space available
//                            }
//                        } catch (e: Exception) {
//                            close(e)
//                        }
//                    } else {
//                        close() // Done processing all records
//                    }
//                }
//
//                processNext()
//
//            } catch (e: Exception) {
//                close(e)
//            }
//        }
//    } catch (e: Exception) {
//        close(e)
//    }
//}.buffer(bufferSize)
