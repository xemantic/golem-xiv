plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
//    alias(libs.plugins.kotlin.plugin.serialization)
//    alias(libs.plugins.kotlin.plugin.power.assert)
////    alias(libs.plugins.kotlinx.binary.compatibility.validator)
//    alias(libs.plugins.dokka)
    alias(libs.plugins.versions)
    `maven-publish`
    signing
}

// TODO change the group
group = "com.xemantic.ai"

//// TODO fill up the details
//xemantic {
//    description = "A template repository for Xemantic's Kotlin multiplatform projects"
//    inceptionYear = 2025
//    license = License.APACHE
//    developer(
//        id = "morisil",
//        name = "Kazik Pogoda",
//        email = "morisil@xemantic.com"
//    )
//}


//val javaTarget = libs.versions.javaTarget.get()
//val kotlinTarget = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())


allprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<AbstractArchiveTask> {
        // Only set if not already set
        if (!archiveBaseName.isPresent) {
            archiveBaseName.set(project.name)
        }
        archiveVersion.set(project.version.toString())
    }
    apply(plugin = "maven-publish")
}

// https://kotlinlang.org/docs/dokka-migration.html#adjust-configuration-options
//dokka {
//    pluginsConfiguration.html {
//        footerMessage.set(xemantic.copyright)
//    }
//}
//
//val javadocJar by tasks.registering(Jar::class) {
//    archiveClassifier.set("javadoc")
//    from(tasks.dokkaGeneratePublicationHtml)
//}

//publishing {
//    publications {
//        withType<MavenPublication> {
//            artifact(javadocJar)
//            xemantic.configurePom(this)
//        }
//    }
//}

//jreleaser {
//    project {
//        description = xemantic.description
//        copyright = xemantic.copyright
//        license = xemantic.license!!.spxdx
//        links {
//            homepage = xemantic.homepageUrl
//            documentation = xemantic.documentationUrl
//        }
//        authors = xemantic.authorIds
//    }
//    deploy {
//        maven {
//            mavenCentral {
//                create("maven-central") {
//                    active = Active.ALWAYS
//                    url = "https://central.sonatype.com/api/v1/publisher"
//                    applyMavenCentralRules = false
//                    maxRetries = 240
//                    stagingRepository(xemantic.stagingDeployDir.path)
//                    // workaround: https://github.com/jreleaser/jreleaser/issues/1784
////                    kotlin.targets.forEach { target ->
////                        if (target !is KotlinJvmTarget) {
////                            val nonJarArtifactId = if (target.platformType == KotlinPlatformType.wasm) {
////                                "${name}-wasm-${target.name.lowercase().substringAfter("wasm")}"
////                            } else {
////                                "${name}-${target.name.lowercase()}"
////                            }
////                            artifactOverride {
////                                artifactId = nonJarArtifactId
////                                jar = false
////                                verifyPom = false
////                                sourceJar = false
////                                javadocJar = false
////                            }
////                        }
////                    }
//                }
//            }
//        }
//    }
//    release {
//        github {
//            skipRelease = true // we are releasing through GitHub UI
//            skipTag = true
//            token = "empty"
//            changelog {
//                enabled = false
//            }
//        }
//    }
//    checksum {
//        individual = false
//        artifacts = false
//        files = false
//    }
//    announce {
//        webhooks {
//            create("discord") {
//                active = Active.ALWAYS
//                message = releaseAnnouncement
//                messageProperty = "content"
//                structuredMessage = true
//            }
//        }
//        linkedin {
//            active = Active.ALWAYS
//            subject = releaseAnnouncementSubject
//            message = releaseAnnouncement
//        }
//        bluesky {
//            active = Active.ALWAYS
//            status = releaseAnnouncement
//        }
//    }
//}
