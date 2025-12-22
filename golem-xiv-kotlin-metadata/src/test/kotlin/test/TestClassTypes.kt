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
