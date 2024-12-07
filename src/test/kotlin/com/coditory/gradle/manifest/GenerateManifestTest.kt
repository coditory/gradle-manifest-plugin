package com.coditory.gradle.manifest

import com.coditory.gradle.manifest.base.SystemOutputCapturer.Companion.captureSystemOutput
import com.coditory.gradle.manifest.base.TestProjectBuilder
import com.coditory.gradle.manifest.base.TestProjectBuilder.Companion.projectWithPlugins
import com.coditory.gradle.manifest.base.getManifestTask
import com.coditory.gradle.manifest.base.readFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GenerateManifestTest {
    private val expectedManifestKeys = listOf(
        "Manifest-Version:",
        "Main-Class:",
        "Implementation-Title:",
        "Implementation-Group:",
        "Implementation-Version:",
        "Built-By:",
        "Built-Host:",
        "Built-Date:",
        "Built-OS:",
        "Built-JDK:",
        "SCM-Repository:",
        "SCM-Branch:",
        "SCM-Commit-Message:",
        "SCM-Commit-Hash:",
        "SCM-Commit-Author:",
        "SCM-Commit-Date:",
    )

    @Test
    fun `should generate manifest to output`() {
        // given
        val project = projectBuilder().build()
        val manifestTask = project.getManifestTask()

        // when
        manifestTask.generateManifest()

        // then
        assertThat(project.readFile("build/resources/main/META-INF/MANIFEST.MF"))
            .contains(expectedManifestKeys)
    }

    @Test
    fun `should generate manifest to output and idea output when idea project is detected`() {
        // given
        val project = projectBuilder()
            .withIdeaProjectFiles()
            .build()
        val manifestTask = project.getManifestTask()

        // when
        manifestTask.generateManifest()

        // then
        assertThat(project.readFile("build/resources/main/META-INF/MANIFEST.MF"))
            .contains(expectedManifestKeys)
    }

    @Test
    fun `should generate manifest to src-main-resources on --main option`() {
        // given
        val project = projectBuilder().build()
        val manifestTask = project.getManifestTask()
        manifestTask.setMain()

        // when
        manifestTask.generateManifest()

        // then
        assertThat(project.readFile("src/main/resources/META-INF/MANIFEST.MF"))
            .contains(expectedManifestKeys)
    }

    @Test
    fun `should generate manifest and print it to stdout on --print option`() {
        // given
        val project = projectBuilder().build()
        val manifestTask = project.getManifestTask()
        manifestTask.setPrint(true)

        // when
        val output = captureSystemOutput()
            .use {
                manifestTask.generateManifest()
                it.readSystemOut()
            }

        // then
        project.readFile("build/resources/main/META-INF/MANIFEST.MF")
            .isNotEmpty()

        // and
        assertThat(output).contains(expectedManifestKeys)
    }

    private fun projectBuilder(): TestProjectBuilder {
        return projectWithPlugins("sample-project")
            .withGroup("com.coditory")
            .withGitRepository()
            .withMainClass("com.coditory.MainClass")
    }
}
