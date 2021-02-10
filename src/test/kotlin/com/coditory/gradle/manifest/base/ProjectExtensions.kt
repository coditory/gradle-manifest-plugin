package com.coditory.gradle.manifest.base

import com.coditory.gradle.manifest.ManifestPlugin
import com.coditory.gradle.manifest.ManifestPluginExtension
import com.coditory.gradle.manifest.ManifestTask
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

fun Project.runGradle(arguments: List<String>, gradleVersion: String? = null): BuildResult {
    val builder = GradleRunner.create()
        .withProjectDir(this.projectDir)
        .withArguments(arguments)
        .withPluginClasspath()
        .forwardOutput()
    if (!gradleVersion.isNullOrBlank() && gradleVersion != "current") {
        builder.withGradleVersion(gradleVersion)
    }
    return builder.build()
}

fun Project.generateManifest(configure: (ManifestPluginExtension) -> Unit = {}): String {
    this.extensions.configure(ManifestPluginExtension::class.java, configure)
    getManifestTask().generateManifest()
    return this.readFile("build/resources/main/META-INF/MANIFEST.MF")
}

fun Project.getManifestTask(): ManifestTask {
    return this.tasks
        .named(ManifestPlugin.GENERATE_MANIFEST_TASK, ManifestTask::class.java)
        .get()
}

fun Project.writeFile(path: String, content: String): Project {
    val resolved = this.projectDir.resolve(path)
    resolved.parentFile.mkdirs()
    resolved.writeText(content)
    return this
}

fun Project.readFile(path: String): String {
    return this.projectDir.resolve(path)
        .readText()
}
