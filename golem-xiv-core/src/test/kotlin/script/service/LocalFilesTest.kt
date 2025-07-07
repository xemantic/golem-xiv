/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.script.service

import com.xemantic.ai.golem.api.backend.script.Files
import com.xemantic.kotlin.test.assert
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class LocalFilesTest {

    // given
    private val files: Files = LocalFiles()

    // create operations test
    @Test
    fun `should create text file`(@TempDir tempDir: Path) = runTest {
        // when
        files.create("$tempDir/foo.txt", "bar")

        // then
        assert(File("$tempDir/foo.txt").readText() == "bar")
    }

    @Test
    fun `should create text file and parent dir`(@TempDir tempDir: Path) = runTest {
        // when
        files.create("$tempDir/buzz/foo.txt", "bar")

        // then
        assert(File("$tempDir/buzz/foo.txt").readText() == "bar")
    }

    @Test
    fun `should create deeply nested directories`(@TempDir tempDir: Path) = runTest {
        // when
        files.create("$tempDir/a/b/c/d/e/file.txt", "content")

        // then
        assert(File("$tempDir/a/b/c/d/e/file.txt").readText() == "content")
    }

    @Test
    fun `should create binary file`(@TempDir tempDir: Path) = runTest {
        // given
        val binaryContent = byteArrayOf(0x01, 0x02, 0x03, 0xFF.toByte())

        // when
        files.create("$tempDir/data.bin", binaryContent)

        // then
        assert(File("$tempDir/data.bin").readBytes().contentEquals(binaryContent))
    }

    @Test
    fun `should create binary file and parent dir`(@TempDir tempDir: Path) = runTest {
        // given
        val binaryContent = byteArrayOf(0x01, 0x02, 0x03, 0xFF.toByte())

        // when
        files.create("$tempDir/buzz/data.bin", binaryContent)

        // then
        assert(File("$tempDir/buzz/data.bin").readBytes().contentEquals(binaryContent))
    }

    @Test
    fun `should overwrite existing file`(@TempDir tempDir: Path) = runTest {
        // given
        files.create("$tempDir/file.txt", "original")

        // when
        files.create("$tempDir/file.txt", "updated")

        // then
        assert(File("$tempDir/file.txt").readText() == "updated")
    }

    // Read operations tests
    @Test
    fun `should read text file`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/test.txt").writeText("hello world")

        // when
        val content = files.read("$tempDir/test.txt")

        // then
        assert(content == "hello world")
    }

    @Test
    fun `should read binary file`(@TempDir tempDir: Path) = runTest {
        // given
        val binaryData = byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte())
        File("$tempDir/data.bin").writeBytes(binaryData)

        // when
        val content = files.readBinary("$tempDir/data.bin")

        // then
        assert(content.contentEquals(binaryData))
    }

    @Test
    fun `should handle reading non-existent file`(@TempDir tempDir: Path) = runTest {
        // when/then
        assertFailsWith<Exception> {
            files.read("$tempDir/non-existent.txt")
        }
    }

    @Test
    fun `should handle reading directory as file`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/dir").mkdir()

        // when/then
        assertFailsWith<Exception> {
            files.read("$tempDir/dir")
        }
    }

    // Exists operations tests
    @Test
    fun `should check file exists`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/exists.txt").writeText("content")

        // when/then
        assert(files.exists("$tempDir/exists.txt"))
        assert(!files.exists("$tempDir/not-exists.txt"))
    }

    @Test
    fun `should check directory exists`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/dir").mkdir()

        // when/then
        assert(files.exists("$tempDir/dir"))
    }

    // Delete operations tests
    @Test
    fun `should delete file`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/delete-me.txt").writeText("content")

        // when
        val deleted = files.delete("$tempDir/delete-me.txt")

        // then
        assertTrue(deleted)
        assertFalse(File("$tempDir/delete-me.txt").exists())
    }

    @Test
    fun `should delete empty directory`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/empty-dir").mkdir()

        // when
        val deleted = files.delete("$tempDir/empty-dir")

        // then
        assertTrue(deleted)
        assertFalse(File("$tempDir/empty-dir").exists())
    }

    @Test
    fun `should recursively delete directory with contents`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/dir/subdir").mkdirs()
        File("$tempDir/dir/file1.txt").writeText("content1")
        File("$tempDir/dir/subdir/file2.txt").writeText("content2")

        // when
        val deleted = files.delete("$tempDir/dir")

        // then
        assertTrue(deleted)
        assertFalse(File("$tempDir/dir").exists())
    }

    @Test
    fun `should return false when deleting non-existent file`(@TempDir tempDir: Path) = runTest {
        // when
        val deleted = files.delete("$tempDir/non-existent.txt")

        // then
        assertFalse(deleted)
    }

    // List operations tests
    @Test
    fun `should list files at depth 0`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/file1.txt").writeText("content")
        File("$tempDir/file2.txt").writeText("content")
        File("$tempDir/dir1").mkdir()

        // when
        val entries = files.list("$tempDir", depth = 0).toList()

        // then
        assert(entries.size == 3)
        assert(entries.any { it.path.endsWith("file1.txt") && !it.isDirectory })
        assert(entries.any { it.path.endsWith("file2.txt") && !it.isDirectory })
        assert(entries.any { it.path.endsWith("dir1") && it.isDirectory })
    }

    @Test
    fun `should list files recursively with depth`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/file.txt").writeText("content")
        File("$tempDir/dir/subfile.txt").also {
            it.parentFile.mkdirs()
            it.writeText("content")
        }
        File("$tempDir/dir/subdir/deepfile.txt").also {
            it.parentFile.mkdirs()
            it.writeText("content")
        }

        // when
        val depth1 = files.list("$tempDir", depth = 1).toList()
        val depth2 = files.list("$tempDir", depth = 2).toList()

        // then
        // Depth 1 should include immediate children and one level deep
        assert(depth1.any { it.path.endsWith("file.txt") })
        assert(depth1.any { it.path.endsWith("dir") })
        assert(depth1.any { it.path.endsWith("subfile.txt") })
        assert(depth1.any { it.path.endsWith("subdir") })
        assert(!depth1.any { it.path.endsWith("deepfile.txt") })

        // Depth 2 should include everything
        assert(depth2.any { it.path.endsWith("deepfile.txt") })
    }

    @Test
    fun `should exclude hidden files when requested`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/.hidden").writeText("content")
        File("$tempDir/visible.txt").writeText("content")

        // when
        val withHidden = files.list("$tempDir", excludeHidden = false).toList()
        val withoutHidden = files.list("$tempDir", excludeHidden = true).toList()

        // then
        assert(withHidden.size == 2)
        assert(withoutHidden.size == 1)
        assert(withoutHidden.all { !it.path.contains(".hidden") })
    }

    @Test
    fun `should respect gitignore when listing files`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/.gitignore").writeText("ignored.txt\n*.log")
        File("$tempDir/ignored.txt").writeText("content")
        File("$tempDir/test.log").writeText("content")
        File("$tempDir/included.txt").writeText("content")

        // when
        val entries = files.list("$tempDir", excludeHidden = true).toList()

        // then
        assert(!entries.any { it.path.endsWith("ignored.txt") })
        assert(!entries.any { it.path.endsWith("test.log") })
        assert(entries.any { it.path.endsWith("included.txt") })
    }

    @Test
    fun `should handle listing empty directory`(@TempDir tempDir: Path) = runTest {
        // given
        File("$tempDir/empty").mkdir()

        // when
        val entries = files.list("$tempDir/empty").toList()

        // then
        assert(entries.isEmpty())
    }

    @Test
    fun `should handle listing non-existent directory`(@TempDir tempDir: Path) = runTest {
        // when/then
        assertFailsWith<Exception> {
            files.list("$tempDir/non-existent").toList()
        }
    }

    // Edge cases and special scenarios
    @Test
    fun `should handle files with special characters`(@TempDir tempDir: Path) = runTest {
        // given
        val specialName = "file with spaces & special!@#$%^&()_+chars.txt"

        // when
        files.create("$tempDir/$specialName", "content")

        // then
        assertTrue(files.exists("$tempDir/$specialName"))
        assert(files.read("$tempDir/$specialName") == "content")
    }

    @Test
    fun `should handle empty file operations`(@TempDir tempDir: Path) = runTest {
        // when
        files.create("$tempDir/empty.txt", "")
        files.create("$tempDir/empty.bin", byteArrayOf())

        // then
        assert(files.read("$tempDir/empty.txt") == "")
        assert(files.readBinary("$tempDir/empty.bin").isEmpty())
    }

    @Test
    fun `should handle large file operations`(@TempDir tempDir: Path) = runTest {
        // given
        val largeContent = "x".repeat(1_000_000) // 1MB of 'x' characters

        // when
        files.create("$tempDir/large.txt", largeContent)

        // then
        assert(files.read("$tempDir/large.txt") == largeContent)
    }

    @Test
    fun `should preserve file content encoding`(@TempDir tempDir: Path) = runTest {
        // given
        val unicodeContent = "Hello 世界 🌍 Привет"

        // when
        files.create("$tempDir/unicode.txt", unicodeContent)

        // then
        assert(files.read("$tempDir/unicode.txt") == unicodeContent)
    }

}
