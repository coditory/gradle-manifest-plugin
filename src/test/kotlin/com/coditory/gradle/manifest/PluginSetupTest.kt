package com.coditory.gradle.manifest

import com.coditory.gradle.manifest.ManifestPlugin.Companion.GENERATE_MANIFEST_TASK
import com.coditory.gradle.manifest.base.TestProjectBuilder.Companion.projectWithPlugins
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.plugins.JavaPlugin
import org.gradle.language.jvm.tasks.ProcessResources
import org.junit.jupiter.api.Test

class PluginSetupTest {
    private val project = projectWithPlugins()
        .build()

    @Test
    fun `should register manifest task to run after processResources`() {
        val task = project.tasks.getByName(GENERATE_MANIFEST_TASK)
        val processResources = project.tasks
            .named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME, ProcessResources::class.java).get()
        assertThat(processResources.finalizedBy.getDependencies(processResources))
            .contains(task)
    }
}
