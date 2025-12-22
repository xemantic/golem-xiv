package com.xemantic.ai.golem.kotlin.metadata.test

/**
 * Example extension function for testing metadata resolution.
 */
fun String.wordCount(): Int = split("\\s+".toRegex()).size
