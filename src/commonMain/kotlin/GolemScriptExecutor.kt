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

import kotlin.reflect.KClass

interface GolemScriptExecutor {

    class Dependency<T : Any>(
        val name: String,
        val type: KClass<T>,
        val value: T
    )

    fun execute(script: String): Any?

}

expect fun golemScriptExecutor(
    dependencies: List<GolemScriptExecutor.Dependency<*>>
): GolemScriptExecutor
