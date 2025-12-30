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

package com.xemantic.ai.golem.core.script.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File

//class StatefulShell : Shell {
//
//    private var process: Process? = null
//    private var inputStream: BufferedWriter? = null
//    private var outputStream: BufferedReader? = null
//    private var errorStream: BufferedReader? = null
//
//    override suspend fun execute(
//        command: String,
//        workingDir: String,
//        timeout: Int
//    ): String = withContext(Dispatchers.IO) {
//
//        if (process == null) {
//            initializeShell(workingDir)
//        }
//
//        return@withContext try {
//            // Send the command to the shell
//            inputStream?.write("$command\n")
//            inputStream?.write("echo '===COMMAND_COMPLETED==='\n")
//            inputStream?.flush()
//
//            // Read the output until our marker
//            val output = StringBuilder()
//            var line: String?
//            while (outputStream?.readLine().also { line = it } != null) {
//                if (line == "===COMMAND_COMPLETED===") break
//                output.append(line).append("\n")
//            }
//
//            // Read any errors
//            val error = StringBuilder()
//            while (errorStream?.ready() == true) {
//                error.append(errorStream?.readLine()).append("\n")
//            }
//
//            if (error.isNotEmpty()) {
//                output.append("Error: ").append(error)
//            }
//
//            output.toString().trim()
//        } catch (e: Exception) {
//            closeShell()
//            "Error executing command: ${e.message}"
//        }
//    }
//
//    private fun initializeShell(workingDir: String) {
//        val processBuilder = ProcessBuilder("/bin/bash")
//        processBuilder.directory(File(workingDir))
//        processBuilder.redirectErrorStream(false)
//
//        process = processBuilder.start()
//        inputStream = process?.outputWriter()?.buffered()
//        outputStream = process?.inputReader()?.buffered()
//        errorStream = process?.errorReader()?.buffered()
//
//        // Change to the working directory
//        if (workingDir.isNotEmpty()) {
//            inputStream?.write("cd $workingDir\n")
//            inputStream?.flush()
//        }
//    }
//
//    fun closeShell() {
//        try {
//            inputStream?.write("exit\n")
//            inputStream?.flush()
//        } catch (e: Exception) {
//            // Ignore
//        }
//
//        inputStream?.close()
//        outputStream?.close()
//        errorStream?.close()
//        process?.destroy()
//
//        inputStream = null
//        outputStream = null
//        errorStream = null
//        process = null
//    }
//}