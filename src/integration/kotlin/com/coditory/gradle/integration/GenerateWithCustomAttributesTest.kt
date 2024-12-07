package com.coditory.gradle.integration

import com.coditory.gradle.manifest.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.manifest.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.manifest.base.TestProject
import com.coditory.gradle.manifest.base.TestProjectBuilder
import com.coditory.gradle.manifest.base.Versions
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.collections.forEach

class GenerateWithCustomAttributesTest {
    @ParameterizedTest(name = "should generate manifest with custom attributes for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should generate manifest with custom attributes`(gradleVersion: String?) {
        // given
        val project = TestProjectBuilder
            .project("project-" + GenerateWithCustomAttributesTest::class.simpleName)
            .withBuildGradleKts(
                """
                plugins {
                    id("java")
                    id("com.coditory.manifest")
                }

                repositories {
                    mavenCentral()
                }
                
                group = "com.coditory"
                version = "0.0.1-SNAPSHOT"

                manifest {
                    buildAttributes = false
                    implementationAttributes = true
                    scmAttributes = false
                    attributes = mapOf<String, Any>(
                        "Custom-1" to "Custom-Value",
                        "Custom-2" to 123,
                    )
                }

                dependencies {
                    compileOnly("org.springframework.boot:spring-boot-starter:${Versions.spring}")
                    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
                }
            """,
            )
            .build()
        deferCleanUp(project)

        // when
        val result = project.runGradle(listOf("processResources"), gradleVersion)

        // then
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)

        // and
        assertThat(project.readFile("build/resources/main/META-INF/MANIFEST.MF"))
            .contains("Implementation-Title: ${project.name}")
            .contains("Implementation-Group: com.coditory")
            .contains("Implementation-Version: 0.0.1-SNAPSHOT")
            .contains("Custom-1: Custom-Value")
            .contains("Custom-2: 123")
            .doesNotContain("Main-Class")
            .doesNotContain("Built-")
            .doesNotContain("SCM-")
    }

    companion object {
        private val projects: MutableList<TestProject> = mutableListOf()

        @Synchronized
        fun deferCleanUp(project: TestProject) {
            projects.add(project)
        }

        @AfterAll
        @JvmStatic
        fun cleanUp() {
            projects.forEach { it.close() }
        }
    }
}
