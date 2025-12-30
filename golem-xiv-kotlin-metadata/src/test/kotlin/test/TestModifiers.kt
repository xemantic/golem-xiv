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

// Test class with various visibility modifiers
class VisibilityTestClass {
    val publicProperty: String = "public"  // default public
    private val privateProperty: String = "private"
    protected val protectedProperty: String = "protected"
    internal val internalProperty: String = "internal"

    fun publicFunction(): String = "public"  // default public
    private fun privateFunction(): String = "private"
    protected fun protectedFunction(): String = "protected"
    internal fun internalFunction(): String = "internal"
}

// Test open class with modality modifiers
open class OpenTestClass {
    open val openProperty: String = "open"
    open fun openFunction(): String = "open"
}

// Test sealed class
sealed class SealedTestClass {
    class SubClass1(val name: String) : SealedTestClass()
    class SubClass2(val count: Int) : SealedTestClass()
}

// Test abstract class
abstract class AbstractTestClass {
    abstract val abstractProperty: String
    abstract fun abstractFunction(): String

    open val openProperty: String = "open"
    open fun openFunction(): String = "open"
}

// Test class implementing interface with override
class OverrideTestClass : Comparable<OverrideTestClass> {
    override fun compareTo(other: OverrideTestClass): Int = 0
}
