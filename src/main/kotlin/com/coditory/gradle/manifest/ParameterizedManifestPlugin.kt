package com.coditory.gradle.manifest

import com.coditory.gradle.manifest.ManifestAttributes.fillAttributes
import com.coditory.gradle.manifest.ManifestPlugin.Companion.GENERATE_MANIFEST_TASK
import com.coditory.gradle.manifest.ManifestPlugin.Companion.MANIFEST_EXTENSION
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP
import java.time.Clock

open class ParameterizedManifestPlugin(
    private val clock: Clock,
    private val hostNameResolver: HostNameResolver
) : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin(JavaPlugin::class.java)) {
            project.plugins.apply(JavaPlugin::class.java)
        }
        val extension = setupExtension(project)
        setupPlugin(project, extension)
    }

    private fun setupExtension(project: Project): ManifestPluginExtension {
        return project.extensions.create(MANIFEST_EXTENSION, ManifestPluginExtension::class.java)
    }

    private fun setupPlugin(project: Project, extension: ManifestPluginExtension) {
        fillAttributes(clock, hostNameResolver, project, extension)
        setupGenerateManifestTask(project)
    }

    @Suppress("UnstableApiUsage")
    private fun setupGenerateManifestTask(project: Project) {
        val manifestTask = project.tasks.register(GENERATE_MANIFEST_TASK, ManifestTask::class.java) {
            it.description = "Generates META-INF/MANIFEST.MF in resources directory with project metadata"
            it.group = BUILD_GROUP
        }
        project.tasks.getByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME)
            .finalizedBy(manifestTask)
    }
}
