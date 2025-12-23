package com.xemantic.ai.golem.kotlin.metadata.test.other

/**
 * A custom type from a different package, used to test cross-package type references.
 */
class CustomProcessor {
    fun process(input: String): String = input.uppercase()
}

/**
 * Another custom type for testing multiple cross-package references.
 */
data class CustomResult(
    val value: String,
    val success: Boolean
)