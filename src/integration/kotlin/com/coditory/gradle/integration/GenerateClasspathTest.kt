package com.coditory.gradle.integration

import com.coditory.gradle.manifest.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.manifest.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.manifest.base.TestProject
import com.coditory.gradle.manifest.base.TestProjectBuilder
import com.coditory.gradle.manifest.base.Versions
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.regex.Pattern.DOTALL

class GenerateClasspathTest {
    @ParameterizedTest(name = "should generate manifest with classpath for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should generate classpath`(gradleVersion: String?) {
        // given
        val project = TestProjectBuilder
            .project("project-01-" + CommandLineAcceptanceTest::class.simpleName)
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

                manifest {
                    classpathPrefix = "my/jars"
                }

                dependencies {
                    implementation("com.github.slugify:slugify:${Versions.slugify}")
                    runtimeOnly("org.hashids:hashids:${Versions.hashids}")
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
        val manifest = project.readFile("build/resources/main/META-INF/MANIFEST.MF")
        assertThat(manifest.replace("\r\n ", ""))
            .contains("Class-Path: my/jars/slugify-${Versions.slugify}.jar my/jars/hashids-${Versions.hashids}.jar my/jars/icu4j-64.2.jar\r\n")
            .matches(".*Class-Path: [^:]+(Built-JDK: [^:]+)?$".toPattern(DOTALL))
    }

    @Test
    fun `should create classpath with unix file separators from windows classpathPrefix`() {
        // given
        val project = TestProjectBuilder
            .project("project-02-" + CommandLineAcceptanceTest::class.simpleName)
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

                manifest {
                    classpathPrefix = "my\\important\\jars\\"
                }

                dependencies {
                    implementation("com.github.slugify:slugify:${Versions.slugify}")
                }
            """,
            )
            .build()
        deferCleanUp(project)

        // when
        val result = project.runGradle(listOf("processResources"))

        // then
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)

        // and
        val manifest = project.readFile("build/resources/main/META-INF/MANIFEST.MF")
        assertThat(manifest.replace("\r\n ", ""))
            .contains("my/important/jars/slugify-${Versions.slugify}.jar")
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
