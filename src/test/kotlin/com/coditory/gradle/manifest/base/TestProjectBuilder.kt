package com.coditory.gradle.manifest.base

import com.coditory.gradle.manifest.base.TestRepository.Companion.repository
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

class TestProjectBuilder private constructor(projectDir: File, name: String) {
    private val project = ProjectBuilder.builder()
        .withProjectDir(projectDir)
        .withName(name)
        .build()

    fun withGroup(group: String): TestProjectBuilder {
        project.group = group
        return this
    }

    fun withVersion(version: String): TestProjectBuilder {
        project.version = version
        return this
    }

    fun withExtProperty(name: String, value: String): TestProjectBuilder {
        project.extensions.extraProperties[name] = value
        return this
    }

    fun withPlugins(vararg plugins: KClass<out Plugin<*>>): TestProjectBuilder {
        plugins
            .toList()
            .onEach { if (it.isSuperclassOf(ManifestPluginWithStubs::class)) ManifestPluginWithStubs.clock.reset() }
            .forEach { project.plugins.apply(it.java) }
        return this
    }

    fun withGitRepository(): TestProjectBuilder {
        repository(project)
            .withRemote()
            .withCommit()
        return this
    }

    fun withIdeaProjectFiles(): TestProjectBuilder {
        project.rootDir.resolve(".idea").createNewFile()
        return this
    }

    fun withBuildGradle(content: String): TestProjectBuilder {
        val buildFile = project.rootDir.resolve("build.gradle")
        buildFile.writeText(content.trimIndent().trim())
        return this
    }

    fun withFile(path: String, content: String): TestProjectBuilder {
        val filePath = project.rootDir.resolve(path).toPath()
        Files.createDirectories(filePath.parent)
        val testFile = Files.createFile(filePath).toFile()
        testFile.writeText(content.trimIndent().trim())
        return this
    }

    fun build(): Project {
        return project
    }

    companion object {
        private var projectDirs = mutableListOf<File>()

        fun project(name: String = "sample-project", projectDir: File): TestProjectBuilder {
            projectDir.mkdir()
            projectDirs.add(projectDir)
            return TestProjectBuilder(projectDir, name)
        }

        fun project(name: String = "sample-project"): TestProjectBuilder {
            return TestProjectBuilder(createProjectDir(name), name)
        }

        fun projectWithPlugins(name: String = "sample-project"): TestProjectBuilder {
            return project(name)
                .withPlugins(JavaPlugin::class, ManifestPluginWithStubs::class)
        }

        @Suppress("EXPERIMENTAL_API_USAGE_ERROR")
        private fun createProjectDir(directory: String): File {
            removeProjectDirs()
            val projectParentDir = createTempDirectory().toFile()
            val projectDir = projectParentDir.resolve(directory)
            projectDir.mkdir()
            projectDirs.add(projectParentDir)
            return projectDir
        }

        fun removeProjectDirs() {
            projectDirs.forEach {
                it.deleteRecursively()
            }
        }
    }
}
