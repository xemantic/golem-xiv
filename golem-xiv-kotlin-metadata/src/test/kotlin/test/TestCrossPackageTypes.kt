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