/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/*
  In the future this should be an independent project
 */

object Action

open class ScopedPresenter(
    private val scope: CoroutineScope
) {

    fun <T> Flow<T>.listen(
        block: suspend (T) -> Unit
    ) {
        listen(scope, block)
    }

}

fun <T> Flow<T>.listen(
    scope: CoroutineScope,
    block: suspend (T) -> Unit
) {
    scope.launch {
        collect {
            block(it)
        }
    }
}
