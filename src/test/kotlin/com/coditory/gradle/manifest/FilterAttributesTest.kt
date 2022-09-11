package com.coditory.gradle.manifest

import com.coditory.gradle.manifest.base.ManifestExtractor.extractManifestMap
import com.coditory.gradle.manifest.base.TestProjectBuilder.Companion.projectWithPlugins
import com.coditory.gradle.manifest.base.generateManifest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FilterAttributesTest {
    @Test
    fun `should filter out some exact attributes`() {
        // given
        val project = projectWithPlugins()
            .build()

        // when
        project.generateManifest {
            it.attributesFilter = listOf(
                "!Built-By",
                "!Built-Date",
                "!Built-Host",
                "!Built-JDK",
                "!Built-OS"
            )
        }

        // then
        val manifest = extractManifestMap(project)
        assertThat(manifest).isEqualTo(
            mapOf(
                "Implementation-Title" to "sample-project",
                "Implementation-Version" to "unspecified",
                "Manifest-Version" to "1.0"
            )
        )
    }

    @Test
    fun `should filter out some exact attributes with case insensitivity`() {
        // given
        val project = projectWithPlugins()
            .build()

        // when
        project.generateManifest {
            it.attributesFilter = listOf(
                "!built-by",
                "!built-Date",
                "!Built-host",
                "!BUILT-JDK",
                "!BuIlT-OS"
            )
        }

        // then
        val manifest = extractManifestMap(project)
        assertThat(manifest).isEqualTo(
            mapOf(
                "Implementation-Title" to "sample-project",
                "Implementation-Version" to "unspecified",
                "Manifest-Version" to "1.0"
            )
        )
    }

    @Test
    fun `should filter out blacklisted attributes`() {
        // given
        val project = projectWithPlugins()
            .build()

        // when
        project.generateManifest {
            it.attributesFilter = listOf("!Built-*")
        }

        // then
        val manifest = extractManifestMap(project)
        assertThat(manifest).isEqualTo(
            mapOf(
                "Implementation-Title" to "sample-project",
                "Implementation-Version" to "unspecified",
                "Manifest-Version" to "1.0"
            )
        )
    }

    @Test
    fun `should pass whitelisted attributes only and Manifest-Version`() {
        // given
        val project = projectWithPlugins()
            .build()

        // when
        project.generateManifest {
            it.attributesFilter = listOf("Implementation-*")
        }

        // then
        val manifest = extractManifestMap(project)
        assertThat(manifest).isEqualTo(
            mapOf(
                "Implementation-Title" to "sample-project",
                "Implementation-Version" to "unspecified",
                "Manifest-Version" to "1.0"
            )
        )
    }

    @Test
    fun `should filter out blacklisted and pass whitelisted attributes`() {
        // given
        val project = projectWithPlugins()
            .build()

        // when
        project.generateManifest {
            it.attributesFilter = listOf("Implementation-*", "!Implementation-T*")
        }

        // then
        val manifest = extractManifestMap(project)
        assertThat(manifest).isEqualTo(
            mapOf(
                "Implementation-Version" to "unspecified",
                "Manifest-Version" to "1.0"
            )
        )
    }
}
