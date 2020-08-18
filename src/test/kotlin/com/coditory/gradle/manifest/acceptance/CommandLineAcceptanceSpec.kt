package com.coditory.gradle.manifest.acceptance

import com.coditory.gradle.manifest.base.SpecProjectBuilder
import com.coditory.gradle.manifest.base.SpecProjectRunner.runGradle
import com.coditory.gradle.manifest.base.SpecRepository.Companion.COMMIT_MESSAGE
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

class CommandLineAcceptanceSpec {
    private val project = SpecProjectBuilder.project("sample-project")
        .withBuildGradle(
            """
            plugins {
                id 'java'
                id 'application'
                id 'com.coditory.manifest'
            }

            group = 'com.coditory'
            version = '0.0.1-SNAPSHOT'

            repositories {
                jcenter()
            }

            application {
                mainClassName = 'com.coditory.Application'
            }
            """.trimIndent()
        )
        .withFile(
            "src/main/java/com/coditory/Application.java",
            """
            package com.coditory;

            public class Application {
                public static void main(String[] args) {
                    System.out.println(">>> Application.main");
                }
            }
            """.trimIndent()
        )
        .withGitRepository()
        .build()

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

    @AfterEach
    fun removeProjectDir() {
        SpecProjectBuilder.removeProjectDirs()
    }

    @ParameterizedTest(name = "should generate manifest on processResources command for gradle {0}")
    @ValueSource(strings = ["current", "5.0"])
    fun `should generate manifest on processResources command`(gradleVersion: String?) {
        val result = runGradle(project, listOf("processResources"), gradleVersion)
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(file("build/resources/main/META-INF/MANIFEST.MF").readText())
            .contains(expectedManifestKeys)
            .contains("Implementation-Title: sample-project")
            .contains("Implementation-Group: com.coditory")
            .contains("Implementation-Version: 0.0.1-SNAPSHOT")
            .contains("SCM-Commit-Message: $COMMIT_MESSAGE")
            .contains("Main-Class: com.coditory.Application")
    }

    @Test
    fun `should generate manifest in output directory on manifest command`() {
        val result = runGradle(project, listOf("manifest"))
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(file("build/resources/main/META-INF/MANIFEST.MF").exists())
            .isTrue()
    }

    @Test
    fun `should print out manifest on manifest command with --print flag`() {
        val result = runGradle(project, listOf("manifest", "--print"))
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output)
            .contains(expectedManifestKeys)
    }

    private fun file(path: String): File {
        return project.projectDir.resolve(path)
            .toPath()
            .toFile()
    }
}
