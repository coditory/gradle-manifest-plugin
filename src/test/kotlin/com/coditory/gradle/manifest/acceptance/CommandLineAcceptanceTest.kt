package com.coditory.gradle.manifest.acceptance

import com.coditory.gradle.manifest.base.TestProjectBuilder
import com.coditory.gradle.manifest.base.TestRepository.Companion.COMMIT_MESSAGE
import com.coditory.gradle.manifest.base.readFile
import com.coditory.gradle.manifest.base.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CommandLineAcceptanceTest {
    private val project = TestProjectBuilder.project("sample-project")
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
            """.trimIndent(),
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
            """.trimIndent(),
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
        "SCM-Commit-Date:",
    )

    @AfterEach
    fun removeProjectDir() {
        TestProjectBuilder.removeProjectDirs()
    }

    @ParameterizedTest(name = "should generate manifest on processResources command for gradle {0}")
    @ValueSource(strings = ["current", "7.6.4"])
    fun `should generate manifest on processResources command`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("processResources"), gradleVersion)
        // then
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        // and
        assertThat(project.readFile("build/resources/main/META-INF/MANIFEST.MF"))
            .contains(expectedManifestKeys)
            .contains("Implementation-Title: sample-project")
            .contains("Implementation-Group: com.coditory")
            .contains("Implementation-Version: 0.0.1-SNAPSHOT")
            .contains("SCM-Commit-Message: $COMMIT_MESSAGE")
            .contains("Main-Class: com.coditory.Application")
    }

    @Test
    fun `should generate manifest in output directory on manifest command`() {
        // when
        val result = project.runGradle(listOf("manifest"))
        // then
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        // and
        assertThat(project.readFile("build/resources/main/META-INF/MANIFEST.MF"))
            .isNotEmpty()
    }

    @Test
    fun `should print out manifest on manifest command with --print flag`() {
        // when
        val result = project.runGradle(listOf("manifest", "--print"))
        // then
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        // and
        assertThat(result.output)
            .contains(expectedManifestKeys)
    }
}
