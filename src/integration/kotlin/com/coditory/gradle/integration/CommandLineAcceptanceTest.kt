package com.coditory.gradle.integration

import com.coditory.gradle.manifest.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.manifest.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.manifest.base.TestProjectBuilder
import com.coditory.gradle.manifest.base.TestRepository.Companion.COMMIT_MESSAGE
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CommandLineAcceptanceTest {
    companion object {
        @AutoClose
        private val project = TestProjectBuilder
            .project("project-" + CommandLineAcceptanceTest::class.simpleName)
            .withBuildGradleKts(
                """
                plugins {
                    id("java")
                    id("application")
                    id("com.coditory.manifest")
                }
    
                group = "com.coditory"
                version = "0.0.1-SNAPSHOT"
    
                repositories {
                    mavenCentral()
                }
    
                application {
                    mainClass.set("com.coditory.Application")
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
    }

    @AfterEach
    fun cleanProject() {
        project.clean()
    }

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

    @ParameterizedTest(name = "should generate manifest on processResources command for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should generate manifest on processResources command`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("processResources"), gradleVersion)
        // then
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        // and
        assertThat(project.readFile("build/resources/main/META-INF/MANIFEST.MF"))
            .contains(expectedManifestKeys)
            .contains("Implementation-Title: ${project.name}")
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

    @Test
    fun `should work with build cache`() {
        // when
        val result = project.runGradle(listOf("manifest", "--build-cache"))
        // then
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        // and
        assertThat(project.readFile("build/resources/main/META-INF/MANIFEST.MF"))
            .isNotEmpty()
    }

    @Test
    fun `should work with configuration cache`() {
        // when
        val result = project.runGradle(listOf("manifest", "--configuration-cache"))
        // then
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        // and
        assertThat(project.readFile("build/resources/main/META-INF/MANIFEST.MF"))
            .isNotEmpty()
    }
}
