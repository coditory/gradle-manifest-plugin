package com.coditory.gradle.manifest.base

import org.gradle.api.Project
import org.gradle.api.java.archives.Attributes
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jvm.tasks.Jar

object ManifestExtractor {
    fun extractManifestMap(project: Project): Map<String, String?> {
        return extractManifestAttributes(project)
            .mapValues { it.value.toString() }
    }

    fun extractManifestAttributes(project: Project): Attributes {
        return project.tasks.named(JavaPlugin.JAR_TASK_NAME, Jar::class.java).get()
            .manifest.effectiveManifest.attributes
    }
}
