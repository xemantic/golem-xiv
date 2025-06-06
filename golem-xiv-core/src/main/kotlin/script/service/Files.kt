/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.script.service

import com.xemantic.ai.golem.api.backend.script.FileEntry
import com.xemantic.ai.golem.api.backend.script.Files
import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

class DefaultFiles : Files {

    override fun list(dir: String) = listDirectoryFlat(dir).map {
        FileEntry(
            path = it.absoluteFile.absolutePath,
            isDirectory = it.isDirectory
        )
    }

    override fun readText(
        vararg paths: String
    ): List<String> = paths.map { File(it).readText() }

    override fun readBinary(
        vararg paths: String
    ): List<ByteArray> = paths.map { File(it).readBytes() }

    override fun create(path: String, content: String) {
        File(path).ensureParentDir().writeText(content)
    }

    override fun create(path: String, content: ByteArray) {
        File(path).ensureParentDir().writeBytes(content)
    }

}

private fun File.ensureParentDir(): File {
    val parent = absoluteFile.parentFile
    if (parent.exists()) {
        if (!parent.isDirectory) {
            throw IllegalStateException("Cannot create because parent path is not a directory: $this")
        }
    } else {
        if (!parentFile.mkdirs()) {
            throw IOException("Cannot create parent directory of file: $this")
        }
    }
    return this
}

/**
 * Lists the contents of a directory recursively and returns a flat list,
 * respecting .gitignore files in each directory.
 *
 * @param directoryPath Path to the directory as a string
 * @param includeDirectories Whether to include directory entries in the output list
 * @return A flat list of all files (and optionally directories) that aren't ignored
 */
fun listDirectoryFlat(directoryPath: String, includeDirectories: Boolean = true): List<File> {
    val directory = File(directoryPath)

    // Check if path exists and is a directory
    if (!directory.exists()) {
        println("ERROR: Path does not exist: $directoryPath")
        return emptyList()
    }
    if (!directory.isDirectory) {
        println("ERROR: Path is not a directory: $directoryPath")
        return emptyList()
    }

    val result = mutableListOf<File>()

    // Parse and store GitIgnore rules from the top directory
    val ignoreRulesStack = mutableListOf<IgnoreRules>()
    val topLevelRules = parseGitIgnoreFile(directory)
    ignoreRulesStack.add(topLevelRules)

    // Helper function to traverse directories recursively
    fun traverse(dir: File, relativePath: String = "") {
        // Check if this directory itself should be ignored
        if (relativePath.isNotEmpty() && isIgnored(dir, relativePath, ignoreRulesStack)) {
            return
        }

        // Add the current directory to the result if includeDirectories is true
        if (includeDirectories) {
            result.add(dir)
        }

        // Check for a .gitignore file in this directory and parse it
        val gitIgnoreFile = File(dir, ".gitignore")
        if (gitIgnoreFile.exists()) {
            val newRules = parseGitIgnoreFile(dir)
            ignoreRulesStack.add(newRules)
        }

        // List all files and directories in the current directory
        dir.listFiles()?.let { files ->
            for (file in files) {
                if (file.name.startsWith(".")) continue

                val childRelativePath = if (relativePath.isEmpty()) file.name
                else "$relativePath/${file.name}"

                // Skip if this file/directory should be ignored
                if (isIgnored(file, childRelativePath, ignoreRulesStack)) {
                    continue
                }

                if (file.isDirectory) {
                    // Recursively traverse subdirectories
                    traverse(file, childRelativePath)
                } else {
                    // Add files to the result
                    result.add(file)
                }
            }
        } ?: println("WARNING: Could not access contents of directory: ${dir.absolutePath}")

        // Remove the rules from this directory when going up
        if (gitIgnoreFile.exists()) {
            ignoreRulesStack.removeAt(ignoreRulesStack.size - 1)
        }
    }

    // Start traversal from the given directory
    traverse(directory)

    return result
}

/**
 * Represents a set of ignore rules parsed from a .gitignore file
 */
data class IgnoreRules(
    val patterns: List<String>,
    val negatedPatterns: List<String>,
    val baseDir: File
)

/**
 * Parses a .gitignore file in the given directory
 */
fun parseGitIgnoreFile(directory: File): IgnoreRules {
    val gitIgnoreFile = File(directory, ".gitignore")
    val patterns = mutableListOf<String>()
    val negatedPatterns = mutableListOf<String>()

    if (gitIgnoreFile.exists()) {
        gitIgnoreFile.forEachLine { line ->
            // Skip empty lines and comments
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                if (trimmedLine.startsWith("!")) {
                    // This is a negated pattern
                    negatedPatterns.add(trimmedLine.substring(1))
                } else {
                    patterns.add(trimmedLine)
                }
            }
        }
    }

    return IgnoreRules(patterns, negatedPatterns, directory)
}

/**
 * Converts a gitignore pattern to a PathMatcher
 */
fun createPathMatcher(pattern: String): PathMatcher {
    val syntaxAndPattern = "glob:$pattern"
    return FileSystems.getDefault().getPathMatcher(syntaxAndPattern)
}

/**
 * Checks if a file or directory should be ignored based on the current ignore rules
 */
fun isIgnored(file: File, relativePath: String, ignoreRulesStack: List<IgnoreRules>): Boolean {
    // First, check if the file is explicitly included by any negated pattern
    for (rules in ignoreRulesStack) {
        for (pattern in rules.negatedPatterns) {
            val matcher = createPathMatcher(pattern)
            if (matcher.matches(Path.of(relativePath))) {
                return false  // File is explicitly included, so don't ignore
            }
        }
    }

    // Then check if it matches any ignore pattern
    for (rules in ignoreRulesStack) {
        for (pattern in rules.patterns) {
            val matcher = createPathMatcher(pattern)
            if (matcher.matches(Path.of(relativePath))) {
                return true  // File matches an ignore pattern
            }

            // Special handling for directory patterns (ending with /)
            if (file.isDirectory && pattern.endsWith("/")) {
                val directoryPattern = if (pattern.startsWith("/")) pattern.substring(1) else pattern
                val matcher2 = createPathMatcher(directoryPattern.removeSuffix("/"))
                if (matcher2.matches(Path.of(relativePath))) {
                    return true
                }
            }
        }
    }

    return false
}

