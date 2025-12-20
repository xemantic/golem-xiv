package com.xemantic.ai.golem.kotlin.metadata.test.ext

import com.xemantic.ai.golem.kotlin.metadata.test.TestClass

/**
 * Extension function from a different package.
 */
fun TestClass.describe(): String = "TestClass(name=$name)"

/**
 * Extension property from a different package.
 */
val TestClass.uppercaseName: String
    get() = name.uppercase()