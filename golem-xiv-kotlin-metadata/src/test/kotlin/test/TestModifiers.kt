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
