/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.script.service

import com.xemantic.ai.golem.api.backend.script.FileEntry
import com.xemantic.ai.golem.api.backend.script.Files
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class LocalFiles : Files {

    override suspend fun list(
        dir: String,
        depth: Int,
        excludeHidden: Boolean
    ): Flow<FileEntry> = flow {
        val rootDir = File(dir)
        if (!rootDir.exists()) {
            throw IllegalArgumentException("Directory does not exist: $dir")
        }
        if (!rootDir.isDirectory) {
            throw IllegalArgumentException("Path is not a directory: $dir")
        }

        // Load .gitignore patterns if excludeHidden is true
        val gitignorePatterns = if (excludeHidden) {
            loadGitignorePatterns(rootDir)
        } else {
            emptyList()
        }

        // Traverse directory tree up to specified depth
        traverseDirectory(rootDir, rootDir, 0, depth, excludeHidden, gitignorePatterns)
            .forEach { emit(it) }
    }

    override suspend fun read(path: String): String {
        val file = File(path)
        return when {
            !file.exists() -> throw IllegalArgumentException("File does not exist: $path")
            file.isDirectory -> throw IllegalArgumentException("Cannot read directory as file: $path")
            else -> file.readText()
        }
    }

    override suspend fun readBinary(path: String): ByteArray {
        val file = File(path)
        return when {
            !file.exists() -> throw IllegalArgumentException("File does not exist: $path")
            file.isDirectory -> throw IllegalArgumentException("Cannot read directory as file: $path")
            else -> file.readBytes()
        }
    }

    override suspend fun create(path: String, content: String) {
        val file = File(path)
        // Create parent directories if they don't exist
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    override suspend fun create(path: String, content: ByteArray) {
        val file = File(path)
        // Create parent directories if they don't exist
        file.parentFile?.mkdirs()
        file.writeBytes(content)
    }

    override suspend fun exists(
        path: String
    ): Boolean = File(path).exists()

    override suspend fun delete(path: String): Boolean {
        val file = File(path)
        if (!file.exists()) {
            return false
        }

        // Use recursive deletion for directories
        return if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    private fun traverseDirectory(
        rootDir: File,
        currentDir: File,
        currentDepth: Int,
        maxDepth: Int,
        excludeHidden: Boolean,
        gitignorePatterns: List<GitignorePattern>
    ): List<FileEntry> {
        val entries = mutableListOf<FileEntry>()

        currentDir.listFiles()?.forEach { file ->
            val relativePath = rootDir.toPath().relativize(file.toPath()).toString()

            // Check if file should be excluded
            if (shouldExclude(file, relativePath, excludeHidden, gitignorePatterns)) {
                return@forEach
            }

            entries.add(FileEntry(file.path, file.isDirectory))

            // Recursively traverse subdirectories if within depth limit
            if (file.isDirectory && currentDepth < maxDepth) {
                entries.addAll(
                    traverseDirectory(
                        rootDir,
                        file,
                        currentDepth + 1,
                        maxDepth,
                        excludeHidden,
                        gitignorePatterns
                    )
                )
            }
        }

        return entries
    }

    private fun shouldExclude(
        file: File,
        relativePath: String,
        excludeHidden: Boolean,
        gitignorePatterns: List<GitignorePattern>
    ): Boolean {
        // Check hidden files
        if (excludeHidden) {
            // Unix-style hidden files start with .
            if (file.name.startsWith(".")) {
                return true
            }

            // Check gitignore patterns
            if (gitignorePatterns.any { it.matches(relativePath) }) {
                return true
            }
        }

        return false
    }

    private fun loadGitignorePatterns(dir: File): List<GitignorePattern> {
        val gitignoreFile = File(dir, ".gitignore")
        if (!gitignoreFile.exists()) {
            return emptyList()
        }

        return gitignoreFile.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .map { GitignorePattern(it.trim()) }
    }

    private class GitignorePattern(private val pattern: String) {
        fun matches(path: String): Boolean {
            // Simple gitignore pattern matching
            // This is a simplified version - real gitignore is more complex
            return when {
                // Exact match
                pattern == path -> true

                // Wildcard patterns
                pattern.contains("*") -> {
                    val regex = pattern
                        .replace(".", "\\.")
                        .replace("*", ".*")
                        .replace("?", ".")
                    path.matches(Regex(regex))
                }

                // Directory patterns (ending with /)
                pattern.endsWith("/") -> {
                    path.startsWith(pattern.removeSuffix("/"))
                }

                // File/directory name anywhere in path
                else -> {
                    path.split("/").any { it == pattern }
                }
            }
        }
    }

}
