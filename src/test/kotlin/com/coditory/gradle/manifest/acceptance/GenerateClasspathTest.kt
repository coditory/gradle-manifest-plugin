package com.coditory.gradle.manifest.acceptance

import com.coditory.gradle.manifest.base.TestProjectBuilder
import com.coditory.gradle.manifest.base.TestProjectBuilder.Companion.projectWithPlugins
import com.coditory.gradle.manifest.base.readFile
import com.coditory.gradle.manifest.base.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.regex.Pattern.DOTALL

class GenerateClasspathTest {
    @AfterEach
    fun removeProjectDir() {
        TestProjectBuilder.removeProjectDirs()
    }

    @ParameterizedTest(name = "should generate manifest with classpath for gradle {0}")
    @ValueSource(strings = ["current", "6.0"])
    fun `should generate classpath`(gradleVersion: String?) {
        // given
        val project = projectWithPlugins()
            .withGroup("com.coditory")
            .withBuildGradle(
                """
                plugins {
                    id 'java'
                    id 'com.coditory.manifest'
                }

                repositories {
                    jcenter()
                }

                manifest {
                    classpathPrefix = "my/jars"
                }

                dependencies {
                    implementation 'com.github.slugify:slugify:2.4'
                    runtimeOnly 'org.hashids:hashids:1.0.3'
                    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.7.0'
                }
            """
            )
            .build()

        // when
        val result = project.runGradle(listOf("processResources"), gradleVersion)

        // then
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)

        // and
        val manifest = project.readFile("build/resources/main/META-INF/MANIFEST.MF")
        assertThat(manifest.replace("\r\n ", ""))
            .contains("Class-Path: my/jars/slugify-2.4.jar my/jars/hashids-1.0.3.jar my/jars/icu4j-64.2.jar\r\n")
            .matches(".*Class-Path: [^:]+(Built-JDK: [^:]+)?$".toPattern(DOTALL))
    }

    @Test
    fun `should create classpath with unix file separators from windows classpathPrefix`() {
        // given
        val project = projectWithPlugins()
            .withGroup("com.coditory")
            .withBuildGradle(
                """
                plugins {
                    id 'java'
                    id 'com.coditory.manifest'
                }

                repositories {
                    jcenter()
                }

                manifest {
                    classpathPrefix = "my\\important\\jars\\"
                }

                dependencies {
                    implementation 'com.github.slugify:slugify:2.4'
                }
            """
            )
            .build()

        // when
        val result = project.runGradle(listOf("processResources"))

        // then
        assertThat(result.task(":manifest")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)

        // and
        val manifest = project.readFile("build/resources/main/META-INF/MANIFEST.MF")
        assertThat(manifest.replace("\r\n ", ""))
            .contains("my/important/jars/slugify-2.4.jar")
    }
}
