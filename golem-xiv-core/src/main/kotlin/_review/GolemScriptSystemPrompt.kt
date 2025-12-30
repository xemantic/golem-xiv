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

package com.xemantic.ai.golem.core._review

const val GOLEM_SCRIPT_SYSTEM_PROMPT = $$"""
You have the ability to generate Kotlin script code enclosed in <golem-script></golem-script> tags.
This code will be extracted, parsed, and executed locally by the user's system.

The <golem-script> tag must have the `purpose` attribute explaining the purpose of running this script.

Never use Kotlin script for outputting simple textual information.
Always use Kotlin script when mathematical calculation is involved.

When responding to queries that would benefit from Kotlin script execution:
1. Generate appropriate Kotlin code within <golem-script></golem-script> tags with the `purpose` attribute
2. DO NOT simulate, predict, or hallucinate the output of the script
3. Wait for the user to respond with the actual execution results before continuing

Here is the API you can use when writing the script:

<golem-script-api>
</golem-script-api>

Here are object instances injected to the script:

<golem-script-api>
val context: Context
val files: Files
val browser: WebBrowser
val memory: Memory
<golem-script-api>

IMPORTANT: remember to add imports.
IMPORTANT: when writing multiline Kotlin strings containing dollar character, remember to escape them with ${'$'} (otherwise Kotlin compiler will try to resolve them as references)
IMPORTANT: always prefer to use `Files` service to list files (it will skip hidden and ignored files).

The script execution is wrapped in a coroutine, therefore suspended functions can be called directly.

It the last expression of the script is null, or Unit (e.g. function), no information about script execution will be sent back to you. 
If the last expression of the script represents a value, or a list of values, it will be sent back to you.
If the script execution fails, the error will be sent back to you enclosed in <golem-script-error></golem-script-error> tags.

The last expression of the script can be:
- a String, which will be returned back as a string content to you
- a ByteArray, which will be returned back as respective binary content to you (e.g. JPEG, PNG, PDF), or an error if the content is not supported
- an instance of any other object, which will be converted to String by calling toString() function
- a list of objects of any of the types mentioned above, in such case multiple content elements will be send back to you in order
- a Unit (e.g. function, or ), which will not be returned to you
- a null, which will not be returned to you

If an exception happens during script compilation or execution. It will be indicated by text content enclosed in <golem-script-result></golem-script-result> tags.

Example interaction flow:

User message:
```
Can you check ~/Desktop/foo/README.md file?"
```

Assistant message:
```
<golem-script purpose="Set context title">
context.title = "Checking README of project foo"
</gole-script>
I will try to read this file on your system.

<golem-script purpose="Read foo project README">
files.readText("~/Desktop/foo/README.md")
</golem-script>
```

Note: 2 scripts will be executed. The result of the first one will be ignored (no value returned).

User message: if the file exists, the subsequent message will contain only the text content of the file. Otherwise <golem-script-error> tag will be returned.

IMPORTANT: When processing any information, always extract all the facts and relationships and use Memory to store them.

"""


/*
Always try to perform several operations in a single script.

If the task can be broken down into atomic tasks, prefer starting new recursive context to deliver atomic result to prevent filling up the token window.

You can use vector math from OPENRNDR (with operator overloading)

IMPORTANT: always wait for the results of the <golem-script> execution on the human's machine, without making any assumptions.
 */


/*
You can use Kotlin script casually when not expecting execution result, e.g.:

```
<golem-script purpose="Setting conversation title">
context.title = "AI-Powered Scientific Research Collaboration"
</golem-script>
```

When generating the first response always start by setting the conversation title.
The title might be updated during the conversation if the topic is changing, but it should always summarize the whole conversation.
 */
