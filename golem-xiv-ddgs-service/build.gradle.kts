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

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

val pythonDir = file("src/main/python")
val venvDir = file("src/main/python/venv")
val venvPython = if (System.getProperty("os.name").lowercase().contains("windows")) {
    file("src/main/python/venv/Scripts/python.exe")
} else {
    file("src/main/python/venv/bin/python")
}

tasks.register<Exec>("createVenv") {
    description = "Create Python virtual environment"
    group = "golem"

    workingDir = pythonDir
    commandLine("python3", "-m", "venv", "venv")

    onlyIf { !venvDir.exists() }

    doFirst {
        println("Creating Python virtual environment...")
    }
}

tasks.register<Exec>("installDdgsDeps") {
    description = "Install Python dependencies for DDGS service"
    group = "golem"

    dependsOn("createVenv")

    workingDir = pythonDir
    commandLine(venvPython.absolutePath, "-m", "pip", "install", "-r", "requirements.txt")

    standardOutput = System.out
    errorOutput = System.err

    doFirst {
        println("Installing DDGS service dependencies in virtual environment...")
    }
}

tasks.register<Exec>("runDdgsSearch") {
    description = "Start DDGS search HTTP service"
    group = "golem"

    dependsOn("installDdgsDeps")

    workingDir = pythonDir
    commandLine(venvPython.absolutePath, "ddgs_service.py")

    standardInput = System.`in`
    standardOutput = System.out
    errorOutput = System.err

    // Make task interruptible
    doFirst {
        println("Starting DDGS Search Service on http://localhost:8001")
        println("API Documentation: http://localhost:8001/docs")
        println("Press Ctrl+C to stop")
    }
}
