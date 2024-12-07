package com.coditory.gradle.manifest

import com.coditory.gradle.manifest.base.TestProjectBuilder
import com.coditory.gradle.manifest.base.generateManifest
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.junit.jupiter.api.Test

class GenerateManifestWithoutDisabledAttributesTest {
    @Test
    fun `should generate manifest without scm attributes`() {
        // when
        val manifest = project().generateManifest { it.scmAttributes = false }
        // then
        assertThat(manifest)
            .contains("Implementation-")
            .contains("Built-")
            .doesNotContain("SCM-")
    }

    @Test
    fun `should generate manifest without build attributes`() {
        // when
        val manifest = project().generateManifest { it.buildAttributes = false }
        // then
        assertThat(manifest)
            .contains("Implementation-")
            .doesNotContain("Built-")
            .contains("SCM-")
    }

    @Test
    fun `should generate manifest without implementation attributes`() {
        // when
        val manifest = project().generateManifest { it.implementationAttributes = false }
        // then
        assertThat(manifest)
            .doesNotContain("Implementation-")
            .contains("Built-")
            .contains("SCM-")
    }

    private fun project(): Project {
        return TestProjectBuilder.projectWithPlugins()
            .withGroup("com.coditory")
            .withGitRepository()
            .withMainClass("com.coditory.MainClass")
            .build()
    }
}
