
package com.xemantic.ai.golem.kotlin.metadata

import com.xemantic.kotlin.test.sameAs
import org.junit.jupiter.api.Test

class KotlinMetadataTest {

    // given
    val resolver = DefaultKotlinMetadata()

    @Test
    fun `should resolve String as known stdlib class`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String"
        )

        // then - verify key structure elements
        signature sameAs """
            // kotlin.String [page 1/5]
            class String : Comparable<String>, CharSequence {
                fun byteInputStream(charset: Charset): ByteArrayInputStream
                fun chars(): IntStream
                fun codePoints(): IntStream
                operator fun compareTo(other: String): Int
                fun describeConstable(): Optional<String>
                operator fun equals(other: Any?): Boolean
                operator fun get(index: Int): Char
                fun hashCode(): Int
                fun hexToByte(format: HexFormat): Byte
                fun hexToByteArray(format: HexFormat): ByteArray
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve String as known stdlib class - page 2`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String",
            page = 2
        )

        // then - verify key structure elements
        signature sameAs """
            // kotlin.String [page 2/5]
            class String : Comparable<String>, CharSequence {
                fun hexToInt(format: HexFormat): Int
                fun hexToLong(format: HexFormat): Long
                fun hexToShort(format: HexFormat): Short
                fun hexToUByte(format: HexFormat): UByte
                fun hexToUByteArray(format: HexFormat): UByteArray
                fun hexToUInt(format: HexFormat): UInt
                fun hexToULong(format: HexFormat): ULong
                fun hexToUShort(format: HexFormat): UShort
                fun isEmpty(): Boolean
                fun isLocalClassName(): Boolean
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve String as known stdlib class - page 3`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String",
            page = 3
        )

        // then - verify key structure elements
        signature sameAs """
            // kotlin.String [page 3/5]
            class String : Comparable<String>, CharSequence {
                val length: Int
                operator fun plus(other: Any?): String
                fun reader(): StringReader
                fun strip(): String
                fun stripIndent(): String
                fun stripLeading(): String
                fun stripTrailing(): String
                fun subSequence(startIndex: Int, endIndex: Int): CharSequence
                fun toJvmInternalName(): String
                fun toString(): String
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve String as known stdlib class - page 4`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String",
            page = 4
        )

        // then - verify key structure elements
        signature sameAs """
            // kotlin.String [page 4/5]
            class String : Comparable<String>, CharSequence {
                fun toUByte(): UByte
                fun toUByte(radix: Int): UByte
                fun toUByteOrNull(): UByte?
                fun toUByteOrNull(radix: Int): UByte?
                fun toUInt(): UInt
                fun toUInt(radix: Int): UInt
                fun toUIntOrNull(): UInt?
                fun toUIntOrNull(radix: Int): UInt?
                fun toULong(): ULong
                fun toULong(radix: Int): ULong
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve String as known stdlib class - page 5`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String",
            page = 5
        )

        // then - verify key structure elements
        signature sameAs """
            // kotlin.String [page 5/5]
            class String : Comparable<String>, CharSequence {
                fun toULongOrNull(): ULong?
                fun toULongOrNull(radix: Int): ULong?
                fun toUShort(): UShort
                fun toUShort(radix: Int): UShort
                fun toUShortOrNull(): UShort?
                fun toUShortOrNull(radix: Int): UShort?
                fun translateEscapes(): String
            }
            // from kotlin
            inline infix fun Comparable<T>.compareTo(other: T): Int
        """.trimIndent()
    }

    @Test
    fun `should resolve String with extension functions from kotlin stdlib - huge page size`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String",
            pageSize = 1000
        )

        // then
        signature sameAs """
            // kotlin.String [page 1/1]
            class String : Comparable<String>, CharSequence {
                fun byteInputStream(charset: Charset): ByteArrayInputStream
                fun chars(): IntStream
                fun codePoints(): IntStream
                operator fun compareTo(other: String): Int
                fun describeConstable(): Optional<String>
                operator fun equals(other: Any?): Boolean
                operator fun get(index: Int): Char
                fun hashCode(): Int
                fun hexToByte(format: HexFormat): Byte
                fun hexToByteArray(format: HexFormat): ByteArray
                fun hexToInt(format: HexFormat): Int
                fun hexToLong(format: HexFormat): Long
                fun hexToShort(format: HexFormat): Short
                fun hexToUByte(format: HexFormat): UByte
                fun hexToUByteArray(format: HexFormat): UByteArray
                fun hexToUInt(format: HexFormat): UInt
                fun hexToULong(format: HexFormat): ULong
                fun hexToUShort(format: HexFormat): UShort
                fun isEmpty(): Boolean
                fun isLocalClassName(): Boolean
                val length: Int
                operator fun plus(other: Any?): String
                fun reader(): StringReader
                fun strip(): String
                fun stripIndent(): String
                fun stripLeading(): String
                fun stripTrailing(): String
                fun subSequence(startIndex: Int, endIndex: Int): CharSequence
                fun toJvmInternalName(): String
                fun toString(): String
                fun toUByte(): UByte
                fun toUByte(radix: Int): UByte
                fun toUByteOrNull(): UByte?
                fun toUByteOrNull(radix: Int): UByte?
                fun toUInt(): UInt
                fun toUInt(radix: Int): UInt
                fun toUIntOrNull(): UInt?
                fun toUIntOrNull(radix: Int): UInt?
                fun toULong(): ULong
                fun toULong(radix: Int): ULong
                fun toULongOrNull(): ULong?
                fun toULongOrNull(radix: Int): ULong?
                fun toUShort(): UShort
                fun toUShort(radix: Int): UShort
                fun toUShortOrNull(): UShort?
                fun toUShortOrNull(radix: Int): UShort?
                fun translateEscapes(): String
            }
            // from kotlin
            inline infix fun Comparable<T>.compareTo(other: T): Int
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
