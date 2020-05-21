package com.coditory.gradle.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.java.archives.Manifest
import org.gradle.api.plugins.JavaPlugin.JAR_TASK_NAME
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.jvm.tasks.Jar
import java.nio.file.Path

internal open class ManifestTask : DefaultTask() {
    private var print: Boolean = false
    private var main: Boolean = false

    @Option(option = "print", description = "Prints out the manifest.")
    open fun setPrint(print: Boolean = true) {
        this.print = print
    }

    @Input
    open fun getPrint(): Boolean {
        return print
    }

    @Option(option = "main", description = "Writes manifest to src/main/resources.")
    open fun setMain(main: Boolean = true) {
        this.main = main
    }

    @Input
    open fun getMain(): Boolean {
        return main
    }

    @Suppress("UnstableApiUsage")
    @TaskAction
    fun generateManifest() {
        val manifest = project.tasks
            .named(JAR_TASK_NAME, Jar::class.java).get()
            .manifest
        if (print) {
            printManifest(manifest)
        }
        if (main) {
            generateManifestToResources(manifest)
        } else {
            generateManifestToOutput(manifest)
        }
    }

    private fun printManifest(manifest: Manifest) {
        println("")
        println("MANIFEST.MF")
        println("===========")
        manifest.attributes.forEach {
            println(it.key + ": " + it.value)
        }
        println("")
    }

    private fun generateManifestToOutput(manifest: Manifest) {
        val resourcePath = project
            .convention.getPlugin(JavaPluginConvention::class.java)
            .sourceSets.getByName(MAIN_SOURCE_SET_NAME)
            .output.resourcesDir?.toPath() // Law of Demeter? xD
        if (resourcePath != null) {
            writeManifest(manifest, resourcePath)
        }
    }

    private fun generateManifestToResources(manifest: Manifest) {
        val resourcePath = project
            .convention.getPlugin(JavaPluginConvention::class.java)
            .sourceSets.getByName(MAIN_SOURCE_SET_NAME)
            .resources.srcDirs
            .firstOrNull()
        if (resourcePath != null) {
            writeManifest(manifest, resourcePath.toPath())
        }
    }

    private fun writeManifest(manifest: Manifest, resourcePath: Path) {
        manifest.writeTo(resourcePath.resolve(MANIFEST_PATH))
    }

    companion object {
        private const val MANIFEST_PATH = "META-INF/MANIFEST.MF"
    }
}
