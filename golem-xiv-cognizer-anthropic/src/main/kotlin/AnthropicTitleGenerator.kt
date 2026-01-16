/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2025  Kazimierz Pogoda / Xemantic
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

package com.xemantic.ai.golem.cognizer.anthropic

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.anthropic.content.Text
import com.xemantic.ai.anthropic.message.Message
import com.xemantic.ai.anthropic.message.Role
import com.xemantic.ai.golem.api.backend.TitleGenerator

class AnthropicTitleGenerator(
    private val anthropic: Anthropic
) : TitleGenerator {

    override suspend fun generateTitle(userMessage: String): String {
        val response = anthropic.messages.create {
            maxTokens = 30
            messages = listOf(
                Message {
                    role = Role.USER
                    content = listOf(Text("""
                        Generate a concise title (max 6 words) for a conversation starting with:

                        "$userMessage"

                        Reply with only the title, no quotes or extra punctuation.
                    """.trimIndent()))
                }
            )
        }
        return (response.content.first() as Text).text.trim()
    }

}
