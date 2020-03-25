package com.coditory.gradle.manifest

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants.HEAD
import org.gradle.api.Project
import org.gradle.api.java.archives.Attributes
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jvm.tasks.Jar
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit.SECONDS

internal object ManifestAttributes {
    @Suppress("UnstableApiUsage")
    fun fillAttributes(clock: Clock, hostNameResolver: HostNameResolver, project: Project) {
        project.tasks.named(JavaPlugin.JAR_TASK_NAME, Jar::class.java) {
            fillAttributes(clock, hostNameResolver, project, it.manifest.attributes)
        }
    }

    private fun fillAttributes(clock: Clock, hostNameResolver: HostNameResolver, project: Project, attributes: Attributes) {
        mapOf(
            "Main-Class" to orEmpty { project.properties["mainClassName"] },
            "Implementation-Title" to lazy { project.name },
            "Implementation-Group" to lazy { project.group },
            "Implementation-Version" to lazy { project.version },
            "Built-By" to systemProperties("user.name"),
            "Built-Host" to orEmpty { hostNameResolver.resolveHostName() },
            "Built-Date" to format(clock.instant()),
            "Built-OS" to systemProperties("os.name", "os.version", "os.arch"),
            "Built-JDK" to systemProperties("java.version", "java.vendor")
        )
            .plus(scmAttributes(project))
            .filter { it.value != null && it.value != "" }
            .filter { !attributes.containsKey(it.key) }
            .forEach { attributes[it.key] = it.value }
    }

    private fun systemProperties(vararg names: String): String {
        return names
            .map { System.getProperty(it) }
            .filter { !it.isNullOrBlank() }
            .joinToString(" ")
    }

    private fun scmAttributes(project: Project): Map<String, String?> {
        return try {
            gitAttributes(project)
        } catch (e: Throwable) {
            mapOf()
        }
    }

    private fun gitAttributes(project: Project): Map<String, String?> {
        val repository = Git.open(project.projectDir).repository
        val head = repository.parseCommit(repository.resolve(HEAD))
        return mapOf(
            "SCM-Repository" to orEmpty { repository.config.getString("remote", "origin", "url") },
            "SCM-Branch" to orEmpty { repository.fullBranch },
            "SCM-Commit-Message" to orEmpty { head.shortMessage },
            "SCM-Commit-Hash" to orEmpty { head.name() },
            "SCM-Commit-Author" to orEmpty { "${head.authorIdent.name} <${head.authorIdent.emailAddress}>" },
            "SCM-Commit-Date" to orEmpty { format(head.authorIdent.`when`.toInstant()) }
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
        } catch (e: Throwable) {
            ""
        }
    }
}
