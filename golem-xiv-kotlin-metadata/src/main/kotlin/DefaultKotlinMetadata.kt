package com.xemantic.ai.golem.kotlin.metadata

import com.xemantic.ai.golem.api.backend.script.KotlinMetadata
import com.xemantic.kotlin.core.text.buildText
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.metadata.*
import kotlin.time.measureTime
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses
import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType

/**
 * Represents an indexed extension function with its source location.
 */
data class IndexedExtension(
    val function: KmFunction,
    val sourceClass: String  // fully qualified class name where it's defined
)

/**
 * Represents an indexed extension property with its source location.
 */
data class IndexedExtensionProperty(
    val property: KmProperty,
    val sourceClass: String  // fully qualified class name where it's defined
)

class DefaultKotlinMetadata(
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
    scanEntireClasspath: Boolean = true
) : KotlinMetadata {

    private val logger = KotlinLogging.logger {}

    // Index of extension functions: receiver type name -> list of extensions
    private val extensionIndex: Map<String, List<IndexedExtension>>

    // Index of extension properties: receiver type name -> list of extension properties
    private val extensionPropertyIndex: Map<String, List<IndexedExtensionProperty>>

    // Index of all Kotlin classes: simple name (lowercase) -> list of fully qualified names
    private val classIndex: Map<String, List<String>>

    // Mapping of Kotlin built-in type names to their KClass
    // These types map to JVM types and don't have Kotlin metadata
    private val builtinTypes: Map<String, KClass<*>> = buildBuiltinTypes()

    companion object {
        /**
         * Builds a map of Kotlin built-in type names to their KClass.
         * Uses JavaToKotlinClassMap as the authoritative source for Java → Kotlin type mappings.
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        private fun buildBuiltinTypes(): Map<String, KClass<*>> = buildMap {
            // 1. Add all collection types from JavaToKotlinClassMap.mutabilityMappings
            for (mapping in JavaToKotlinClassMap.mutabilityMappings) {
                // Read-only variant (e.g., kotlin.collections.List)
                val readOnlyFqn = mapping.kotlinReadOnly.asSingleFqName().asString()
                // Convert ClassId to JVM class name (nested classes use $ not .)
                val javaClassId = mapping.javaClass
                val javaClassName = javaClassId.packageFqName.asString() + "." +
                    javaClassId.relativeClassName.asString().replace('.', '$')
                val javaClass = Class.forName(javaClassName)
                put(readOnlyFqn, javaClass.kotlin)

                // Mutable variant (e.g., kotlin.collections.MutableList)
                val mutableFqn = mapping.kotlinMutable.asSingleFqName().asString()
                put(mutableFqn, javaClass.kotlin)
            }

            // 2. Add primitive types from JvmPrimitiveType
            for (primitiveType in JvmPrimitiveType.entries) {
                val javaWrapperClass = Class.forName(primitiveType.wrapperFqName.asString())
                val kotlinClassId = JavaToKotlinClassMap.mapJavaToKotlin(primitiveType.wrapperFqName)
                if (kotlinClassId != null) {
                    put(kotlinClassId.asSingleFqName().asString(), javaWrapperClass.kotlin)
                }
            }

            // 3. Add other core types that have Java equivalents
            val coreJavaTypes = listOf(
                Any::class.java,
                String::class.java,
                CharSequence::class.java,
                Throwable::class.java,
                Cloneable::class.java,
                Number::class.java,
                Comparable::class.java,
                Enum::class.java,
                Annotation::class.java,
            )
            for (javaClass in coreJavaTypes) {
                val kotlinClassId = JavaToKotlinClassMap.mapJavaToKotlin(FqName(javaClass.name))
                if (kotlinClassId != null) {
                    put(kotlinClassId.asSingleFqName().asString(), javaClass.kotlin)
                }
            }

            // 4. Special cases not in JavaToKotlinClassMap or requiring special handling
            put("kotlin.Nothing", Nothing::class)
            put("kotlin.Unit", Unit::class)
            put("kotlin.reflect.KClass", KClass::class)

            // 5. Array types (Kotlin-specific, not mapped from Java)
            put("kotlin.Array", Array::class)
            put("kotlin.IntArray", IntArray::class)
            put("kotlin.LongArray", LongArray::class)
            put("kotlin.ShortArray", ShortArray::class)
            put("kotlin.ByteArray", ByteArray::class)
            put("kotlin.FloatArray", FloatArray::class)
            put("kotlin.DoubleArray", DoubleArray::class)
            put("kotlin.BooleanArray", BooleanArray::class)
            put("kotlin.CharArray", CharArray::class)

            // 6. Unsigned types (Kotlin-specific)
            put("kotlin.UByte", UByte::class)
            put("kotlin.UShort", UShort::class)
            put("kotlin.UInt", UInt::class)
            put("kotlin.ULong", ULong::class)
            put("kotlin.UByteArray", UByteArray::class)
            put("kotlin.UShortArray", UShortArray::class)
            put("kotlin.UIntArray", UIntArray::class)
            put("kotlin.ULongArray", ULongArray::class)
        }
    }

    init {
        val scanTime = measureTime {
            if (scanEntireClasspath) {
                val (funcIndex, propIndex, clsIndex) = buildFullClasspathIndex()
                extensionIndex = funcIndex
                extensionPropertyIndex = propIndex
                classIndex = clsIndex
            } else {
                extensionIndex = emptyMap()
                extensionPropertyIndex = emptyMap()
                classIndex = emptyMap()
            }
        }
        val extensionsFound = extensionIndex.values.sumOf { it.size } +
            extensionPropertyIndex.values.sumOf { it.size }
        logger.info { "Classpath scan completed in $scanTime, found $extensionsFound extensions" }
    }

    private fun resolve(name: String): String? {
        // Check if it's a built-in type first
        builtinTypes[name]?.let { kClass ->
            return formatBuiltinClass(kClass, name)
        }

        val parts = name.split(".")

        // Try to find where the class name ends and member name begins
        // by progressively trying to load the class
        for (i in parts.size downTo 1) {
            val className = parts.take(i).joinToString(".")
            val memberName = if (i < parts.size) parts.drop(i).joinToString(".") else null

            // Check if this className is a built-in type
            builtinTypes[className]?.let { kClass ->
                if (memberName != null) {
                    return resolveBuiltinMember(kClass, className, memberName)
                } else {
                    return formatBuiltinClass(kClass, className)
                }
            }

            // First try direct class loading
            val clazz = try {
                classLoader.loadClass(className)
            } catch (_: ClassNotFoundException) {
                null
            }

            if (clazz == null) continue

            // If we have a memberName, first try it as a nested class (using $ separator)
            if (memberName != null) {
                val nestedClassName = "$className\$${memberName.replace('.', '$')}"
                val nestedClazz = try {
                    classLoader.loadClass(nestedClassName)
                } catch (_: ClassNotFoundException) {
                    null
                }
                // If we found a nested class, format it and return
                if (nestedClazz != null) {
                    val metadataAnnotation = nestedClazz.getAnnotation(Metadata::class.java)
                        ?: return null
                    return when (
                        val metadata = KotlinClassMetadata.readLenient(
                            annotationData = metadataAnnotation
                        )
                    ) {
                        is KotlinClassMetadata.Class -> {
                            formatClass(metadata.kmClass, name, nestedClazz)
                        }
                        else -> null
                    }
                }
            }

            val metadataAnnotation = clazz.getAnnotation(Metadata::class.java)
                ?: return null

            return when (
                val metadata = KotlinClassMetadata.readLenient(
                    annotationData = metadataAnnotation
                )
            ) {

                is KotlinClassMetadata.Class -> {
                    val kmClass = metadata.kmClass
                    if (memberName != null) {
                        resolveMember(kmClass, className, memberName, clazz)
                    } else {
                        formatClass(kmClass, className, clazz)
                    }
                }

                is KotlinClassMetadata.FileFacade -> {
                    val kmPackage = metadata.kmPackage
                    if (memberName != null) {
                        resolvePackageMember(kmPackage, memberName)
                    } else {
                        null
                    }
                }

                else -> null
            }
        }

        return null
    }

    private fun resolveMember(kmClass: KmClass, className: String, memberName: String, clazz: Class<*>): String? {
        val isInterface = kmClass.kind == ClassKind.INTERFACE

        // Check functions
        kmClass.functions.find { it.name == memberName }?.let { function ->
            return formatFunction(function, inInterface = isInterface, clazz = clazz)
        }

        // Check properties
        kmClass.properties.find { it.name == memberName }?.let { property ->
            return formatProperty(property, inInterface = isInterface, clazz = clazz)
        }

        // Check extension functions
        getExtensionsFor(className).find { it.function.name == memberName }?.let { ext ->
            return formatFunction(ext.function)
        }

        return null
    }

    private fun resolvePackageMember(kmPackage: KmPackage, memberName: String): String? {
        kmPackage.functions.find {
            it.name == memberName
        }?.let { function ->
            return formatFunction(function)
        }

        kmPackage.properties.find {
            it.name == memberName
        }?.let { property ->
            return formatProperty(property)
        }

        return null
    }

    /**
     * Format a Kotlin built-in type using kotlin-reflect.
     */
    private fun formatBuiltinClass(
        kClass: KClass<*>,
        kotlinName: String
    ): String {
        val simpleName = kotlinName.substringAfterLast(".")
        val isInterface = kClass.java.isInterface

        // Supertypes (excluding Any and Java-specific interfaces)
        val javaOnlyInterfaces = setOf(
            java.io.Serializable::class,
            java.lang.Cloneable::class,
        )
        val supertypes = kClass.superclasses
            .filter { it != Any::class }
            .filter { it !in javaOnlyInterfaces }
            .map { formatKTypeWithTypeArg(it, simpleName) }

        // Collect all members to sort them together alphabetically
        data class Member(val name: String, val formatted: String)
        val allMembers = mutableListOf<Member>()

        // Properties (only public, non-extension)
        kClass.memberProperties
            .filter { it.visibility == KVisibility.PUBLIC }
            .forEach { prop ->
                allMembers.add(Member(prop.name, formatKProperty(prop)))
            }

        // Functions (only public, non-extension, excluding synthetic, internal, and Java-only methods)
        kClass.memberFunctions
            .asSequence()
            .filter { it.visibility == KVisibility.PUBLIC }
            .filter { !it.name.startsWith("component") }  // Exclude componentN
            .filter { it.name != "copy" }  // Exclude copy for data classes
            .filter { !it.name.contains("$") }  // Exclude synthetic
            .filter { !it.toString().contains("internal ") }  // Exclude internal
            // Filter out Java-only methods (those with argN parameter names)
            .filter { func ->
                val params = func.parameters.drop(1)  // Skip 'this'
                params.isEmpty() || params.all { it.name != null && !it.name!!.startsWith("arg") }
            }
            .toList()
            .forEach { func ->
                allMembers.add(Member(func.name, formatKFunction(func)))
            }

        // Extension functions on this exact type (shown inside class body)
        // Only include public extensions from kotlin.* packages for built-in types
        getExtensionsFor(kotlinName)
            .filter { it.sourceClass.startsWith("kotlin.") }
            .filter { it.function.visibility == Visibility.PUBLIC }
            .forEach { ext ->
                allMembers.add(Member(ext.function.name, formatFunctionWithoutReceiver(ext.function)))
            }

        // Extension properties on this exact type (shown inside class body)
        getExtensionPropertiesFor(kotlinName)
            .filter { it.sourceClass.startsWith("kotlin.") }
            .filter { it.property.visibility == Visibility.PUBLIC }
            .forEach { extProp ->
                allMembers.add(Member(extProp.property.name, formatExtensionPropertyWithoutReceiver(extProp.property)))
            }

        // Extension functions and properties on supertypes (shown outside class body)
        // Only include public extensions from kotlin.* packages for built-in types
        val supertypeExtensions = mutableListOf<Pair<String, IndexedExtension>>()
        val supertypeExtensionProperties = mutableListOf<Pair<String, IndexedExtensionProperty>>()
        for (supertype in kClass.superclasses) {
            if (supertype == Any::class) continue
            val supertypeName = "kotlin.${supertype.simpleName}"
            val extensions = getExtensionsFor(supertypeName)
                .filter { it.sourceClass.startsWith("kotlin.") }
                .filter { it.function.visibility == Visibility.PUBLIC }
            for (ext in extensions) {
                supertypeExtensions.add(supertypeName to ext)
            }
            val extensionProps = getExtensionPropertiesFor(supertypeName)
                .filter { it.sourceClass.startsWith("kotlin.") }
                .filter { it.property.visibility == Visibility.PUBLIC }
            for (extProp in extensionProps) {
                supertypeExtensionProperties.add(supertypeName to extProp)
            }
        }

        return buildText {
            // Class/interface declaration
            +(if (isInterface) "interface " else "class ")
            +simpleName

            if (supertypes.isNotEmpty()) {
                +" : "
                +supertypes.joinToString(", ")
            }

            +" {\n"

            // Sort all members alphabetically and output
            for (member in allMembers.sortedBy { it.name }) {
                +"    "
                +member.formatted
                +"\n"
            }

            +"}"

            if (supertypeExtensions.isNotEmpty() || supertypeExtensionProperties.isNotEmpty()) {
                // Group extension functions by source package
                val funcsBySource = supertypeExtensions.groupBy { it.second.sourceClass.substringBeforeLast('.') }
                // Group extension properties by source package
                val propsBySource = supertypeExtensionProperties.groupBy { it.second.sourceClass.substringBeforeLast('.') }

                // Get all unique source packages, sorted
                val allPackages = (funcsBySource.keys + propsBySource.keys).sorted()

                allPackages.forEachIndexed { packageIndex, sourcePackage ->
                    if (packageIndex == 0) {
                        +"\n// from $sourcePackage\n"
                    } else {
                        +"// from $sourcePackage\n"
                    }
                    // Extension properties first, sorted by name
                    for ((_, extProp) in propsBySource[sourcePackage]?.sortedBy { it.second.property.name } ?: emptyList()) {
                        +formatExtensionProperty(extProp.property)
                        +"\n"
                    }
                    // Then extension functions, sorted by name
                    for ((_, ext) in funcsBySource[sourcePackage]?.sortedBy { it.second.function.name } ?: emptyList()) {
                        +formatFunction(ext.function)
                        +"\n"
                    }
                }
            }

            trimLastNewLine()
        }
    }

    /**
     * Resolve a member of a built-in type.
     */
    private fun resolveBuiltinMember(
        kClass: KClass<*>,
        className: String,
        memberName: String
    ): String? {

        // Check functions
        kClass.memberFunctions.find {
            it.name == memberName && it.visibility == KVisibility.PUBLIC
        }?.let { return formatKFunction(it) }

        // Check properties
        kClass.memberProperties.find {
            it.name == memberName && it.visibility == KVisibility.PUBLIC
        }?.let { return formatKProperty(it) }

        // Check extension functions
        getExtensionsFor(className).find {
            it.function.name == memberName
        }?.let { ext ->
            return formatFunction(ext.function)
        }

        return null
    }

    /**
     * Format a KClass with type argument for supertypes.
     */
    private fun formatKTypeWithTypeArg(
        kClass: KClass<*>,
        typeArg: String
    ): String {
        val simpleName = kClass.simpleName ?: "Unknown"
        // Handle common generic supertypes
        return when (kClass) {
            Comparable::class -> "Comparable<$typeArg>"
            else -> simpleName
        }
    }

    /**
     * Format a KFunction using kotlin-reflect.
     */
    private fun formatKFunction(func: KFunction<*>): String = buildText {

        if (func.isOperator) +"operator "
        if (func.isInfix) +"infix "
        if (func.isSuspend) +"suspend "

        +"fun "
        +func.name
        +"("

        // Skip first parameter (receiver) for member functions
        val params = func.parameters.drop(1)  // Drop 'this'
        +(params.joinToString(", ") { param ->
            val paramName = param.name ?: "arg${param.index}"
            val typeName = formatKTypeProjection(param.type)
            "$paramName: $typeName"
        })

        +")"

        val returnType = formatKTypeProjection(func.returnType)
        if (returnType != "Unit") {
            +": "
            +returnType
        }
    }

    /**
     * Format a KProperty using kotlin-reflect.
     */
    private fun formatKProperty(prop: KProperty1<*, *>): String = buildText {
        +"val "
        +prop.name
        +": "
        +formatKTypeProjection(prop.returnType)
    }

    /**
     * Format a KType for display.
     * Uses simple names for kotlin.* types and fully qualified names for java.* types.
     */
    private fun formatKTypeProjection(type: kotlin.reflect.KType): String {
        val baseName = when (val classifier = type.classifier) {
            is KClass<*> -> {
                val qualifiedName = classifier.qualifiedName ?: classifier.simpleName ?: "Unknown"
                // Use simple name for kotlin.* types, fully qualified for others
                if (qualifiedName.startsWith("kotlin.")) {
                    classifier.simpleName ?: "Unknown"
                } else {
                    qualifiedName
                }
            }
            else -> type.toString()
        }

        val args = type.arguments.map { projection ->
            val projType = projection.type ?: return@map "*"
            when (projection.variance) {
                kotlin.reflect.KVariance.INVARIANT, null -> formatKTypeProjection(projType)
                kotlin.reflect.KVariance.IN -> "in ${formatKTypeProjection(projType)}"
                kotlin.reflect.KVariance.OUT -> "out ${formatKTypeProjection(projType)}"
            }
        }

        val result = if (args.isEmpty()) {
            baseName
        } else {
            "$baseName<${args.joinToString(", ")}>"
        }

        return if (type.isMarkedNullable) "$result?" else result
    }

    /**
     * Format an extension function without the receiver prefix (for showing inside class body).
     */
    private fun formatFunctionWithoutReceiver(function: KmFunction): String = buildText {
        +formatVisibility(function.visibility)
        if (function.isOperator) +"operator "
        if (function.isInfix) +"infix "
        if (function.isSuspend) +"suspend "

        +"fun "
        +function.name
        +"("

        +(function.valueParameters.joinToString(", ") { param ->
            "${param.name}: ${formatType(param.type)}"
        })

        +")"

        function.returnType.let { returnType ->
            val typeName = formatType(returnType)
            if (typeName != "Unit") {
                +": "
                +typeName
            }
        }
    }

    /**
     * Format an extension property without the receiver prefix (for showing inside class body).
     */
    private fun formatExtensionPropertyWithoutReceiver(property: KmProperty): String = buildText {
        +formatVisibility(property.visibility)
        +(if (property.isVar) "var " else "val ")
        +property.name
        +": "
        +formatType(property.returnType)
    }

    private fun formatClass(kmClass: KmClass, className: String, clazz: Class<*>): String {
        val isInterface = kmClass.kind == ClassKind.INTERFACE
        val isEnum = kmClass.kind == ClassKind.ENUM_CLASS
        val isAnnotation = kmClass.kind == ClassKind.ANNOTATION_CLASS
        val isSealed = kmClass.modality == Modality.SEALED

        // Extract the package name for context-aware type formatting
        // For nested classes, use the actual Java package, not the parent class
        val contextPackage = clazz.`package`?.name ?: className.substringBeforeLast(".", "")

        // Find primary constructor and its parameter names
        val primaryConstructor = kmClass.constructors.firstOrNull { !it.isSecondary }
        val constructorParamNames = primaryConstructor?.valueParameters?.map { it.name }?.toSet() ?: emptySet()

        // Constructor parameters - properties that match constructor parameter names
        val constructorProperties = kmClass.properties.filter { it.name in constructorParamNames }

        // Supertypes
        val supertypes = kmClass.supertypes
            .map { formatType(it, contextPackage) }
            .filter { it != "Any" }
            .filter { !(isEnum && it.startsWith("Enum<")) }  // Enum supertype is redundant for enum classes

        // Extensions from the index (shown after class body with source)
        val extensions = getExtensionsFor(className)
        val extensionProperties = getExtensionPropertiesFor(className)

        return buildText {
            // Visibility and modifiers
            +formatVisibility(kmClass.visibility)
            // Skip 'abstract' for interfaces - they're always abstract
            if (kmClass.modality == Modality.ABSTRACT && !isInterface) +"abstract "
            if (kmClass.modality == Modality.OPEN) +"open "
            if (kmClass.modality == Modality.SEALED) +"sealed "
            if (kmClass.isData) +"data "
            if (kmClass.isValue) +"value "

            // Class kind
            +when (kmClass.kind) {
                ClassKind.CLASS -> "class "
                ClassKind.INTERFACE -> "interface "
                ClassKind.ENUM_CLASS -> "enum class "
                ClassKind.ENUM_ENTRY -> "enum entry "
                ClassKind.OBJECT -> "object "
                ClassKind.COMPANION_OBJECT -> "companion object "
                ClassKind.ANNOTATION_CLASS -> "annotation class "
            }

            // Class name (simple name, not FQN)
            // For nested classes, the name might be "Parent.Nested" or "Parent$Nested"
            +kmClass.name.substringAfterLast("/").substringAfterLast(".").substringAfterLast("$")

            // For annotation classes, show parameters inline without body
            if (isAnnotation && primaryConstructor != null && primaryConstructor.valueParameters.isNotEmpty()) {
                +"("
                +(primaryConstructor.valueParameters.joinToString(", ") { param ->
                    val defaultValue = if (param.declaresDefaultValue) " = \"\"" else ""
                    "val ${param.name}: ${formatType(param.type, contextPackage)}$defaultValue"
                })
                +")"
                return@buildText
            }

            if (constructorProperties.isNotEmpty()) {
                +"("
                +(constructorProperties.joinToString(", ") { prop ->
                    val prefix = if (prop.isVar) "var " else "val "
                    "$prefix${prop.name}: ${formatType(prop.returnType, contextPackage)}"
                })
                +")"
            }

            if (supertypes.isNotEmpty()) {
                +" : "
                +supertypes.joinToString(", ")
            }

            // Check if there's any body content
            val hasEnumEntries = isEnum && kmClass.kmEnumEntries.isNotEmpty()
            val hasSealedSubclasses = isSealed && kmClass.sealedSubclasses.isNotEmpty()
            val hasBodyProperties = kmClass.properties.any { it.name !in constructorParamNames }
            val hasFunctions = kmClass.functions.isNotEmpty()
            val hasCompanion = kmClass.companionObject != null
            val hasBodyContent = hasEnumEntries || hasSealedSubclasses || hasBodyProperties || hasFunctions || hasCompanion

            // Only add braces if there's body content or extensions
            if (!hasBodyContent && extensions.isEmpty() && extensionProperties.isEmpty()) {
                return@buildText
            }

            +" {\n"

            // For enum classes, show enum entries
            if (hasEnumEntries) {
                // Check if there are properties or functions after the entries
                val hasMembers = hasBodyProperties || hasFunctions

                kmClass.kmEnumEntries.forEachIndexed { index, entry ->
                    +"    "
                    +entry.name
                    if (index < kmClass.kmEnumEntries.size - 1) {
                        +","
                    } else if (hasMembers) {
                        // Last entry needs semicolon if there are properties/functions after
                        +";"
                    }
                    +"\n"
                }
            }

            // For sealed classes, show sealed subclasses
            if (hasSealedSubclasses) {
                val parentSimpleName = kmClass.name.substringAfterLast("/")
                kmClass.sealedSubclasses.forEach { subclassName ->
                    // subclassName format: "com/example/Parent.SubClass" or "com/example/Parent$SubClass"
                    val simpleName = subclassName.substringAfterLast("/")
                        .substringAfterLast(".")  // Remove parent class prefix like "Parent."
                        .substringAfterLast("$")  // Remove parent class prefix like "Parent$"
                    +"    class $simpleName : $parentSimpleName\n"
                }
            }

            // Properties (excluding constructor properties)
            kmClass.properties
                .filter { it.name !in constructorParamNames }
                .forEach { property ->
                    +"    "
                    +formatProperty(property, inInterface = isInterface, clazz = clazz, contextPackage = contextPackage)
                    +"\n"
                }

            // Functions
            kmClass.functions.forEach { function ->
                +"    "
                +formatFunction(function, inInterface = isInterface, clazz = clazz, contextPackage = contextPackage)
                +"\n"
            }

            // Companion object members
            val companionObjectName = kmClass.companionObject
            if (companionObjectName != null) {
                val companionClassName = "${className}\$$companionObjectName"
                try {
                    val companionClazz = classLoader.loadClass(companionClassName)
                    val companionMetadata = companionClazz.getAnnotation(Metadata::class.java)
                    if (companionMetadata != null) {
                        val companionKmMetadata = KotlinClassMetadata.readLenient(companionMetadata)
                        if (companionKmMetadata is KotlinClassMetadata.Class) {
                            val companionKmClass = companionKmMetadata.kmClass
                            +"    companion object {\n"

                            // Companion properties
                            companionKmClass.properties.forEach { property ->
                                +"        "
                                if (property.isConst) +"const "
                                +(if (property.isVar) "var " else "val ")
                                +property.name
                                +": "
                                +formatType(property.returnType, contextPackage)
                                +"\n"
                            }

                            // Companion functions
                            companionKmClass.functions.forEach { function ->
                                +"        "
                                +formatFunction(function, inInterface = false, clazz = companionClazz, contextPackage = contextPackage)
                                +"\n"
                            }

                            +"    }\n"
                        }
                    }
                } catch (_: Exception) {
                    // Companion class not found, skip
                }
            }

            +"}"

            if (extensions.isNotEmpty() || extensionProperties.isNotEmpty()) {
                // Group extension functions by source package
                val funcsBySource = extensions.groupBy { it.sourceClass.substringBeforeLast('.') }
                // Group extension properties by source package
                val propsBySource = extensionProperties.groupBy { it.sourceClass.substringBeforeLast('.') }

                // Get all unique source packages, sorted
                val allPackages = (funcsBySource.keys + propsBySource.keys).sorted()

                allPackages.forEachIndexed { packageIndex, sourcePackage ->
                    // First package gets newline before comment, subsequent don't (avoid blank line)
                    if (packageIndex == 0) {
                        +"\n// from $sourcePackage\n"
                    } else {
                        +"// from $sourcePackage\n"
                    }

                    // Extension properties first
                    propsBySource[sourcePackage]?.forEach { extProp ->
                        +formatExtensionProperty(extProp.property, contextPackage)
                        +"\n"
                    }

                    // Then extension functions
                    funcsBySource[sourcePackage]?.forEach { ext ->
                        +formatFunction(ext.function, contextPackage = contextPackage)
                        +"\n"
                    }
                }
            }

            trimLastNewLine()
        }
    }

    private fun formatFunction(
        function: KmFunction,
        inInterface: Boolean = false,
        clazz: Class<*>? = null,
        contextPackage: String? = null
    ): String = buildText {
        +formatVisibility(function.visibility)

        // Check for override - either FAKE_OVERRIDE or explicit override detected via reflection
        val isOverride = function.kind == MemberKind.FAKE_OVERRIDE ||
            (clazz != null && isMethodOverride(clazz, function.name))
        if (isOverride) +"override "

        // Skip 'abstract' in interfaces - all interface members are implicitly abstract
        // Also skip if it's an override (can't be both override and abstract in output)
        if (function.modality == Modality.ABSTRACT && !inInterface && !isOverride) +"abstract "
        // Only show 'open' if not an override (override implies open-ness for further subclassing)
        if (function.modality == Modality.OPEN && !isOverride) +"open "

        // Function-specific modifiers
        if (function.isTailrec) +"tailrec "
        if (function.isSuspend) +"suspend "
        if (function.isInline) +"inline "
        if (function.isInfix) +"infix "
        if (function.isOperator) +"operator "

        +"fun "

        // Handle extension function receiver
        function.receiverParameterType?.let { receiverType ->
            +formatType(receiverType, contextPackage)
            +"."
        }

        +function.name
        +"("

        +(function.valueParameters.joinToString(", ") { param ->
            "${param.name}: ${formatType(param.type, contextPackage)}"
        })

        +")"

        function.returnType.let { returnType ->
            val typeName = formatType(returnType, contextPackage)
            if (typeName != "Unit") {
                +": "
                +typeName
            }
        }
    }

    private fun formatProperty(
        property: KmProperty,
        inInterface: Boolean = false,
        clazz: Class<*>? = null,
        contextPackage: String? = null
    ): String = buildText {
        +formatVisibility(property.visibility)

        // Check for override - either FAKE_OVERRIDE or explicit override detected via reflection
        val isOverride = property.kind == MemberKind.FAKE_OVERRIDE ||
            (clazz != null && isPropertyOverride(clazz, property.name))
        if (isOverride) +"override "

        // Skip 'abstract' in interfaces - all interface members are implicitly abstract
        if (property.modality == Modality.ABSTRACT && !inInterface && !isOverride) +"abstract "
        if (property.modality == Modality.OPEN && !isOverride) +"open "

        +(if (property.isVar) "var " else "val ")
        +property.name
        +": "
        +formatType(property.returnType, contextPackage)
    }

    private fun formatExtensionProperty(property: KmProperty, contextPackage: String? = null): String = buildText {
        +formatVisibility(property.visibility)
        +(if (property.isVar) "var " else "val ")

        // Extension property receiver
        property.receiverParameterType?.let { receiverType ->
            +formatType(receiverType, contextPackage)
            +"."
        }

        +property.name
        +": "
        +formatType(property.returnType, contextPackage)
    }

    private fun formatType(type: KmType, contextPackage: String? = null): String {
        val classifier = type.classifier
        val baseName = when (classifier) {
            is KmClassifier.Class -> {
                val fqn = classifier.name.replace("/", ".")
                val typePackage = fqn.substringBeforeLast(".", "")
                // Use simple name for:
                // 1. Well-known stdlib types (kotlin.*, java.*)
                // 2. Types from the same package as the context
                if (isWellKnownType(fqn) || typePackage == contextPackage) {
                    fqn.substringAfterLast(".")
                } else {
                    fqn
                }
            }
            is KmClassifier.TypeAlias -> classifier.name.substringAfterLast("/")
            is KmClassifier.TypeParameter -> "T" // Simplified
        }

        val typeArgs = type.arguments.map { projection ->
            val projType = projection.type ?: return@map "*"
            when (projection.variance) {
                KmVariance.INVARIANT, null -> formatType(projType, contextPackage)
                KmVariance.IN -> "in ${formatType(projType, contextPackage)}"
                KmVariance.OUT -> "out ${formatType(projType, contextPackage)}"
            }
        }

        return if (typeArgs.isEmpty()) {
            baseName + if (type.isNullable) "?" else ""
        } else {
            "$baseName<${typeArgs.joinToString(", ")}>" + if (type.isNullable) "?" else ""
        }
    }

    /**
     * Determines if a type is a well-known stdlib type that should use simple name.
     * Only types from kotlin.* packages are considered well-known, as LLMs
     * have reliable knowledge of Kotlin standard library types.
     * Java types (java.*) use fully qualified names to avoid ambiguity.
     */
    private fun isWellKnownType(fqn: String): Boolean {
        return fqn.startsWith("kotlin.")
    }

    private fun formatVisibility(visibility: Visibility): String {
        return when (visibility) {
            Visibility.PUBLIC -> ""  // public is default in Kotlin, no need to show
            Visibility.PRIVATE -> "private "
            Visibility.PROTECTED -> "protected "
            Visibility.INTERNAL -> "internal "
            Visibility.PRIVATE_TO_THIS -> "private "
            Visibility.LOCAL -> ""
        }
    }

    private fun hasMethodInHierarchy(clazz: Class<*>, methodName: String): Boolean {
        // Check interfaces
        for (iface in clazz.interfaces) {
            if (iface.methods.any { it.name == methodName }) {
                return true
            }
        }
        // Check superclass (excluding Object's methods like hashCode, equals, toString
        // which are almost always overrides but we might not want to mark them)
        clazz.superclass?.let { superclass ->
            if (superclass != Any::class.java) {
                if (superclass.methods.any { it.name == methodName }) {
                    return true
                }
            }
        }
        return false
    }

    private fun isMethodOverride(clazz: Class<*>, methodName: String): Boolean =
        hasMethodInHierarchy(clazz, methodName)

    private fun isPropertyOverride(clazz: Class<*>, propertyName: String): Boolean {
        val getterName = "get${propertyName.replaceFirstChar { it.uppercase() }}"
        return hasMethodInHierarchy(clazz, getterName)
    }

    private fun buildFullClasspathIndex(): Triple<Map<String, List<IndexedExtension>>, Map<String, List<IndexedExtensionProperty>>, Map<String, List<String>>> {
        val funcIndex = mutableMapOf<String, MutableList<IndexedExtension>>()
        val propIndex = mutableMapOf<String, MutableList<IndexedExtensionProperty>>()
        val clsIndex = mutableMapOf<String, MutableList<String>>()

        // Get all classpath entries
        val classpath = System.getProperty("java.class.path")
        val classpathEntries = classpath.split(java.io.File.pathSeparator)

        for (entry in classpathEntries) {
            val file = java.io.File(entry)
            if (!file.exists()) continue

            if (file.isDirectory) {
                scanDirectoryRecursively(file, "", funcIndex, propIndex, clsIndex)
            } else if (file.name.endsWith(".jar")) {
                scanJarFile(file, funcIndex, propIndex, clsIndex)
            }
        }

        return Triple(funcIndex, propIndex, clsIndex)
    }

    private fun scanDirectoryRecursively(
        directory: java.io.File,
        packagePrefix: String,
        funcIndex: MutableMap<String, MutableList<IndexedExtension>>,
        propIndex: MutableMap<String, MutableList<IndexedExtensionProperty>>,
        clsIndex: MutableMap<String, MutableList<String>>
    ) {
        directory.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(".class") && !file.name.contains('$')) {
                val className = if (packagePrefix.isEmpty()) {
                    file.name.removeSuffix(".class")
                } else {
                    "$packagePrefix.${file.name.removeSuffix(".class")}"
                }
                indexClassFromFile(className, funcIndex, propIndex, clsIndex)
            } else if (file.isDirectory) {
                val newPrefix = if (packagePrefix.isEmpty()) file.name else "$packagePrefix.${file.name}"
                scanDirectoryRecursively(file, newPrefix, funcIndex, propIndex, clsIndex)
            }
        }
    }

    private fun scanJarFile(
        jarFile: java.io.File,
        funcIndex: MutableMap<String, MutableList<IndexedExtension>>,
        propIndex: MutableMap<String, MutableList<IndexedExtensionProperty>>,
        clsIndex: MutableMap<String, MutableList<String>>
    ) {
        try {
            java.util.jar.JarFile(jarFile).use { jar ->
                jar.entries().asSequence()
                    .filter { it.name.endsWith(".class") && !it.name.contains('$') }
                    .forEach { entry ->
                        val className = entry.name
                            .removeSuffix(".class")
                            .replace('/', '.')
                        indexClassFromFile(className, funcIndex, propIndex, clsIndex)
                    }
            }
        } catch (_: Exception) {
            // Skip unreadable JARs
        }
    }

    private fun indexClassFromFile(
        className: String,
        funcIndex: MutableMap<String, MutableList<IndexedExtension>>,
        propIndex: MutableMap<String, MutableList<IndexedExtensionProperty>>,
        clsIndex: MutableMap<String, MutableList<String>>
    ) {
        val clazz = try {
            classLoader.loadClass(className)
        } catch (_: ClassNotFoundException) {
            return
        } catch (_: NoClassDefFoundError) {
            return
        } catch (_: LinkageError) {
            return
        }

        val metadataAnnotation = try {
            clazz.getAnnotation(Metadata::class.java)
        } catch (_: Exception) {
            return
        } ?: return

        val metadata = try {
            KotlinClassMetadata.readLenient(metadataAnnotation)
        } catch (_: Exception) {
            return
        }

        val (functions, properties) = when (metadata) {
            is KotlinClassMetadata.Class -> {
                // Index the class by its simple name (lowercase for case-insensitive search)
                val simpleName = className.substringAfterLast('.').lowercase()
                clsIndex.getOrPut(simpleName) { mutableListOf() }.add(className)
                metadata.kmClass.functions to metadata.kmClass.properties
            }
            is KotlinClassMetadata.FileFacade -> metadata.kmPackage.functions to metadata.kmPackage.properties
            is KotlinClassMetadata.MultiFileClassPart -> metadata.kmPackage.functions to metadata.kmPackage.properties
            else -> return
        }

        for (function in functions) {
            val receiverType = function.receiverParameterType ?: continue
            val receiverName = getReceiverTypeName(receiverType)

            funcIndex.getOrPut(receiverName) { mutableListOf() }
                .add(IndexedExtension(function, className))
        }

        for (property in properties) {
            val receiverType = property.receiverParameterType ?: continue
            val receiverName = getReceiverTypeName(receiverType)

            propIndex.getOrPut(receiverName) { mutableListOf() }
                .add(IndexedExtensionProperty(property, className))
        }
    }

    private fun getReceiverTypeName(type: KmType): String {
        return when (val classifier = type.classifier) {
            is KmClassifier.Class -> classifier.name.replace('/', '.')
            is KmClassifier.TypeAlias -> classifier.name.replace('/', '.')
            is KmClassifier.TypeParameter -> "TypeParameter"
        }
    }

    /**
     * Get extension functions for a given type name.
     */
    private fun getExtensionsFor(typeName: String): List<IndexedExtension> {
        // Try exact match first
        extensionIndex[typeName]?.let { return it }

        // Try with kotlin. prefix for built-in types
        extensionIndex["kotlin.$typeName"]?.let { return it }

        return emptyList()
    }

    /**
     * Get extension properties for a given type name.
     */
    private fun getExtensionPropertiesFor(typeName: String): List<IndexedExtensionProperty> {
        // Try exact match first
        extensionPropertyIndex[typeName]?.let { return it }

        // Try with kotlin. prefix for built-in types
        extensionPropertyIndex["kotlin.$typeName"]?.let { return it }

        return emptyList()
    }

    override fun resolve(
        name: String,
        page: Int,
        pageSize: Int
    ): String? {
        val content = resolve(name) ?: return null

        // Parse the content into class header, members, and extensions
        val lines = content.lines()

        // Find the class/interface declaration line (first line)
        val classDeclarationLine = lines.firstOrNull() ?: return null

        // Find closing brace of class body
        val closingBraceIndex = lines.indexOfFirst { it == "}" }
        if (closingBraceIndex < 0) {
            // No class body, return as-is
            val header = "// $name [page 1/1]"
            return "$header\n$content"
        }

        // Extract members (lines between class declaration and closing brace)
        // Preserve relative indentation by only removing the base 4-space indent
        val members = lines.subList(1, closingBraceIndex)
            .filter { it.isNotBlank() }
            .map { line ->
                // Remove exactly 4 spaces if present, preserving any additional indentation
                if (line.startsWith("    ")) line.substring(4) else line.trimStart()
            }

        // Extract extensions (lines after closing brace), preserving package headers
        // We need to parse extensions as items with their package context
        data class ExtensionItem(val line: String, val packageHeader: String?)
        val extensionItems = mutableListOf<ExtensionItem>()
        var currentPackage: String? = null

        if (closingBraceIndex < lines.size - 1) {
            for (line in lines.subList(closingBraceIndex + 1, lines.size)) {
                if (line.isBlank()) continue
                if (line.startsWith("// from ")) {
                    currentPackage = line
                } else {
                    extensionItems.add(ExtensionItem(line, currentPackage))
                }
            }
        }

        // Combine members and extensions for pagination
        // Members are represented as items without package context
        val allItems = members.map { ExtensionItem(it, null) } + extensionItems
        val totalItems = allItems.size

        // Calculate total pages based on all items (members + extensions)
        val totalPages = if (totalItems == 0) 1 else ((totalItems + pageSize - 1) / pageSize)
        val actualPage = page.coerceIn(1, totalPages)

        // Get items for this page
        val startIndex = (actualPage - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, totalItems)
        val pageItems = if (totalItems > 0) allItems.subList(startIndex, endIndex) else emptyList()

        // Build the page content
        return buildText {
            +"// $name [page $actualPage/$totalPages]\n"

            // Separate members (no package header) from extensions (have package header)
            val pageMembers = pageItems.filter { it.packageHeader == null }
            val pageExtensions = pageItems.filter { it.packageHeader != null }

            // Only include class body if there are members on this page
            if (pageMembers.isNotEmpty()) {
                +classDeclarationLine
                +"\n"
                for (item in pageMembers) {
                    +"    "
                    +item.line
                    +"\n"
                }
                +"}"
                if (pageExtensions.isNotEmpty()) {
                    +"\n"
                }
            }

            // Add extensions that are on this page
            if (pageExtensions.isNotEmpty()) {
                var lastPackage: String? = null
                for (ext in pageExtensions) {
                    if (ext.packageHeader != lastPackage) {
                        +ext.packageHeader!! // TODO could it be null?
                        +"\n"
                        lastPackage = ext.packageHeader
                    }
                    +ext.line
                    +"\n"
                }
            }

            trimLastNewLine()
        }

    }

    override fun search(query: String, page: Int, pageSize: Int): String? {
        val queryLower = query.lowercase()

        // Find all matching classes: classes whose simple name contains the query (case-insensitive)
        val matchingClasses = classIndex.entries
            .filter { (simpleName, _) -> simpleName.contains(queryLower) }
            .flatMap { (_, fqns) -> fqns }
            .sorted()  // Sort alphabetically by fully qualified name

        if (matchingClasses.isEmpty()) {
            return null
        }

        // Calculate pagination
        val totalItems = matchingClasses.size
        val totalPages = (totalItems + pageSize - 1) / pageSize
        val actualPage = page.coerceIn(1, totalPages)

        // Get items for this page
        val startIndex = (actualPage - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, totalItems)
        val pageItems = matchingClasses.subList(startIndex, endIndex)

        // Build the result
        return buildText {
            +"// search results for \"$query\" [page $actualPage/$totalPages]\n"
            for (className in pageItems) {
                +className
                +"\n"
            }
            trimLastNewLine()
        }
    }
}
