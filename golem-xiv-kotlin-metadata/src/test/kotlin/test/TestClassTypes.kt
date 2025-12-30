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

// Data class
data class DataTestClass(
    val id: Int,
    val name: String
)

// Value class (inline class)
@JvmInline
value class ValueTestClass(val value: String)

// Object (singleton)
object ObjectTestClass {
    val name: String = "singleton"
    fun greet(): String = "Hello from $name"
}

// Simple enum class
enum class EnumTestClass {
    FIRST,
    SECOND,
    THIRD
}

// Rich enum class with constructor, properties and functions
enum class RichEnumTestClass(val code: Int, val label: String) {
    ALPHA(1, "Alpha"),
    BETA(2, "Beta"),
    GAMMA(3, "Gamma");

    val displayName: String get() = "$label ($code)"

    fun isFirst(): Boolean = this == ALPHA
}

// Annotation class
annotation class AnnotationTestClass(
    val message: String = ""
)

// Class with companion object
class CompanionTestClass {
    companion object {
        const val CONSTANT: String = "const"
        fun create(): CompanionTestClass = CompanionTestClass()
    }
}
