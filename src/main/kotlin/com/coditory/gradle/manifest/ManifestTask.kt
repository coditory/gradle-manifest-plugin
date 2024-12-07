package com.coditory.gradle.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPlugin.JAR_TASK_NAME
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.jvm.tasks.Jar
import java.nio.file.Path

open class ManifestTask : DefaultTask() {
    private val manifest = project.tasks
        .named(JAR_TASK_NAME, Jar::class.java).get()
        .manifest
    private val outputPath = project.extensions.getByType(JavaPluginExtension::class.java)
        .sourceSets
        .getByName(MAIN_SOURCE_SET_NAME)
        .output.resourcesDir?.toPath()
    private val srcMainPath = project.extensions.getByType(JavaPluginExtension::class.java)
        .sourceSets
        .getByName(MAIN_SOURCE_SET_NAME)
        .resources.srcDirs
        .firstOrNull()?.toPath()

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

    @TaskAction
    fun generateManifest() {
        if (print) {
            printManifest()
        }
        if (main) {
            generateManifestToResources()
        } else {
            generateManifestToOutput()
        }
    }

    private fun printManifest() {
        println("")
        println("MANIFEST.MF")
        println("===========")
        manifest.attributes.forEach {
            println(it.key + ": " + it.value)
        }
        println("")
    }

    private fun generateManifestToOutput() {
        if (outputPath != null) {
            writeManifest(outputPath)
        }
    }

    private fun generateManifestToResources() {
        if (srcMainPath != null) {
            writeManifest(srcMainPath)
        }
    }

    private fun writeManifest(resourcePath: Path) {
        manifest.writeTo(resourcePath.resolve(MANIFEST_PATH))
    }

    companion object {
        private const val MANIFEST_PATH = "META-INF/MANIFEST.MF"
    }
}
