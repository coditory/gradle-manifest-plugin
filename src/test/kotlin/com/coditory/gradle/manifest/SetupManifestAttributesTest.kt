package com.coditory.gradle.manifest

import com.coditory.gradle.manifest.base.ManifestExtractor.extractManifestAttributes
import com.coditory.gradle.manifest.base.ManifestExtractor.extractManifestMap
import com.coditory.gradle.manifest.base.SystemProperties.withSystemProperties
import com.coditory.gradle.manifest.base.SystemProperties.withoutSystemProperties
import com.coditory.gradle.manifest.base.TestProjectBuilder.Companion.projectWithPlugins
import com.coditory.gradle.manifest.base.TestRepository.Companion.repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SetupManifestAttributesTest {
    @Test
    fun `should generate basic attributes for bare bone project with no git and no system properties`() {
        // given
        val project = projectWithPlugins("sample-project")
            .build()

        // when
        val manifest = withoutSystemProperties {
            extractManifestMap(project)
        }

        // then
        assertThat(manifest).isEqualTo(
            mapOf(
                "Built-Date" to "2015-12-03T10:15:30Z",
                "Built-Host" to "localhost",
                "Implementation-Title" to "sample-project",
                "Implementation-Version" to "unspecified",
                "Manifest-Version" to "1.0"
            )
        )
    }

    @Test
    fun `should generate attributes from project and system properties`() {
        // given
        val project = projectWithPlugins("sample-project")
            .withGroup("com.coditory")
            .withVersion("0.0.1-SNAPSHOT")
            .withExtProperty("mainClassName", "com.coditory.MainClass")
            .build()
        val properties = mapOf(
            "user.name" to "john.doe",
            "java.version" to "11.0.6",
            "java.vendor" to "AdoptOpenJDK",
            "os.arch" to "amd64",
            "os.name" to "Linux",
            "os.version" to "4.15.0-91-generic"
        )

        // when
        val manifest = withSystemProperties(properties) {
            extractManifestMap(project)
        }

        // then
        assertThat(manifest).isEqualTo(
            mapOf(
                "Built-Date" to "2015-12-03T10:15:30Z",
                "Built-Host" to "localhost",
                "Built-By" to "john.doe",
                "Built-JDK" to "11.0.6 AdoptOpenJDK",
                "Built-OS" to "Linux 4.15.0-91-generic amd64",
                "Implementation-Group" to "com.coditory",
                "Implementation-Title" to "sample-project",
                "Implementation-Version" to "0.0.1-SNAPSHOT",
                "Main-Class" to "com.coditory.MainClass",
                "Manifest-Version" to "1.0"
            )
        )
    }

    @Test
    fun `should skip empty properties`() {
        // given
        val project = projectWithPlugins("sample-project")
            .withGroup("")
            .withVersion("")
            .withExtProperty("mainClassName", "")
            .build()
        val properties = mapOf(
            "user.name" to "",
            "java.version" to "",
            "java.vendor" to "",
            "os.arch" to "",
            "os.name" to "",
            "os.version" to ""
        )

        // when
        val manifest = withSystemProperties(properties) {
            extractManifestMap(project)
        }

        // then
        assertThat(manifest).isEqualTo(
            mapOf(
                "Built-Date" to "2015-12-03T10:15:30Z",
                "Built-Host" to "localhost",
                "Implementation-Title" to "sample-project",
                "Manifest-Version" to "1.0"
            )
        )
    }

    @Test
    fun `should format partially empty properties`() {
        // given
        val project = projectWithPlugins("sample-project")
            .build()
        val properties = mapOf(
            "java.version" to "11.0.6",
            "java.vendor" to "",
            "os.arch" to "",
            "os.name" to "Linux",
            "os.version" to ""
        )

        // when
        val manifest = withSystemProperties(properties) {
            extractManifestMap(project)
        }

        // then
        assertThat(manifest)
            .containsEntry("Built-OS", "Linux")
            .containsEntry("Built-JDK", "11.0.6")
    }

    @Test
    fun `should generate attributes from git metadata`() {
        // given
        val project = projectWithPlugins("sample-project")
            .withGroup("com.coditory")
            .withVersion("0.0.1-SNAPSHOT")
            .build()
        val repo = repository(project)
            .withRemote()
            .withCommit()

        // when
        val scmManifest = extractManifestMap(project)
            .filter { it.key.startsWith("SCM-") }
            .toMap()

        // then
        assertThat(scmManifest).isEqualTo(
            mapOf(
                "SCM-Branch" to "refs/heads/master",
                "SCM-Commit-Message" to "Very important commit",
                "SCM-Commit-Author" to "John Doe <john.doe@acme.com>",
                "SCM-Commit-Date" to "2020-03-24T19:46:03Z",
                "SCM-Commit-Hash" to repo.getLastCommit().name(), // long hash
                "SCM-Repository" to "git@github.com:pmendelski/gradle-manifest-plugin.git"
            )
        )
    }

    @Test
    fun `should skip git attributes when nothing was committed`() {
        // given
        val project = projectWithPlugins("sample-project")
            .withGroup("com.coditory")
            .withVersion("0.0.1-SNAPSHOT")
            .build()
        repository(project)

        // when
        val scmManifest = extractManifestMap(project)
            .filter { it.key.startsWith("SCM-") }
            .toMap()

        // then
        assertThat(scmManifest).isEmpty()
    }

    @Test
    fun `should allow overwriting manifest attributes by user`() {
        // given
        val project = projectWithPlugins("sample-project")
            .build()

        // when
        extractManifestAttributes(project).let {
            it.put("Built-Host", "host-1024")
        }

        // then
        val manifest = extractManifestMap(project)
        assertThat(manifest["Built-Host"])
            .isEqualTo("host-1024")
    }

    @Test
    fun `should use latest defined manifest attributes`() {
        // given
        val project = projectWithPlugins("sample-project")
            .withGroup("com.coditory")
            .withVersion("0.0.1-SNAPSHOT")
            .withExtProperty("mainClassName", "com.coditory.MainClass")
            .build()

        // when
        project.version = "1.0.0-new-version"
        project.group = "new.group.name"
        project.extensions.extraProperties["mainClassName"] = "com.acme.NewClassName"

        // then
        val manifest = extractManifestMap(project)
        assertThat(manifest["Main-Class"]).isEqualTo("com.acme.NewClassName")
        assertThat(manifest["Implementation-Group"]).isEqualTo("new.group.name")
        assertThat(manifest["Implementation-Version"]).isEqualTo("1.0.0-new-version")
    }
}
