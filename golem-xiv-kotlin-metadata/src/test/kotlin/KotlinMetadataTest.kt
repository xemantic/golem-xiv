
package com.xemantic.ai.golem.kotlin.metadata

import com.xemantic.kotlin.test.sameAs
import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class KotlinMetadataTest {

    // given
    val resolver = DefaultKotlinMetadata()

    @Test
    @Ignore
    fun `should resolve String as known stdlib class`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String"
        )

        // then
        signature sameAs /* language=kotlin */ """
            // kotlin.String [page 1/8]
            class String : Comparable<String>, CharSequence {
                fun compareTo(other: String): Int
                fun equals(other: Any?): Boolean
                fun get(index: Int): Char
                fun hashCode(): Int
                val length: Int
                operator fun plus(other: Any?): String
                fun subSequence(startIndex: Int, endIndex: Int): CharSequence
                fun toString(): String
            }
        """.trimIndent()
    }

    @Test
    @Ignore
    fun `should resolve String as known stdlib class with a further page`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String",
            page = 7
        )

        // then
        signature sameAs /* language=kotlin */ """
            // kotlin.String [page 8/8]
            class String : Comparable<String>, CharSequence {
                fun reversed(): String
            }
            fun CharSequence.zipWithNext(): List<Pair<Char, Char>>
        """.trimIndent()
    }

    @Test
    fun `should resolve String with all members when pageSize is large`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String",
            pageSize = 1000
        )

        // then
        signature sameAs /* language=kotlin */ """
            // kotlin.String [page 1/1]
            class String : Comparable<String>, CharSequence {
                fun compareTo(other: String): Int
                fun equals(other: Any?): Boolean
                fun get(index: Int): Char
                fun hashCode(): Int
                val length: Int
                operator fun plus(other: Any?): String
                fun subSequence(startIndex: Int, endIndex: Int): CharSequence
                fun toString(): String
                fun reversed(): String
            }
            fun CharSequence.zipWithNext(): List<Pair<Char, Char>>
        """.trimIndent()
    }

    @Test
    fun `should resolve TestClass with extension functions from multiple packages`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.TestClass"
        )

        // then
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.TestClass [page 1/1]
            class TestClass(val name: String) {
                fun greet(): String
            }
            // from com.xemantic.ai.golem.kotlin.metadata.test
            fun TestClass.farewell(): String
            // from com.xemantic.ai.golem.kotlin.metadata.test.ext
            val TestClass.uppercaseName: String
            fun TestClass.describe(): String
        """.trimIndent()
    }

}
