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


package com.xemantic.ai.golem.kotlin.metadata

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.sameAs
import org.junit.jupiter.api.Test

// we keep this dependency statically initialized, so that classpath scanning happens only once
// given
val resolver = DefaultKotlinMetadata()

class KotlinMetadataTest {

    @Test
    fun `should resolve String as known stdlib class`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String"
        )

        // then
        signature sameAs /* language=kotlin */ """
            // kotlin.String [page 1/42]
            class String : Comparable<String>, CharSequence {
                fun byteInputStream(charset: java.nio.charset.Charset): java.io.ByteArrayInputStream
                fun capitalize(): String
                fun capitalize(locale: java.util.Locale): String
                fun chars(): java.util.stream.IntStream
                fun codePointAt(index: Int): Int
                fun codePointBefore(index: Int): Int
                fun codePointCount(beginIndex: Int, endIndex: Int): Int
                fun codePoints(): java.util.stream.IntStream
                operator fun compareTo(other: String): Int
                fun compareTo(other: String, ignoreCase: Boolean): Int
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
        signature sameAs /* language=kotlin */ """
            // kotlin.String [page 2/42]
            class String : Comparable<String>, CharSequence {
                fun contentEquals(charSequence: CharSequence): Boolean
                fun contentEquals(stringBuilder: java.lang.StringBuffer): Boolean
                fun decapitalize(): String
                fun decapitalize(locale: java.util.Locale): String
                fun describeConstable(): java.util.Optional<String>
                fun drop(n: Int): String
                fun dropLast(n: Int): String
                fun dropLastWhile(predicate: Function1<Char, Boolean>): String
                fun dropWhile(predicate: Function1<Char, Boolean>): String
                fun encodeToByteArray(): ByteArray
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
        signature sameAs /* language=kotlin */ """
            // kotlin.String [page 3/42]
            class String : Comparable<String>, CharSequence {
                fun encodeToByteArray(startIndex: Int, endIndex: Int, throwOnInvalidSequence: Boolean): ByteArray
                fun endsWith(suffix: String, ignoreCase: Boolean): Boolean
                operator fun equals(other: Any?): Boolean
                fun equals(other: String?, ignoreCase: Boolean): Boolean
                fun filter(predicate: Function1<Char, Boolean>): String
                fun filterIndexed(predicate: Function2<Int, Char, Boolean>): String
                fun filterNot(predicate: Function1<Char, Boolean>): String
                fun format(args: Array<out Any?>): String
                fun format(locale: java.util.Locale?, args: Array<out Any?>): String
                operator fun get(index: Int): Char
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve String as known stdlib class - page 19 (transition page with members and extensions)`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String",
            page = 19
        )

        // then - page 19 shows the last class member and first extensions
        signature sameAs /* language=kotlin */ """
            // kotlin.String [page 19/42]
            class String : Comparable<String>, CharSequence {
                fun uppercase(locale: java.util.Locale): String
            }
            // from kotlin
            inline infix fun Comparable<T>.compareTo(other: T): Int
            // from kotlin.text
            val CharSequence.indices: IntRange
            val CharSequence.lastIndex: Int
            inline fun CharSequence.all(predicate: Function1<Char, Boolean>): Boolean
            fun CharSequence.any(): Boolean
            inline fun CharSequence.any(predicate: Function1<Char, Boolean>): Boolean
            fun CharSequence.asIterable(): Iterable<Char>
            fun CharSequence.asSequence(): Sequence<Char>
            inline fun CharSequence.associate(transform: Function1<Char, Pair<T, T>>): Map<T, T>
        """.trimIndent()
    }

    @Test
    fun `should resolve String as known stdlib class - page 42 (the last)`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String",
            page = 42
        )

        // then - extension-only pages omit the empty class body
        signature sameAs /* language=kotlin */ """
            // kotlin.String [page 42/42]
            // from kotlin.text
            fun CharSequence.zipWithNext(): List<Pair<Char, Char>>
            inline fun CharSequence.zipWithNext(transform: Function2<Char, Char, T>): List<T>
        """.trimIndent()
    }

    @Test
    fun `should resolve String with extension functions from kotlin stdlib - huge page size`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String",
            pageSize = 10000
        )

        // then
        signature sameAs /* language=kotlin */ """
            // kotlin.String [page 1/1]
            class String : Comparable<String>, CharSequence {
                fun byteInputStream(charset: java.nio.charset.Charset): java.io.ByteArrayInputStream
                fun capitalize(): String
                fun capitalize(locale: java.util.Locale): String
                fun chars(): java.util.stream.IntStream
                fun codePointAt(index: Int): Int
                fun codePointBefore(index: Int): Int
                fun codePointCount(beginIndex: Int, endIndex: Int): Int
                fun codePoints(): java.util.stream.IntStream
                operator fun compareTo(other: String): Int
                fun compareTo(other: String, ignoreCase: Boolean): Int
                fun contentEquals(charSequence: CharSequence): Boolean
                fun contentEquals(stringBuilder: java.lang.StringBuffer): Boolean
                fun decapitalize(): String
                fun decapitalize(locale: java.util.Locale): String
                fun describeConstable(): java.util.Optional<String>
                fun drop(n: Int): String
                fun dropLast(n: Int): String
                fun dropLastWhile(predicate: Function1<Char, Boolean>): String
                fun dropWhile(predicate: Function1<Char, Boolean>): String
                fun encodeToByteArray(): ByteArray
                fun encodeToByteArray(startIndex: Int, endIndex: Int, throwOnInvalidSequence: Boolean): ByteArray
                fun endsWith(suffix: String, ignoreCase: Boolean): Boolean
                operator fun equals(other: Any?): Boolean
                fun equals(other: String?, ignoreCase: Boolean): Boolean
                fun filter(predicate: Function1<Char, Boolean>): String
                fun filterIndexed(predicate: Function2<Int, Char, Boolean>): String
                fun filterNot(predicate: Function1<Char, Boolean>): String
                fun format(args: Array<out Any?>): String
                fun format(locale: java.util.Locale?, args: Array<out Any?>): String
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
                fun intern(): String
                fun isEmpty(): Boolean
                fun isLocalClassName(): Boolean
                val length: Int
                fun lowercase(): String
                fun lowercase(locale: java.util.Locale): String
                fun offsetByCodePoints(index: Int, codePointOffset: Int): Int
                fun orEmpty(): String
                fun padEnd(length: Int, padChar: Char): String
                fun padStart(length: Int, padChar: Char): String
                fun partition(predicate: Function1<Char, Boolean>): Pair<String, String>
                operator fun plus(other: Any?): String
                fun prependIndent(indent: String): String
                fun reader(): java.io.StringReader
                fun regionMatches(thisOffset: Int, other: String, otherOffset: Int, length: Int, ignoreCase: Boolean): Boolean
                fun removePrefix(prefix: CharSequence): String
                fun removeRange(startIndex: Int, endIndex: Int): String
                fun removeRange(range: IntRange): String
                fun removeSuffix(suffix: CharSequence): String
                fun removeSurrounding(prefix: CharSequence, suffix: CharSequence): String
                fun removeSurrounding(delimiter: CharSequence): String
                fun replace(oldChar: Char, newChar: Char, ignoreCase: Boolean): String
                fun replace(oldValue: String, newValue: String, ignoreCase: Boolean): String
                fun replaceAfter(delimiter: Char, replacement: String, missingDelimiterValue: String): String
                fun replaceAfter(delimiter: String, replacement: String, missingDelimiterValue: String): String
                fun replaceAfterLast(delimiter: String, replacement: String, missingDelimiterValue: String): String
                fun replaceAfterLast(delimiter: Char, replacement: String, missingDelimiterValue: String): String
                fun replaceBefore(delimiter: Char, replacement: String, missingDelimiterValue: String): String
                fun replaceBefore(delimiter: String, replacement: String, missingDelimiterValue: String): String
                fun replaceBeforeLast(delimiter: Char, replacement: String, missingDelimiterValue: String): String
                fun replaceBeforeLast(delimiter: String, replacement: String, missingDelimiterValue: String): String
                fun replaceFirst(oldChar: Char, newChar: Char, ignoreCase: Boolean): String
                fun replaceFirst(oldValue: String, newValue: String, ignoreCase: Boolean): String
                fun replaceFirstChar(transform: Function1<Char, Char>): String
                fun replaceFirstChar(transform: Function1<Char, CharSequence>): String
                fun replaceIndent(newIndent: String): String
                fun replaceIndentByMargin(newIndent: String, marginPrefix: String): String
                fun replaceRange(startIndex: Int, endIndex: Int, replacement: CharSequence): String
                fun replaceRange(range: IntRange, replacement: CharSequence): String
                fun reversed(): String
                fun slice(indices: IntRange): String
                fun slice(indices: Iterable<Int>): String
                fun startsWith(prefix: String, ignoreCase: Boolean): Boolean
                fun startsWith(prefix: String, startIndex: Int, ignoreCase: Boolean): Boolean
                fun strip(): String
                fun stripIndent(): String
                fun stripLeading(): String
                fun stripTrailing(): String
                fun subSequence(startIndex: Int, endIndex: Int): CharSequence
                fun subSequence(start: Int, end: Int): CharSequence
                fun substring(startIndex: Int): String
                fun substring(startIndex: Int, endIndex: Int): String
                fun substring(range: IntRange): String
                fun substringAfter(delimiter: Char, missingDelimiterValue: String): String
                fun substringAfter(delimiter: String, missingDelimiterValue: String): String
                fun substringAfterLast(delimiter: Char, missingDelimiterValue: String): String
                fun substringAfterLast(delimiter: String, missingDelimiterValue: String): String
                fun substringBefore(delimiter: Char, missingDelimiterValue: String): String
                fun substringBefore(delimiter: String, missingDelimiterValue: String): String
                fun substringBeforeLast(delimiter: Char, missingDelimiterValue: String): String
                fun substringBeforeLast(delimiter: String, missingDelimiterValue: String): String
                fun take(n: Int): String
                fun takeLast(n: Int): String
                fun takeLastWhile(predicate: Function1<Char, Boolean>): String
                fun takeWhile(predicate: Function1<Char, Boolean>): String
                fun toBigDecimal(): java.math.BigDecimal
                fun toBigDecimal(mathContext: java.math.MathContext): java.math.BigDecimal
                fun toBigDecimalOrNull(): java.math.BigDecimal?
                fun toBigDecimalOrNull(mathContext: java.math.MathContext): java.math.BigDecimal?
                fun toBigInteger(): java.math.BigInteger
                fun toBigInteger(radix: Int): java.math.BigInteger
                fun toBigIntegerOrNull(): java.math.BigInteger?
                fun toBigIntegerOrNull(radix: Int): java.math.BigInteger?
                fun toBoolean(): Boolean
                fun toBooleanStrict(): Boolean
                fun toBooleanStrictOrNull(): Boolean?
                fun toByte(): Byte
                fun toByte(radix: Int): Byte
                fun toByteArray(charset: java.nio.charset.Charset): ByteArray
                fun toByteOrNull(): Byte?
                fun toByteOrNull(radix: Int): Byte?
                fun toCharArray(startIndex: Int, endIndex: Int): CharArray
                fun toCharArray(): CharArray
                fun toCharArray(destination: CharArray, destinationOffset: Int, startIndex: Int, endIndex: Int): CharArray
                fun toDouble(): Double
                fun toDoubleOrNull(): Double?
                fun toFloat(): Float
                fun toFloatOrNull(): Float?
                fun toInt(): Int
                fun toInt(radix: Int): Int
                fun toIntOrNull(): Int?
                fun toIntOrNull(radix: Int): Int?
                fun toJvmInternalName(): String
                fun toLong(): Long
                fun toLong(radix: Int): Long
                fun toLongOrNull(): Long?
                fun toLongOrNull(radix: Int): Long?
                fun toLowerCase(): String
                fun toLowerCase(locale: java.util.Locale): String
                fun toPattern(flags: Int): java.util.regex.Pattern
                fun toRegex(): Regex
                fun toRegex(option: RegexOption): Regex
                fun toRegex(options: Set<RegexOption>): Regex
                fun toShort(): Short
                fun toShort(radix: Int): Short
                fun toShortOrNull(): Short?
                fun toShortOrNull(radix: Int): Short?
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
                fun toUpperCase(): String
                fun toUpperCase(locale: java.util.Locale): String
                fun translateEscapes(): String
                fun trim(predicate: Function1<Char, Boolean>): String
                fun trim(chars: CharArray): String
                fun trim(): String
                fun trimEnd(predicate: Function1<Char, Boolean>): String
                fun trimEnd(chars: CharArray): String
                fun trimEnd(): String
                fun trimIndent(): String
                fun trimMargin(marginPrefix: String): String
                fun trimStart(predicate: Function1<Char, Boolean>): String
                fun trimStart(chars: CharArray): String
                fun trimStart(): String
                fun uppercase(): String
                fun uppercase(locale: java.util.Locale): String
            }
            // from kotlin
            inline infix fun Comparable<T>.compareTo(other: T): Int
            // from kotlin.text
            val CharSequence.indices: IntRange
            val CharSequence.lastIndex: Int
            inline fun CharSequence.all(predicate: Function1<Char, Boolean>): Boolean
            fun CharSequence.any(): Boolean
            inline fun CharSequence.any(predicate: Function1<Char, Boolean>): Boolean
            fun CharSequence.asIterable(): Iterable<Char>
            fun CharSequence.asSequence(): Sequence<Char>
            inline fun CharSequence.associate(transform: Function1<Char, Pair<T, T>>): Map<T, T>
            inline fun CharSequence.associateBy(keySelector: Function1<Char, T>): Map<T, Char>
            inline fun CharSequence.associateBy(keySelector: Function1<Char, T>, valueTransform: Function1<Char, T>): Map<T, T>
            inline fun CharSequence.associateByTo(destination: T, keySelector: Function1<Char, T>): T
            inline fun CharSequence.associateByTo(destination: T, keySelector: Function1<Char, T>, valueTransform: Function1<Char, T>): T
            inline fun CharSequence.associateTo(destination: T, transform: Function1<Char, Pair<T, T>>): T
            inline fun CharSequence.associateWith(valueSelector: Function1<Char, T>): Map<Char, T>
            inline fun CharSequence.associateWithTo(destination: T, valueSelector: Function1<Char, T>): T
            fun CharSequence.chunked(size: Int): List<String>
            fun CharSequence.chunked(size: Int, transform: Function1<CharSequence, T>): List<T>
            fun CharSequence.chunkedSequence(size: Int): Sequence<String>
            fun CharSequence.chunkedSequence(size: Int, transform: Function1<CharSequence, T>): Sequence<T>
            fun CharSequence.commonPrefixWith(other: CharSequence, ignoreCase: Boolean): String
            fun CharSequence.commonSuffixWith(other: CharSequence, ignoreCase: Boolean): String
            operator fun CharSequence.contains(other: CharSequence, ignoreCase: Boolean): Boolean
            operator fun CharSequence.contains(char: Char, ignoreCase: Boolean): Boolean
            inline operator fun CharSequence.contains(regex: Regex): Boolean
            infix fun CharSequence?.contentEquals(other: CharSequence?): Boolean
            fun CharSequence?.contentEquals(other: CharSequence?, ignoreCase: Boolean): Boolean
            inline fun CharSequence.count(): Int
            inline fun CharSequence.count(predicate: Function1<Char, Boolean>): Int
            fun CharSequence.drop(n: Int): CharSequence
            fun CharSequence.dropLast(n: Int): CharSequence
            inline fun CharSequence.dropLastWhile(predicate: Function1<Char, Boolean>): CharSequence
            inline fun CharSequence.dropWhile(predicate: Function1<Char, Boolean>): CharSequence
            inline fun CharSequence.elementAt(index: Int): Char
            inline fun CharSequence.elementAtOrElse(index: Int, defaultValue: Function1<Int, Char>): Char
            inline fun CharSequence.elementAtOrNull(index: Int): Char?
            fun CharSequence.endsWith(char: Char, ignoreCase: Boolean): Boolean
            fun CharSequence.endsWith(suffix: CharSequence, ignoreCase: Boolean): Boolean
            inline fun CharSequence.filter(predicate: Function1<Char, Boolean>): CharSequence
            inline fun CharSequence.filterIndexed(predicate: Function2<Int, Char, Boolean>): CharSequence
            inline fun CharSequence.filterIndexedTo(destination: T, predicate: Function2<Int, Char, Boolean>): T
            inline fun CharSequence.filterNot(predicate: Function1<Char, Boolean>): CharSequence
            inline fun CharSequence.filterNotTo(destination: T, predicate: Function1<Char, Boolean>): T
            inline fun CharSequence.filterTo(destination: T, predicate: Function1<Char, Boolean>): T
            inline fun CharSequence.find(predicate: Function1<Char, Boolean>): Char?
            fun CharSequence.findAnyOf(strings: Collection<String>, startIndex: Int, ignoreCase: Boolean): Pair<Int, String>?
            inline fun CharSequence.findLast(predicate: Function1<Char, Boolean>): Char?
            fun CharSequence.findLastAnyOf(strings: Collection<String>, startIndex: Int, ignoreCase: Boolean): Pair<Int, String>?
            fun CharSequence.first(): Char
            inline fun CharSequence.first(predicate: Function1<Char, Boolean>): Char
            inline fun CharSequence.firstNotNullOf(transform: Function1<Char, T?>): T
            inline fun CharSequence.firstNotNullOfOrNull(transform: Function1<Char, T?>): T?
            fun CharSequence.firstOrNull(): Char?
            inline fun CharSequence.firstOrNull(predicate: Function1<Char, Boolean>): Char?
            inline fun CharSequence.flatMap(transform: Function1<Char, Iterable<T>>): List<T>
            inline fun CharSequence.flatMapIndexed(transform: Function2<Int, Char, Iterable<T>>): List<T>
            inline fun CharSequence.flatMapIndexedTo(destination: T, transform: Function2<Int, Char, Iterable<T>>): T
            inline fun CharSequence.flatMapTo(destination: T, transform: Function1<Char, Iterable<T>>): T
            inline fun CharSequence.fold(initial: T, operation: Function2<T, Char, T>): T
            inline fun CharSequence.foldIndexed(initial: T, operation: Function3<Int, T, Char, T>): T
            inline fun CharSequence.foldRight(initial: T, operation: Function2<Char, T, T>): T
            inline fun CharSequence.foldRightIndexed(initial: T, operation: Function3<Int, Char, T, T>): T
            inline fun CharSequence.forEach(action: Function1<Char, Unit>)
            inline fun CharSequence.forEachIndexed(action: Function2<Int, Char, Unit>)
            inline fun CharSequence.getOrElse(index: Int, defaultValue: Function1<Int, Char>): Char
            fun CharSequence.getOrNull(index: Int): Char?
            inline fun CharSequence.groupBy(keySelector: Function1<Char, T>): Map<T, List<Char>>
            inline fun CharSequence.groupBy(keySelector: Function1<Char, T>, valueTransform: Function1<Char, T>): Map<T, List<T>>
            inline fun CharSequence.groupByTo(destination: T, keySelector: Function1<Char, T>): T
            inline fun CharSequence.groupByTo(destination: T, keySelector: Function1<Char, T>, valueTransform: Function1<Char, T>): T
            inline fun CharSequence.groupingBy(keySelector: Function1<Char, T>): Grouping<Char, T>
            fun CharSequence.hasSurrogatePairAt(index: Int): Boolean
            fun CharSequence.indexOf(char: Char, startIndex: Int, ignoreCase: Boolean): Int
            fun CharSequence.indexOf(string: String, startIndex: Int, ignoreCase: Boolean): Int
            fun CharSequence.indexOfAny(chars: CharArray, startIndex: Int, ignoreCase: Boolean): Int
            fun CharSequence.indexOfAny(strings: Collection<String>, startIndex: Int, ignoreCase: Boolean): Int
            inline fun CharSequence.indexOfFirst(predicate: Function1<Char, Boolean>): Int
            inline fun CharSequence.indexOfLast(predicate: Function1<Char, Boolean>): Int
            fun CharSequence.isBlank(): Boolean
            inline fun CharSequence.isEmpty(): Boolean
            inline fun CharSequence.isNotBlank(): Boolean
            inline fun CharSequence.isNotEmpty(): Boolean
            inline fun CharSequence?.isNullOrBlank(): Boolean
            inline fun CharSequence?.isNullOrEmpty(): Boolean
            operator fun CharSequence.iterator(): CharIterator
            fun CharSequence.last(): Char
            inline fun CharSequence.last(predicate: Function1<Char, Boolean>): Char
            fun CharSequence.lastIndexOf(char: Char, startIndex: Int, ignoreCase: Boolean): Int
            fun CharSequence.lastIndexOf(string: String, startIndex: Int, ignoreCase: Boolean): Int
            fun CharSequence.lastIndexOfAny(chars: CharArray, startIndex: Int, ignoreCase: Boolean): Int
            fun CharSequence.lastIndexOfAny(strings: Collection<String>, startIndex: Int, ignoreCase: Boolean): Int
            fun CharSequence.lastOrNull(): Char?
            inline fun CharSequence.lastOrNull(predicate: Function1<Char, Boolean>): Char?
            fun CharSequence.lineSequence(): Sequence<String>
            fun CharSequence.lines(): List<String>
            inline fun CharSequence.map(transform: Function1<Char, T>): List<T>
            inline fun CharSequence.mapIndexed(transform: Function2<Int, Char, T>): List<T>
            inline fun CharSequence.mapIndexedNotNull(transform: Function2<Int, Char, T?>): List<T>
            inline fun CharSequence.mapIndexedNotNullTo(destination: T, transform: Function2<Int, Char, T?>): T
            inline fun CharSequence.mapIndexedTo(destination: T, transform: Function2<Int, Char, T>): T
            inline fun CharSequence.mapNotNull(transform: Function1<Char, T?>): List<T>
            inline fun CharSequence.mapNotNullTo(destination: T, transform: Function1<Char, T?>): T
            inline fun CharSequence.mapTo(destination: T, transform: Function1<Char, T>): T
            inline infix fun CharSequence.matches(regex: Regex): Boolean
            fun CharSequence.max(): Char?
            fun CharSequence.max(): Char
            inline fun CharSequence.maxBy(selector: Function1<Char, T>): Char?
            inline fun CharSequence.maxBy(selector: Function1<Char, T>): Char
            inline fun CharSequence.maxByOrNull(selector: Function1<Char, T>): Char?
            inline fun CharSequence.maxOf(selector: Function1<Char, Double>): Double
            inline fun CharSequence.maxOf(selector: Function1<Char, Float>): Float
            inline fun CharSequence.maxOf(selector: Function1<Char, T>): T
            inline fun CharSequence.maxOfOrNull(selector: Function1<Char, Double>): Double?
            inline fun CharSequence.maxOfOrNull(selector: Function1<Char, Float>): Float?
            inline fun CharSequence.maxOfOrNull(selector: Function1<Char, T>): T?
            inline fun CharSequence.maxOfWith(comparator: java.util.Comparator<in T>, selector: Function1<Char, T>): T
            inline fun CharSequence.maxOfWithOrNull(comparator: java.util.Comparator<in T>, selector: Function1<Char, T>): T?
            fun CharSequence.maxOrNull(): Char?
            fun CharSequence.maxWith(comparator: java.util.Comparator<in Char>): Char?
            fun CharSequence.maxWith(comparator: java.util.Comparator<in Char>): Char
            fun CharSequence.maxWithOrNull(comparator: java.util.Comparator<in Char>): Char?
            fun CharSequence.min(): Char?
            fun CharSequence.min(): Char
            inline fun CharSequence.minBy(selector: Function1<Char, T>): Char?
            inline fun CharSequence.minBy(selector: Function1<Char, T>): Char
            inline fun CharSequence.minByOrNull(selector: Function1<Char, T>): Char?
            inline fun CharSequence.minOf(selector: Function1<Char, Double>): Double
            inline fun CharSequence.minOf(selector: Function1<Char, Float>): Float
            inline fun CharSequence.minOf(selector: Function1<Char, T>): T
            inline fun CharSequence.minOfOrNull(selector: Function1<Char, Double>): Double?
            inline fun CharSequence.minOfOrNull(selector: Function1<Char, Float>): Float?
            inline fun CharSequence.minOfOrNull(selector: Function1<Char, T>): T?
            inline fun CharSequence.minOfWith(comparator: java.util.Comparator<in T>, selector: Function1<Char, T>): T
            inline fun CharSequence.minOfWithOrNull(comparator: java.util.Comparator<in T>, selector: Function1<Char, T>): T?
            fun CharSequence.minOrNull(): Char?
            fun CharSequence.minWith(comparator: java.util.Comparator<in Char>): Char?
            fun CharSequence.minWith(comparator: java.util.Comparator<in Char>): Char
            fun CharSequence.minWithOrNull(comparator: java.util.Comparator<in Char>): Char?
            fun CharSequence.none(): Boolean
            inline fun CharSequence.none(predicate: Function1<Char, Boolean>): Boolean
            fun CharSequence.padEnd(length: Int, padChar: Char): CharSequence
            fun CharSequence.padStart(length: Int, padChar: Char): CharSequence
            inline fun CharSequence.partition(predicate: Function1<Char, Boolean>): Pair<CharSequence, CharSequence>
            inline fun CharSequence.random(): Char
            fun CharSequence.random(random: Random): Char
            inline fun CharSequence.randomOrNull(): Char?
            fun CharSequence.randomOrNull(random: Random): Char?
            inline fun CharSequence.reduce(operation: Function2<Char, Char, Char>): Char
            inline fun CharSequence.reduceIndexed(operation: Function3<Int, Char, Char, Char>): Char
            inline fun CharSequence.reduceIndexedOrNull(operation: Function3<Int, Char, Char, Char>): Char?
            inline fun CharSequence.reduceOrNull(operation: Function2<Char, Char, Char>): Char?
            inline fun CharSequence.reduceRight(operation: Function2<Char, Char, Char>): Char
            inline fun CharSequence.reduceRightIndexed(operation: Function3<Int, Char, Char, Char>): Char
            inline fun CharSequence.reduceRightIndexedOrNull(operation: Function3<Int, Char, Char, Char>): Char?
            inline fun CharSequence.reduceRightOrNull(operation: Function2<Char, Char, Char>): Char?
            fun CharSequence.regionMatches(thisOffset: Int, other: CharSequence, otherOffset: Int, length: Int, ignoreCase: Boolean): Boolean
            fun CharSequence.removePrefix(prefix: CharSequence): CharSequence
            fun CharSequence.removeRange(startIndex: Int, endIndex: Int): CharSequence
            fun CharSequence.removeRange(range: IntRange): CharSequence
            fun CharSequence.removeSuffix(suffix: CharSequence): CharSequence
            fun CharSequence.removeSurrounding(prefix: CharSequence, suffix: CharSequence): CharSequence
            fun CharSequence.removeSurrounding(delimiter: CharSequence): CharSequence
            fun CharSequence.repeat(n: Int): String
            inline fun CharSequence.replace(regex: Regex, replacement: String): String
            inline fun CharSequence.replace(regex: Regex, transform: Function1<MatchResult, CharSequence>): String
            inline fun CharSequence.replaceFirst(regex: Regex, replacement: String): String
            fun CharSequence.replaceRange(startIndex: Int, endIndex: Int, replacement: CharSequence): CharSequence
            fun CharSequence.replaceRange(range: IntRange, replacement: CharSequence): CharSequence
            fun CharSequence.reversed(): CharSequence
            inline fun CharSequence.runningFold(initial: T, operation: Function2<T, Char, T>): List<T>
            inline fun CharSequence.runningFoldIndexed(initial: T, operation: Function3<Int, T, Char, T>): List<T>
            inline fun CharSequence.runningReduce(operation: Function2<Char, Char, Char>): List<Char>
            inline fun CharSequence.runningReduceIndexed(operation: Function3<Int, Char, Char, Char>): List<Char>
            inline fun CharSequence.scan(initial: T, operation: Function2<T, Char, T>): List<T>
            inline fun CharSequence.scanIndexed(initial: T, operation: Function3<Int, T, Char, T>): List<T>
            fun CharSequence.single(): Char
            inline fun CharSequence.single(predicate: Function1<Char, Boolean>): Char
            fun CharSequence.singleOrNull(): Char?
            inline fun CharSequence.singleOrNull(predicate: Function1<Char, Boolean>): Char?
            fun CharSequence.slice(indices: IntRange): CharSequence
            fun CharSequence.slice(indices: Iterable<Int>): CharSequence
            fun CharSequence.split(regex: java.util.regex.Pattern, limit: Int): List<String>
            fun CharSequence.split(delimiters: Array<out String>, ignoreCase: Boolean, limit: Int): List<String>
            fun CharSequence.split(delimiters: CharArray, ignoreCase: Boolean, limit: Int): List<String>
            inline fun CharSequence.split(regex: Regex, limit: Int): List<String>
            fun CharSequence.splitToSequence(delimiters: Array<out String>, ignoreCase: Boolean, limit: Int): Sequence<String>
            fun CharSequence.splitToSequence(delimiters: CharArray, ignoreCase: Boolean, limit: Int): Sequence<String>
            inline fun CharSequence.splitToSequence(regex: Regex, limit: Int): Sequence<String>
            fun CharSequence.startsWith(char: Char, ignoreCase: Boolean): Boolean
            fun CharSequence.startsWith(prefix: CharSequence, ignoreCase: Boolean): Boolean
            fun CharSequence.startsWith(prefix: CharSequence, startIndex: Int, ignoreCase: Boolean): Boolean
            fun CharSequence.subSequence(range: IntRange): CharSequence
            inline fun CharSequence.substring(startIndex: Int, endIndex: Int): String
            fun CharSequence.substring(range: IntRange): String
            inline fun CharSequence.sumBy(selector: Function1<Char, Int>): Int
            inline fun CharSequence.sumByDouble(selector: Function1<Char, Double>): Double
            inline fun CharSequence.sumOf(selector: Function1<Char, java.math.BigDecimal>): java.math.BigDecimal
            inline fun CharSequence.sumOf(selector: Function1<Char, java.math.BigInteger>): java.math.BigInteger
            inline fun CharSequence.sumOf(selector: Function1<Char, Double>): Double
            inline fun CharSequence.sumOf(selector: Function1<Char, Int>): Int
            inline fun CharSequence.sumOf(selector: Function1<Char, Long>): Long
            inline fun CharSequence.sumOf(selector: Function1<Char, UInt>): UInt
            inline fun CharSequence.sumOf(selector: Function1<Char, ULong>): ULong
            fun CharSequence.take(n: Int): CharSequence
            fun CharSequence.takeLast(n: Int): CharSequence
            inline fun CharSequence.takeLastWhile(predicate: Function1<Char, Boolean>): CharSequence
            inline fun CharSequence.takeWhile(predicate: Function1<Char, Boolean>): CharSequence
            fun CharSequence.toCollection(destination: T): T
            fun CharSequence.toHashSet(): java.util.HashSet<Char>
            fun CharSequence.toList(): List<Char>
            fun CharSequence.toMutableList(): MutableList<Char>
            fun CharSequence.toSet(): Set<Char>
            fun CharSequence.toSortedSet(): java.util.SortedSet<Char>
            inline fun CharSequence.trim(predicate: Function1<Char, Boolean>): CharSequence
            fun CharSequence.trim(chars: CharArray): CharSequence
            fun CharSequence.trim(): CharSequence
            inline fun CharSequence.trimEnd(predicate: Function1<Char, Boolean>): CharSequence
            fun CharSequence.trimEnd(chars: CharArray): CharSequence
            fun CharSequence.trimEnd(): CharSequence
            inline fun CharSequence.trimStart(predicate: Function1<Char, Boolean>): CharSequence
            fun CharSequence.trimStart(chars: CharArray): CharSequence
            fun CharSequence.trimStart(): CharSequence
            fun CharSequence.windowed(size: Int, step: Int, partialWindows: Boolean): List<String>
            fun CharSequence.windowed(size: Int, step: Int, partialWindows: Boolean, transform: Function1<CharSequence, T>): List<T>
            fun CharSequence.windowedSequence(size: Int, step: Int, partialWindows: Boolean): Sequence<String>
            fun CharSequence.windowedSequence(size: Int, step: Int, partialWindows: Boolean, transform: Function1<CharSequence, T>): Sequence<T>
            fun CharSequence.withIndex(): Iterable<IndexedValue<Char>>
            infix fun CharSequence.zip(other: CharSequence): List<Pair<Char, Char>>
            inline fun CharSequence.zip(other: CharSequence, transform: Function2<Char, Char, T>): List<T>
            fun CharSequence.zipWithNext(): List<Pair<Char, Char>>
            inline fun CharSequence.zipWithNext(transform: Function2<Char, Char, T>): List<T>
        """.trimIndent()
    }


    // TestClass tests

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

    @Test
    fun `should resolve class with cross-package type references`() {
        // when - CrossPackageTypeTest references types from test.other package
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.CrossPackageTypeTest"
        )

        // then - types from different packages should be fully qualified
        // so that an LLM can look them up or import them correctly
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.CrossPackageTypeTest [page 1/1]
            class CrossPackageTypeTest(val processor: com.xemantic.ai.golem.kotlin.metadata.test.other.CustomProcessor) {
                fun process(input: String): com.xemantic.ai.golem.kotlin.metadata.test.other.CustomResult
                fun processWithProcessor(processor: com.xemantic.ai.golem.kotlin.metadata.test.other.CustomProcessor, input: String): com.xemantic.ai.golem.kotlin.metadata.test.other.CustomResult
            }
        """.trimIndent()
    }

    // TestClassTypes tests

    @Test
    fun `should resolve data class`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.DataTestClass"
        )

        // then - data classes include generated methods like componentN, copy, equals, hashCode, toString
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.DataTestClass [page 1/1]
            data class DataTestClass(val id: Int, val name: String) {
                operator fun component1(): Int
                operator fun component2(): String
                fun copy(id: Int, name: String): DataTestClass
                open operator fun equals(other: Any?): Boolean
                open fun hashCode(): Int
                open fun toString(): String
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve value class`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.ValueTestClass"
        )

        // then - value classes include generated methods equals, hashCode, toString
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.ValueTestClass [page 1/1]
            value class ValueTestClass(val value: String) {
                open operator fun equals(other: Any?): Boolean
                open fun hashCode(): Int
                open fun toString(): String
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve object (singleton)`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.ObjectTestClass"
        )

        // then
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.ObjectTestClass [page 1/1]
            object ObjectTestClass {
                val name: String
                fun greet(): String
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve simple enum class`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.EnumTestClass"
        )

        // then - an LLM needs to see enum entries to know what values are available
        // Note: Enum<T> supertype is filtered out as redundant - enum class already implies it
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.EnumTestClass [page 1/1]
            enum class EnumTestClass {
                FIRST,
                SECOND,
                THIRD
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve rich enum class with constructor, properties and functions`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.RichEnumTestClass"
        )

        // then - an LLM needs to see constructor params, entries, properties and functions
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.RichEnumTestClass [page 1/1]
            enum class RichEnumTestClass(val code: Int, val label: String) {
                ALPHA,
                BETA,
                GAMMA;
                val displayName: String
                fun isFirst(): Boolean
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve annotation class`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.AnnotationTestClass"
        )

        // then - an LLM needs to see annotation parameters to know how to use the annotation
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.AnnotationTestClass [page 1/1]
            annotation class AnnotationTestClass(val message: String = "")
        """.trimIndent()
    }

    @Test
    fun `should resolve class with companion object`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.CompanionTestClass"
        )

        // then - an LLM needs to see companion object members to access static-like functionality
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.CompanionTestClass [page 1/1]
            class CompanionTestClass {
                companion object {
                    const val CONSTANT: String
                    fun create(): CompanionTestClass
                }
            }
        """.trimIndent()
    }

    // TestFunctionModifiers tests

    @Test
    fun `should resolve class with function modifiers`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.FunctionModifiersTestClass"
        )

        // then - functions are output in declaration order
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.FunctionModifiersTestClass [page 1/1]
            class FunctionModifiersTestClass {
                suspend fun fetchData(): String
                inline fun processData(block: Function0<Unit>)
                infix fun combine(other: String): String
                operator fun plus(other: FunctionModifiersTestClass): FunctionModifiersTestClass
                tailrec fun factorial(n: Int, acc: Int): Int
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve class with property modifiers`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.PropertyModifiersTestClass"
        )

        // then
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.PropertyModifiersTestClass [page 1/1]
            class PropertyModifiersTestClass {
                var lateInitProperty: String
                val lazyProperty: String
            }
        """.trimIndent()
    }

    // TestModifiers tests

    @Test
    fun `should resolve class with visibility modifiers`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.VisibilityTestClass"
        )

        // then - members are output in declaration order
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.VisibilityTestClass [page 1/1]
            class VisibilityTestClass {
                val publicProperty: String
                private val privateProperty: String
                protected val protectedProperty: String
                internal val internalProperty: String
                fun publicFunction(): String
                private fun privateFunction(): String
                protected fun protectedFunction(): String
                internal fun internalFunction(): String
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve open class`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.OpenTestClass"
        )

        // then
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.OpenTestClass [page 1/1]
            open class OpenTestClass {
                open val openProperty: String
                open fun openFunction(): String
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve sealed class`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.SealedTestClass"
        )

        // then - an LLM needs to see sealed subclasses to know what types are valid in when expressions
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.SealedTestClass [page 1/1]
            sealed class SealedTestClass {
                class SubClass1 : SealedTestClass
                class SubClass2 : SealedTestClass
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve nested class directly`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.SealedTestClass.SubClass1"
        )

        // then - nested class can be resolved independently to see its full details
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.SealedTestClass.SubClass1 [page 1/1]
            class SubClass1(val name: String) : SealedTestClass
        """.trimIndent()
    }

    @Test
    fun `should resolve abstract class`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.AbstractTestClass"
        )

        // then
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.AbstractTestClass [page 1/1]
            abstract class AbstractTestClass {
                abstract val abstractProperty: String
                open val openProperty: String
                abstract fun abstractFunction(): String
                open fun openFunction(): String
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve class with override modifier`() {
        // when
        val signature = resolver.resolve(
            "com.xemantic.ai.golem.kotlin.metadata.test.OverrideTestClass"
        )

        // then - compareTo from Comparable is an operator function
        signature sameAs /* language=kotlin */ """
            // com.xemantic.ai.golem.kotlin.metadata.test.OverrideTestClass [page 1/1]
            class OverrideTestClass : Comparable<OverrideTestClass> {
                override operator fun compareTo(other: OverrideTestClass): Int
            }
        """.trimIndent()
    }

    // search() tests

    @Test
    fun `search should find class by partial name match`() {
        // when
        val result = resolver.search("TestClass")

        // then - should find our test classes
        result sameAs /* language=kotlin */ """
            // search results for "TestClass" [page 1/2]
            com.xemantic.ai.golem.kotlin.metadata.test.AbstractTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.AnnotationTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.CompanionTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.DataTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.EnumTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.FunctionModifiersTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.ObjectTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.OpenTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.OverrideTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.PropertyModifiersTestClass
        """.trimIndent()
    }

    @Test
    fun `search should be case-insensitive`() {
        // when
        val result = resolver.search("TESTCLASS")

        // then - should still find test classes despite uppercase query
        result sameAs /* language=kotlin */ """
            // search results for "TESTCLASS" [page 1/2]
            com.xemantic.ai.golem.kotlin.metadata.test.AbstractTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.AnnotationTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.CompanionTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.DataTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.EnumTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.FunctionModifiersTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.ObjectTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.OpenTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.OverrideTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.PropertyModifiersTestClass
        """.trimIndent()
    }

    @Test
    fun `search should return null when no matches found`() {
        // when
        val result = resolver.search("XyzNonExistentClass12345")

        // then
        assert(result == null)
    }

    @Test
    fun `search should support pagination with pageSize`() {
        // when
        val result = resolver.search("TestClass", pageSize = 3)

        // then - should show first 3 results (15 total classes, 5 pages)
        result sameAs /* language=kotlin */ """
            // search results for "TestClass" [page 1/5]
            com.xemantic.ai.golem.kotlin.metadata.test.AbstractTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.AnnotationTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.CompanionTestClass
        """.trimIndent()
    }

    @Test
    fun `search should support pagination with page parameter`() {
        // when
        val result = resolver.search("TestClass", page = 2, pageSize = 3)

        // then - should show results 4-6 (15 total classes, 5 pages)
        result sameAs /* language=kotlin */ """
            // search results for "TestClass" [page 2/5]
            com.xemantic.ai.golem.kotlin.metadata.test.DataTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.EnumTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.FunctionModifiersTestClass
        """.trimIndent()
    }

    @Test
    fun `search should find enum class`() {
        // when
        val result = resolver.search("EnumTestClass")

        // then
        result sameAs /* language=kotlin */ """
            // search results for "EnumTestClass" [page 1/1]
            com.xemantic.ai.golem.kotlin.metadata.test.EnumTestClass
            com.xemantic.ai.golem.kotlin.metadata.test.RichEnumTestClass
        """.trimIndent()
    }

    @Test
    fun `search should find annotation class`() {
        // when
        val result = resolver.search("AnnotationTestClass")

        // then
        result sameAs /* language=kotlin */ """
            // search results for "AnnotationTestClass" [page 1/1]
            com.xemantic.ai.golem.kotlin.metadata.test.AnnotationTestClass
        """.trimIndent()
    }

    @Test
    fun `search should find value class`() {
        // when
        val result = resolver.search("ValueTestClass")

        // then
        result sameAs /* language=kotlin */ """
            // search results for "ValueTestClass" [page 1/1]
            com.xemantic.ai.golem.kotlin.metadata.test.ValueTestClass
        """.trimIndent()
    }

}
