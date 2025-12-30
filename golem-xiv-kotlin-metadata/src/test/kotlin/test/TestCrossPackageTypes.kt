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

package com.xemantic.ai.golem.kotlin.metadata.test

import com.xemantic.ai.golem.kotlin.metadata.test.other.CustomProcessor
import com.xemantic.ai.golem.kotlin.metadata.test.other.CustomResult

/**
 * Test class that references types from a different package
 * to verify cross-package type representation in metadata.
 */
class CrossPackageTypeTest(
    val processor: CustomProcessor
) {
    fun process(input: String): CustomResult = CustomResult(
        value = processor.process(input),
        success = true
    )

    fun processWithProcessor(processor: CustomProcessor, input: String): CustomResult {
        return CustomResult(
            value = processor.process(input),
            success = true
        )
    }
}