package com.coditory.gradle.manifest

import com.coditory.gradle.manifest.ManifestPlugin.Companion.GENERATE_MANIFEST_TASK
import com.coditory.gradle.manifest.base.SpecProjectBuilder
import com.coditory.gradle.manifest.base.SpecProjectBuilder.Companion.projectWithPlugins
import com.coditory.gradle.manifest.base.SystemOutputCapturer.Companion.captureSystemOutput
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.junit.jupiter.api.Test
import java.io.File

class GenerateManifestSpec {
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
        "SCM-Commit-Date:"
    )

    @Test
    fun `should generate manifest to output`() {
        // given
        val project = projectBuilder().build()
        val manifestTask = getManifestTask(project)

        // when
        manifestTask.generateManifest()

        // then
        assertThat(file(project, "build/resources/main/META-INF/MANIFEST.MF").readText())
            .contains(expectedManifestKeys)
    }

    @Test
    fun `should generate manifest to output and idea output when idea project is detected`() {
        // given
        val project = projectBuilder()
            .withIdeaProjectFiles()
            .build()
        val manifestTask = getManifestTask(project)

        // when
        manifestTask.generateManifest()

        // then
        assertThat(file(project, "build/resources/main/META-INF/MANIFEST.MF").readText())
            .contains(expectedManifestKeys)
        // and
        assertThat(file(project, "out/production/resources/META-INF/MANIFEST.MF").readText())
            .contains(expectedManifestKeys)
    }

    @Test
    fun `should generate manifest to src-main-resources on --main option`() {
        // given
        val project = projectBuilder().build()
        val manifestTask = getManifestTask(project)
        manifestTask.setMain()

        // when
        manifestTask.generateManifest()

        // then
        assertThat(file(project, "src/main/resources/META-INF/MANIFEST.MF").readText())
            .contains(expectedManifestKeys)
    }

    @Test
    fun `should generate manifest and print it to stdout on --print option`() {
        // given
        val project = projectBuilder().build()
        val manifestTask = getManifestTask(project)
        manifestTask.setPrint(true)

        // when
        val output = captureSystemOutput()
            .use {
                manifestTask.generateManifest()
                it.readSystemOut()
            }

        // then
        assertThat(file(project, "build/resources/main/META-INF/MANIFEST.MF"))
            .exists()

        // and
        assertThat(output).contains(expectedManifestKeys)
    }

    private fun projectBuilder(): SpecProjectBuilder {
        return projectWithPlugins("sample-project")
            .withGroup("com.coditory")
            .withGitRepository()
            .withExtProperty("mainClassName", "com.coditory.MainClass")
    }

    private fun getManifestTask(project: Project): ManifestTask {
        return project.tasks
            .named(GENERATE_MANIFEST_TASK, ManifestTask::class.java).get()
    }

    private fun file(project: Project, path: String): File {
        return project.projectDir.resolve(path)
            .toPath()
            .toFile()
    }
}
