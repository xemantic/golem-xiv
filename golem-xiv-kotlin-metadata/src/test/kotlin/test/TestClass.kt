package com.xemantic.ai.golem.kotlin.metadata.test

/**
 * A simple test class for testing metadata resolution.
 */
class TestClass(val name: String) {
    fun greet(): String = "Hello, $name!"
}

/**
 * Extension function for TestClass.
 */
fun TestClass.farewell(): String = "Goodbye, $name!"
