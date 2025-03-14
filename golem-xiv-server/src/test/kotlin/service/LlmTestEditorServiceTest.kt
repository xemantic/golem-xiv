///*
// * Copyright 2025 Kazimierz Pogoda / Xemantic
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.xemantic.ai.golem.service
//
//import com.xemantic.ai.golem.service.impl.DefaultLlmTestEditorService
//import kotlinx.coroutines.runBlocking
//import kotlin.io.path.createTempDirectory
//import kotlin.io.path.deleteRecursively
//import kotlin.io.path.writeText
//import kotlin.test.AfterTest
//import kotlin.test.BeforeTest
//import kotlin.test.Test
//import java.nio.file.Path
//import kotlin.io.path.ExperimentalPathApi
//
//class LlmTextEditorServiceTest {
//
//    private lateinit var editor: LlmTextEditorService
//
//    private lateinit var tempDir: Path
//    private lateinit var testFile: Path
//    private lateinit var testFilePath: String
//    private lateinit var testFileContent: String
//
//    @BeforeTest
//    fun setup() {
//        editor = DefaultLlmTestEditorService()
//
//        // Create a temp directory
//        tempDir = createTempDirectory("llm-editor-test")
//
//        // Create a test file
//        testFile = tempDir.resolve("test.txt")
//        testFileContent = """
//            Line 1: This is a test file
//            Line 2: It contains multiple lines
//            Line 3: For testing the editor
//            Line 4: With some content to modify
//            Line 5: And a final line
//        """.trimIndent()
//
//        testFile.writeText(testFileContent)
//        testFilePath = testFile.toAbsolutePath().toString()
//    }
//
//    @OptIn(ExperimentalPathApi::class)
//    @AfterTest
//    fun cleanup() {
//        tempDir.deleteRecursively()
//    }
//
//    // ===== BASIC READ/WRITE TESTS =====
//
//    @Test
//    fun `read resource should return file content`() = runTest {
//        val content = editor.readResource(testFilePath)
//        assert(content == testFileContent)
//    }
//
//    @Test
//    fun `write resource should update file content`() = runTest {
//        val newContent = "This is new content"
//        val result = editor.writeResource(testFilePath, newContent)
//
//        assert(result)
//        assert(editor.readResource(testFilePath) == newContent)
//    }
//
//    @Test
//    fun `read non-existent resource should throw exception`() = runTest {
//        val nonExistentPath = tempDir.resolve("nonexistent.txt").toString()
//
//        var exceptionThrown = false
//        try {
//            runBlocking { editor.readResource(nonExistentPath) }
//        } catch (e: IllegalArgumentException) {
//            exceptionThrown = true
//        }
//        assert(exceptionThrown)
//    }
//
//    // ===== REPLACEMENT TESTS =====
//
//    @Test
//    fun `replace text should replace first occurrence when replaceAll is false`() = runTest {
//        val operationId = editor.replaceText(testFilePath, "Line 3:", "Modified Line 3:", false)
//
//        val content = editor.readResource(testFilePath)
//        assert(content.contains("Modified Line 3:"))
//        assert(!content.contains("Line 3:"))
//
//        // Verify operation ID is non-empty
//        assert(operationId != null)
//        assert(operationId.isNotEmpty())
//    }
//
//    @Test
//    fun `replace text should replace all occurrences when replaceAll is true`() = runTest {
//        val operationId = editor.replaceText(testFilePath, "Line", "REPLACED", true)
//
//        val content = editor.readResource(testFilePath)
//        assert(!content.contains("Line"))
//        assert(content.split("REPLACED").size - 1 == 5)
//    }
//
//    @Test
//    fun `replace text should not modify content when text not found`() = runTest {
//        val originalContent = editor.readResource(testFilePath)
//        val operationId = editor.replaceText(testFilePath, "This text does not exist", "Replacement", false)
//
//        // Content should remain unchanged
//        val newContent = editor.readResource(testFilePath)
//        assert(originalContent == newContent)
//    }
//
//    // ===== INSERTION TESTS =====
//
//    @Test
//    fun `insert at pattern should add text before pattern when insertBefore is true`() = runTest {
//        val operationId = editor.insertAtPattern(testFilePath, "Line 3:", "INSERTED BEFORE ", true)
//
//        val content = editor.readResource(testFilePath)
//        assert(content.contains("INSERTED BEFORE Line 3:"))
//    }
//
//    @Test
//    fun `insert at pattern should add text after pattern when insertBefore is false`() = runTest {
//        val operationId = editor.insertAtPattern(testFilePath, "Line 3:", " INSERTED AFTER", false)
//
//        val content = editor.readResource(testFilePath)
//        assert(content.contains("Line 3: INSERTED AFTER"))
//    }
//
//    @Test
//    fun `insert at pattern should not modify content when pattern not found`() = runTest {
//        val originalContent = editor.readResource(testFilePath)
//        val operationId = editor.insertAtPattern(testFilePath, "This pattern does not exist", "Text", false)
//
//        // Content should remain unchanged
//        val newContent = editor.readResource(testFilePath)
//        assert(originalContent == newContent)
//    }
//
//    @Test
//    fun `insert at line should insert text at specified line position`() = runTest {
//        val operationId = editor.insertAtLine(testFilePath, 3, "This is a new line inserted at position 3")
//
//        val content = editor.readResource(testFilePath)
//        val lines = content.lines()
//        assert(lines[2] == "This is a new line inserted at position 3")
//        assert(lines[3] == "Line 3: For testing the editor")
//    }
//
//    @Test
//    fun `insert at line should add text at beginning when line is 1`() = runTest {
//        val operationId = editor.insertAtLine(testFilePath, 1, "This is a new first line")
//
//        val content = editor.readResource(testFilePath)
//        val lines = content.lines()
//        assert(lines[0] == "This is a new first line")
//        assert(lines[1] == "Line 1: This is a test file")
//    }
//
//    @Test
//    fun `insert at line should add text at end when line is after last line`() = runTest {
//        val lineCount = testFileContent.lines().size
//        val operationId = editor.insertAtLine(testFilePath, lineCount + 1, "This is a new last line")
//
//        val content = editor.readResource(testFilePath)
//        val lines = content.lines()
//        assert(lines.last() == "This is a new last line")
//    }
//
//    @Test
//    fun `insert at line should not modify content when line number is invalid`() = runTest {
//        val originalContent = editor.readResource(testFilePath)
//
//        // Test with line number 0
//        editor.insertAtLine(testFilePath, 0, "Invalid line")
//        assert(editor.readResource(testFilePath) == originalContent)
//
//        // Test with line number way beyond file size
//        editor.insertAtLine(testFilePath, 1000, "Invalid line")
//        assert(editor.readResource(testFilePath) == originalContent)
//    }
//
//    // ===== REMOVAL TESTS =====
//
//    @Test
//    fun `remove text should remove first occurrence when removeAll is false`() = runTest {
//        val operationId = editor.removeText(testFilePath, "Line 3: For testing the editor\n", false)
//
//        val content = editor.readResource(testFilePath)
//        assert(!content.contains("Line 3: For testing the editor"))
//        assert(content.contains("Line 2: It contains multiple lines"))
//        assert(content.contains("Line 4: With some content to modify"))
//    }
//
//    @Test
//    fun `remove text should remove all occurrences when removeAll is true`() = runTest {
//        val operationId = editor.removeText(testFilePath, "Line ", true)
//
//        val content = editor.readResource(testFilePath)
//        assert(!content.contains("Line "))
//        assert(content.contains("1: This is a test file"))
//    }
//
//    // ===== MODIFYBETWEEN TESTS =====
//
//    @Test
//    fun `modify between should transform text between start and end markers`() = runTest {
//        val operationId = editor.modifyBetween(
//            testFilePath,
//            "Line 2:",
//            "Line 4:",
//            { text -> text.uppercase() }
//        )
//
//        val content = editor.readResource(testFilePath)
//        assert(content.contains("Line 2: IT CONTAINS MULTIPLE LINES"))
//        assert(content.contains("LINE 3: FOR TESTING THE EDITOR"))
//    }
//
//    @Test
//    fun `modify between should not change content when start marker not found`() = runTest {
//        val originalContent = editor.readResource(testFilePath)
//
//        val operationId = editor.modifyBetween(
//            testFilePath,
//            "NonExistentStartMarker",
//            "Line 4:",
//            { text -> text.uppercase() }
//        )
//
//        // Content should remain unchanged
//        assert(editor.readResource(testFilePath) == originalContent)
//    }
//
//    @Test
//    fun `modify between should not change content when end marker not found`() = runTest {
//        val originalContent = editor.readResource(testFilePath)
//
//        val operationId = editor.modifyBetween(
//            testFilePath,
//            "Line 2:",
//            "NonExistentEndMarker",
//            { text -> text.uppercase() }
//        )
//
//        // Content should remain unchanged
//        assert(editor.readResource(testFilePath) == originalContent)
//    }
//
//    // ===== WRAPSELECTION TESTS =====
//
//    @Test
//    fun `wrap selection should wrap specified text with markers`() = runTest {
//        val operationId = editor.wrapSelection(
//            testFilePath,
//            "Line 3: For testing the editor",
//            "*** ",
//            " ***"
//        )
//
//        val content = editor.readResource(testFilePath)
//        assert(content.contains("*** Line 3: For testing the editor ***"))
//    }
//
//    // ===== APPLYTOPATTERN TESTS =====
//
//    @Test
//    fun `apply to pattern should transform all regex matches`() = runTest {
//        val operationId = editor.applyToPattern(
//            testFilePath,
//            Regex("Line \\d+:"),
//            { match -> "HEADER${match.value.substringAfter("Line ")} >" }
//        )
//
//        val content = editor.readResource(testFilePath)
//        assert(content.contains("HEADER1: >"))
//        assert(content.contains("HEADER2: >"))
//        assert(!content.contains("Line 1:"))
//    }
//
//    // ===== UTILITY METHOD TESTS =====
//
//    @Test
//    fun `find line number should return correct line for existing pattern`() = runTest {
//        val lineNumber = editor.findLineNumber(testFilePath, "For testing")
//        assert(lineNumber == 3)
//    }
//
//    @Test
//    fun `find line number should return -1 when pattern not found`() = runTest {
//        val lineNumber = editor.findLineNumber(testFilePath, "This text doesn't exist")
//        assert(lineNumber == -1)
//    }
//
//    @Test
//    fun `get resource info should return correct metadata for existing file`() = runTest {
//        val info = editor.getResourceInfo(testFilePath)
//
//        assert(info.exists)
//        assert(info.size > 0)
//        assert(!info.isReadOnly)
//        assert(info.extension == "txt")
//        assert(info.mimeType != null)
//    }
//
//    @Test
//    fun `get resource info should indicate non-existence for missing files`() = runTest {
//        val nonExistentPath = tempDir.resolve("nonexistent.txt").toString()
//        val info = editor.getResourceInfo(nonExistentPath)
//
//        assert(!info.exists)
//    }
//
//    // ===== UNDO TESTS =====
//
//    @Test
//    fun `undo should revert changes made by the specified operation`() = runTest {
//        val originalContent = editor.readResource(testFilePath)
//
//        // Perform an operation
//        val operationId = editor.replaceText(testFilePath, "Line 3:", "MODIFIED:", false)
//
//        // Verify the operation worked
//        val modifiedContent = editor.readResource(testFilePath)
//        assert(modifiedContent.contains("MODIFIED:"))
//
//        // Undo the operation
//        val undoResult = editor.undo(operationId)
//
//        // Verify undo worked
//        assert(undoResult)
//        val restoredContent = editor.readResource(testFilePath)
//        assert(restoredContent == originalContent)
//    }
//
//    @Test
//    fun `undo should selectively revert operations when multiple changes exist`() = runTest {
//        val originalContent = editor.readResource(testFilePath)
//
//        // Perform multiple operations
//        val op1 = editor.replaceText(testFilePath, "Line 1:", "FIRST:", false)
//        val op2 = editor.replaceText(testFilePath, "Line 3:", "THIRD:", false)
//        val op3 = editor.insertAtLine(testFilePath, 5, "NEW LINE")
//
//        // Verify the operations worked
//        val modifiedContent = editor.readResource(testFilePath)
//        assert(modifiedContent.contains("FIRST:"))
//        assert(modifiedContent.contains("THIRD:"))
//        assert(modifiedContent.contains("NEW LINE"))
//
//        // Undo operations in any order
//        val undoResult2 = editor.undo(op2)
//        assert(undoResult2)
//
//        // Verify selective undo worked
//        val afterUndo2 = editor.readResource(testFilePath)
//        assert(afterUndo2.contains("FIRST:"))
//        assert(!afterUndo2.contains("THIRD:"))
//        assert(afterUndo2.contains("Line 3:"))
//        assert(afterUndo2.contains("NEW LINE"))
//
//        // Undo another operation
//        val undoResult1 = editor.undo(op1)
//        assert(undoResult1)
//
//        // Verify another selective undo worked
//        val afterUndo1 = editor.readResource(testFilePath)
//        assert(!afterUndo1.contains("FIRST:"))
//        assert(afterUndo1.contains("Line 1:"))
//        assert(afterUndo1.contains("NEW LINE"))
//
//        // Try to undo an already undone operation (should fail)
//        val undoAgain1 = editor.undo(op1)
//        assert(!undoAgain1)
//    }
//
//    @Test
//    fun `undo should return false for invalid operation ids`() = runTest {
//        val undoResult = editor.undo("invalid-operation-id")
//        assert(!undoResult)
//    }
//
//    // ===== EDGE CASE TESTS =====
//
//    @Test
//    fun `operations should work correctly on empty files`() = runTest {
//        // Create an empty file
//        val emptyFile = tempDir.resolve("empty.txt")
//        emptyFile.writeText("")
//        val emptyFilePath = emptyFile.toAbsolutePath().toString()
//
//        // Test operations on empty file
//        val content = editor.readResource(emptyFilePath)
//        assert(content == "")
//
//        val op1 = editor.insertAtLine(emptyFilePath, 1, "New content")
//        assert(editor.readResource(emptyFilePath) == "New content")
//
//        editor.undo(op1)
//        assert(editor.readResource(emptyFilePath) == "")
//    }
//
//    @Test
//    fun `operations should handle large files efficiently`() = runTest {
//        // Create a large file
//        val largeFile = tempDir.resolve("large.txt")
//        val largeContent = StringBuilder()
//        repeat(10000) { i ->
//            largeContent.append("This is line $i of a very large file\n")
//        }
//
//        largeFile.writeText(largeContent.toString())
//        val largeFilePath = largeFile.toAbsolutePath().toString()
//
//        // Test operations on large file
//        val startTime = System.currentTimeMillis()
//        val operationId = editor.replaceText(largeFilePath, "line 5000", "MODIFIED LINE", false)
//        val endTime = System.currentTimeMillis()
//
//        val content = editor.readResource(largeFilePath)
//        assert(content.contains("This is MODIFIED LINE of a very large file"))
//
//        // Performance check - should be reasonably fast
//        assert(endTime - startTime < 5000) { "Operation took too long: ${endTime - startTime}ms" }
//    }
//
//    @Test
//    fun `operations should correctly handle special characters and unicode`() = runTest {
//        val specialCharsFile = tempDir.resolve("special.txt")
//        val specialContent = """
//            Line with Unicode: 你好, こんにちは, Привет
//            Line with symbols: !@#$%^&*()_+{}|:"<>?~`-=[]\\;',./
//            Line with emojis: 😀🤔🚀🌍🔥💯
//        """.trimIndent()
//
//        specialCharsFile.writeText(specialContent)
//        val specialFilePath = specialCharsFile.toAbsolutePath().toString()
//
//        // Test operations with special characters
//        val content = editor.readResource(specialFilePath)
//        assert(content == specialContent)
//
//        val operationId = editor.replaceText(specialFilePath, "你好", "问候", false)
//        val newContent = editor.readResource(specialFilePath)
//        assert(newContent.contains("问候"))
//        assert(!newContent.contains("你好"))
//    }
//
//    // Additional multiplatform-friendly tests can be added here
//}