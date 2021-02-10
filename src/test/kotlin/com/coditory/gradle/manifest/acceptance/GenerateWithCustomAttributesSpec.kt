package com.coditory.gradle.manifest.acceptance

import com.coditory.gradle.manifest.base.SpecProjectBuilder
import com.coditory.gradle.manifest.base.SpecProjectBuilder.Companion.projectWithPlugins
import com.coditory.gradle.manifest.base.readFile
import com.coditory.gradle.manifest.base.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class GenerateWithCustomAttributesSpec {
    @AfterEach
    fun removeProjectDir() {
        SpecProjectBuilder.removeProjectDirs()
    }

    @ParameterizedTest(name = "should generate manifest with custom attributes for gradle {0}")
    @ValueSource(strings = ["current", "5.0"])
    fun `should generate manifest with custom attributes`(gradleVersion: String?) {
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

                group = 'com.coditory'
                version = '0.0.1-SNAPSHOT'

                manifest {
                    buildAttributes = false
                    implementationAttributes = true
                    scmAttributes = false
                    attributes = [
                        "Custom-1": "Custom-Value",
                        "Custom-2": 123
                    ]
                }

                dependencies {
                    compile 'org.springframework.boot:spring-boot-starter:2.4.2'
                    testCompile 'org.junit.jupiter:junit-jupiter-api:5.7.0'
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
        assertThat(project.readFile("build/resources/main/META-INF/MANIFEST.MF"))
            .contains("Implementation-Title: sample-project")
            .contains("Implementation-Group: com.coditory")
            .contains("Implementation-Version: 0.0.1-SNAPSHOT")
            .contains("Custom-1: Custom-Value")
            .contains("Custom-2: 123")
            .doesNotContain("Main-Class")
            .doesNotContain("Built-")
            .doesNotContain("SCM-")
    }
}
