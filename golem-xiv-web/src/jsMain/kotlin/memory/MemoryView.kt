/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2026  Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.golem.web.memory

import com.xemantic.golem.viewmodel.memory.MemoryViewModel
import com.xemantic.golem.web.ElementBuilder
import com.xemantic.golem.web.ui.errorIndicator
import com.xemantic.golem.web.ui.loadingIndicator
import com.xemantic.kotlin.js.dom.html.iframe
import com.xemantic.kotlin.js.dom.style
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

fun ElementBuilder.memoryView(
    viewModel: MemoryViewModel
) {

    val loadingIndicator = loadingIndicator()

    val frame = iframe {
        style.display = "none"
        node.onload = { viewModel.onLoaded() }
    }

    viewModel.uiState.onEach { uiState ->
        when (uiState) {
            is MemoryViewModel.UiState.Loading -> {
                /* nothing to do, since the loadingIndicator is already on */
            }
            is MemoryViewModel.UiState.Error -> {
                loadingIndicator.remove()
                errorIndicator(uiState.message)
            }
            is MemoryViewModel.UiState.Loaded -> {
                loadingIndicator.remove()
                frame.style.display = "block"
            }
        }
    }.launchIn(viewModel.scope)

    viewModel.scope.launch {
        frame.src = viewModel.getMemoryBrowserUrl()
    }

}
