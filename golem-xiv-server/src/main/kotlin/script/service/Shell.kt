/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.script.service

import com.xemantic.ai.golem.server.script.candidate.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File

class StatefulShell : Shell {

    private var process: Process? = null
    private var inputStream: BufferedWriter? = null
    private var outputStream: BufferedReader? = null
    private var errorStream: BufferedReader? = null

    override suspend fun execute(
        command: String,
        workingDir: String,
        timeout: Int
    ): String = withContext(Dispatchers.IO) {

        if (process == null) {
            initializeShell(workingDir)
        }

        return@withContext try {
            // Send the command to the shell
            inputStream?.write("$command\n")
            inputStream?.write("echo '===COMMAND_COMPLETED==='\n")
            inputStream?.flush()

            // Read the output until our marker
            val output = StringBuilder()
            var line: String?
            while (outputStream?.readLine().also { line = it } != null) {
                if (line == "===COMMAND_COMPLETED===") break
                output.append(line).append("\n")
            }

            // Read any errors
            val error = StringBuilder()
            while (errorStream?.ready() == true) {
                error.append(errorStream?.readLine()).append("\n")
            }

            if (error.isNotEmpty()) {
                output.append("Error: ").append(error)
            }

            output.toString().trim()
        } catch (e: Exception) {
            closeShell()
            "Error executing command: ${e.message}"
        }
    }

    private fun initializeShell(workingDir: String) {
        val processBuilder = ProcessBuilder("/bin/bash")
        processBuilder.directory(File(workingDir))
        processBuilder.redirectErrorStream(false)

        process = processBuilder.start()
        inputStream = process?.outputWriter()?.buffered()
        outputStream = process?.inputReader()?.buffered()
        errorStream = process?.errorReader()?.buffered()

        // Change to the working directory
        if (workingDir.isNotEmpty()) {
            inputStream?.write("cd $workingDir\n")
            inputStream?.flush()
        }
    }

    fun closeShell() {
        try {
            inputStream?.write("exit\n")
            inputStream?.flush()
        } catch (e: Exception) {
            // Ignore
        }

        inputStream?.close()
        outputStream?.close()
        errorStream?.close()
        process?.destroy()

        inputStream = null
        outputStream = null
        errorStream = null
        process = null
    }
}