package com.coditory.gradle.manifest

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

object BackwardCompatibilities {
    @Suppress("DEPRECATION")
    fun archivesBaseName(project: Project): String {
        return if (usesExtension(project)) {
            project.extensions.getByType(org.gradle.api.plugins.BasePluginExtension::class.java)
                .archivesBaseName
        } else {
            project.convention.getPlugin(org.gradle.api.plugins.internal.DefaultBasePluginConvention::class.java)
                .archivesBaseName
        }
    }

    @Suppress("DEPRECATION")
    fun sourceSets(project: Project): SourceSetContainer {
        return if (usesExtension(project)) {
            project.extensions.getByType(org.gradle.api.plugins.JavaPluginExtension::class.java)
                .sourceSets
        } else {
            project.convention.getPlugin(org.gradle.api.plugins.JavaPluginConvention::class.java)
                .sourceSets
        }
    }

    private fun usesExtension(project: Project): Boolean {
        val chunks = project.gradle.gradleVersion.split('.')
        if (chunks.isEmpty()) {
            return false
        }
        val major = chunks.getOrNull(0)?.toInt() ?: 0
        val minor = chunks.getOrNull(1)?.toInt() ?: 0
        return major > 7 || (major == 7 && minor >= 1)
    }
}
