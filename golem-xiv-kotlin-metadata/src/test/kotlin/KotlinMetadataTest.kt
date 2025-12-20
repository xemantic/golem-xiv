
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

        // then
        signature sameAs /* language=kotlin */ """
            // kotlin.String [page 1/19]
            class String : Comparable<String>, CharSequence {
                fun byteInputStream(charset: Charset): ByteArrayInputStream
                fun capitalize(): String
                fun capitalize(locale: Locale): String
                fun chars(): IntStream
                fun codePointAt(index: Int): Int
                fun codePointBefore(index: Int): Int
                fun codePointCount(beginIndex: Int, endIndex: Int): Int
                fun codePoints(): IntStream
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
            // kotlin.String [page 2/19]
            class String : Comparable<String>, CharSequence {
                fun contentEquals(charSequence: CharSequence): Boolean
                fun contentEquals(stringBuilder: StringBuffer): Boolean
                fun decapitalize(): String
                fun decapitalize(locale: Locale): String
                fun describeConstable(): Optional<String>
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
            // kotlin.String [page 3/19]
            class String : Comparable<String>, CharSequence {
                fun encodeToByteArray(startIndex: Int, endIndex: Int, throwOnInvalidSequence: Boolean): ByteArray
                fun endsWith(suffix: String, ignoreCase: Boolean): Boolean
                operator fun equals(other: Any?): Boolean
                fun equals(other: String?, ignoreCase: Boolean): Boolean
                fun filter(predicate: Function1<Char, Boolean>): String
                fun filterIndexed(predicate: Function2<Int, Char, Boolean>): String
                fun filterNot(predicate: Function1<Char, Boolean>): String
                fun format(args: Array<out Any?>): String
                fun format(locale: Locale?, args: Array<out Any?>): String
                operator fun get(index: Int): Char
            }
        """.trimIndent()
    }

    @Test
    fun `should resolve String as known stdlib class - page 19 (last page with extensions)`() {
        // when
        val signature = resolver.resolve(
            "kotlin.String",
            page = 19
        )

        // then - verify last page includes supertype extensions
        signature sameAs /* language=kotlin */ """
            // kotlin.String [page 19/19]
            class String : Comparable<String>, CharSequence {
                fun uppercase(locale: Locale): String
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
            inline fun CharSequence.maxOfWith(comparator: Comparator<in T>, selector: Function1<Char, T>): T
            inline fun CharSequence.maxOfWithOrNull(comparator: Comparator<in T>, selector: Function1<Char, T>): T?
            fun CharSequence.maxOrNull(): Char?
            fun CharSequence.maxWith(comparator: Comparator<in Char>): Char?
            fun CharSequence.maxWith(comparator: Comparator<in Char>): Char
            fun CharSequence.maxWithOrNull(comparator: Comparator<in Char>): Char?
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
            inline fun CharSequence.minOfWith(comparator: Comparator<in T>, selector: Function1<Char, T>): T
            inline fun CharSequence.minOfWithOrNull(comparator: Comparator<in T>, selector: Function1<Char, T>): T?
            fun CharSequence.minOrNull(): Char?
            fun CharSequence.minWith(comparator: Comparator<in Char>): Char?
            fun CharSequence.minWith(comparator: Comparator<in Char>): Char
            fun CharSequence.minWithOrNull(comparator: Comparator<in Char>): Char?
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
            fun CharSequence.split(regex: Pattern, limit: Int): List<String>
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
            inline fun CharSequence.sumOf(selector: Function1<Char, BigDecimal>): BigDecimal
            inline fun CharSequence.sumOf(selector: Function1<Char, BigInteger>): BigInteger
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
            fun CharSequence.toHashSet(): HashSet<Char>
            fun CharSequence.toList(): List<Char>
            fun CharSequence.toMutableList(): MutableList<Char>
            fun CharSequence.toSet(): Set<Char>
            fun CharSequence.toSortedSet(): SortedSet<Char>
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
                fun byteInputStream(charset: Charset): ByteArrayInputStream
                fun capitalize(): String
                fun capitalize(locale: Locale): String
                fun chars(): IntStream
                fun codePointAt(index: Int): Int
                fun codePointBefore(index: Int): Int
                fun codePointCount(beginIndex: Int, endIndex: Int): Int
                fun codePoints(): IntStream
                operator fun compareTo(other: String): Int
                fun compareTo(other: String, ignoreCase: Boolean): Int
                fun contentEquals(charSequence: CharSequence): Boolean
                fun contentEquals(stringBuilder: StringBuffer): Boolean
                fun decapitalize(): String
                fun decapitalize(locale: Locale): String
                fun describeConstable(): Optional<String>
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
                fun format(locale: Locale?, args: Array<out Any?>): String
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
                fun lowercase(locale: Locale): String
                fun offsetByCodePoints(index: Int, codePointOffset: Int): Int
                fun orEmpty(): String
                fun padEnd(length: Int, padChar: Char): String
                fun padStart(length: Int, padChar: Char): String
                fun partition(predicate: Function1<Char, Boolean>): Pair<String, String>
                operator fun plus(other: Any?): String
                fun prependIndent(indent: String): String
                fun reader(): StringReader
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
                fun toBigDecimal(): BigDecimal
                fun toBigDecimal(mathContext: MathContext): BigDecimal
                fun toBigDecimalOrNull(): BigDecimal?
                fun toBigDecimalOrNull(mathContext: MathContext): BigDecimal?
                fun toBigInteger(): BigInteger
                fun toBigInteger(radix: Int): BigInteger
                fun toBigIntegerOrNull(): BigInteger?
                fun toBigIntegerOrNull(radix: Int): BigInteger?
                fun toBoolean(): Boolean
                fun toBooleanStrict(): Boolean
                fun toBooleanStrictOrNull(): Boolean?
                fun toByte(): Byte
                fun toByte(radix: Int): Byte
                fun toByteArray(charset: Charset): ByteArray
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
                fun toLowerCase(locale: Locale): String
                fun toPattern(flags: Int): Pattern
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
                fun toUpperCase(locale: Locale): String
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
                fun uppercase(locale: Locale): String
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
            inline fun CharSequence.maxOfWith(comparator: Comparator<in T>, selector: Function1<Char, T>): T
            inline fun CharSequence.maxOfWithOrNull(comparator: Comparator<in T>, selector: Function1<Char, T>): T?
            fun CharSequence.maxOrNull(): Char?
            fun CharSequence.maxWith(comparator: Comparator<in Char>): Char?
            fun CharSequence.maxWith(comparator: Comparator<in Char>): Char
            fun CharSequence.maxWithOrNull(comparator: Comparator<in Char>): Char?
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
            inline fun CharSequence.minOfWith(comparator: Comparator<in T>, selector: Function1<Char, T>): T
            inline fun CharSequence.minOfWithOrNull(comparator: Comparator<in T>, selector: Function1<Char, T>): T?
            fun CharSequence.minOrNull(): Char?
            fun CharSequence.minWith(comparator: Comparator<in Char>): Char?
            fun CharSequence.minWith(comparator: Comparator<in Char>): Char
            fun CharSequence.minWithOrNull(comparator: Comparator<in Char>): Char?
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
            fun CharSequence.split(regex: Pattern, limit: Int): List<String>
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
            inline fun CharSequence.sumOf(selector: Function1<Char, BigDecimal>): BigDecimal
            inline fun CharSequence.sumOf(selector: Function1<Char, BigInteger>): BigInteger
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
            fun CharSequence.toHashSet(): HashSet<Char>
            fun CharSequence.toList(): List<Char>
            fun CharSequence.toMutableList(): MutableList<Char>
            fun CharSequence.toSet(): Set<Char>
            fun CharSequence.toSortedSet(): SortedSet<Char>
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
