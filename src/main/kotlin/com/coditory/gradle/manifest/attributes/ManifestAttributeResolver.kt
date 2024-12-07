package com.coditory.gradle.manifest.attributes

import com.coditory.gradle.manifest.ManifestPluginExtension
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPlugin.JAR_TASK_NAME
import org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME
import org.gradle.jvm.tasks.Jar
import java.nio.file.Paths
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit.SECONDS

internal object ManifestAttributeResolver {
    fun fillManifestAttributes(clock: Clock, hostNameResolver: HostNameResolver, project: Project) {
        project.tasks.named(JAR_TASK_NAME, Jar::class.java) {
            val attributes = project.tasks
                .named(JAR_TASK_NAME, Jar::class.java).get()
                .manifest.attributes
            val generated = resolveAttributes(clock, hostNameResolver, project)
                .filter { !attributes.containsKey(it.first) }
            attributes.putAll(generated)
        }
    }

    private fun resolveAttributes(
        clock: Clock,
        hostNameResolver: HostNameResolver,
        project: Project,
    ): List<Pair<String, String?>> {
        val extension = project.extensions.getByType(ManifestPluginExtension::class.java)
        return mainClassAttribute(project)
            .plus(implementationAttributes(project, extension))
            .plus(buildAttributes(clock, hostNameResolver, extension))
            .plus(scmAttributes(project, extension))
            .plus(customAttributes(extension))
            .plus(classpathAttribute(project, extension))
            .map { it.key to it.value?.toString()?.trim() }
            .filter { !it.second.isNullOrBlank() }
    }

    private fun customAttributes(extension: ManifestPluginExtension): Map<String, Any?> {
        val attributes = extension.attributes
        if (attributes.isNullOrEmpty()) {
            return mapOf()
        }
        return attributes.entries
            .filter { it.key.isNotBlank() && it.value != null }
            .associate { it.key to orEmpty { it.value } }
    }

    private fun classpathAttribute(project: Project, extension: ManifestPluginExtension): Map<String, Any?> {
        val classpathPrefix = extension.classpathPrefix ?: return mapOf()
        val classPath = project.configurations.getByName(RUNTIME_CLASSPATH_CONFIGURATION_NAME)
            .map { Paths.get(classpathPrefix, it.name).toString() }
            .joinToString(" ") { it.replace('\\', '/').replace("//+".toRegex(), "/") }
        return mapOf("Class-Path" to classPath)
    }

    private fun implementationTitle(project: Project): String {
        return project.extensions.getByType(BasePluginExtension::class.java)
            .archivesName.get()
    }

    private fun systemProperties(vararg names: String): String {
        return names
            .map { System.getProperty(it)?.trim() }
            .filter { !it.isNullOrBlank() }
            .joinToString(" ")
    }

    private fun mainClassAttribute(project: Project): Map<String, Any?> {
        val javaApplication = project.extensions.findByType(JavaApplication::class.java)
        return mapOf(
            "Main-Class" to orEmpty { javaApplication?.mainClass?.orNull },
        )
    }

    private fun implementationAttributes(
        project: Project,
        extension: ManifestPluginExtension,
    ): Map<String, Any?> {
        if (!extension.implementationAttributes) {
            return mapOf()
        }
        return mapOf(
            "Implementation-Title" to lazy { implementationTitle(project) },
            "Implementation-Group" to lazy { project.group },
            "Implementation-Version" to lazy { project.version },
        )
    }

    private fun buildAttributes(
        clock: Clock,
        hostNameResolver: HostNameResolver,
        extension: ManifestPluginExtension,
    ): Map<String, String?> {
        if (!extension.buildAttributes) {
            return mapOf()
        }
        return mapOf(
            "Built-By" to systemProperties("user.name"),
            "Built-Host" to orEmpty { hostNameResolver.resolveHostName() },
            "Built-Date" to format(clock.instant()),
            "Built-OS" to systemProperties("os.name", "os.version", "os.arch"),
            "Built-JDK" to systemProperties("java.version", "java.vendor"),
        )
    }

    private fun scmAttributes(project: Project, extension: ManifestPluginExtension): Map<String, String?> {
        if (!extension.scmAttributes) {
            return mapOf()
        }
        val repository = project.providers.of(RepositoryValueSource::class.java) { config ->
            config.parameters { it.projectDir.set(project.rootProject.projectDir) }
        }.get()
        return mapOf(
            "SCM-Repository" to orEmpty { repository.url },
            "SCM-Branch" to orEmpty { repository.branch },
            "SCM-Commit-Message" to orEmpty { repository.commitMessage },
            "SCM-Commit-Hash" to orEmpty { repository.commitHash },
            "SCM-Commit-Author" to orEmpty {
                repository.commitAuthorName?.let {
                    if (repository.commitAuthorEmail != null) {
                        "${repository.commitAuthorName} <${repository.commitAuthorEmail}>"
                    } else {
                        repository.commitAuthorName
                    }
                }
            },
            "SCM-Commit-Date" to orEmpty { repository.commitEpochSeconds?.let { format(Instant.ofEpochSecond(it)) } },
        )
    }

    private fun format(instant: Instant): String {
        return instant.truncatedTo(SECONDS).toString()
    }

    private fun lazy(provider: () -> Any?): Any {
        return object {
            override fun toString(): String {
                return orEmpty(provider)
            }
        }
    }

    private fun orEmpty(provider: () -> Any?): String {
        return try {
            provider()?.toString() ?: ""
        } catch (_: Throwable) {
            ""
        }
    }
}
