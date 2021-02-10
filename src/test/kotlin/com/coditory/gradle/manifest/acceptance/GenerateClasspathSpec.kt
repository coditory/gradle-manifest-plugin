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

class GenerateClasspathSpec {
    @AfterEach
    fun removeProjectDir() {
        SpecProjectBuilder.removeProjectDirs()
    }

    @ParameterizedTest(name = "should generate manifest with classpath for gradle {0}")
    @ValueSource(strings = ["current", "5.0"])
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
                    classpathPrefix = "my-jars"
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
            .contains(
                "Class-Path: my-jars/spring-boot-starter-2.4.2.jar my-jars/spring-boot-au\r\n" +
                    " toconfigure-2.4.2.jar my-jars/spring-boot-2.4.2.jar my-jars/spring-boot\r\n" +
                    " -starter-logging-2.4.2.jar my-jars/jakarta.annotation-api-1.3.5.jar my-\r\n" +
                    " jars/spring-context-5.3.3.jar my-jars/spring-aop-5.3.3.jar my-jars/spri\r\n" +
                    " ng-beans-5.3.3.jar my-jars/spring-expression-5.3.3.jar my-jars/spring-c\r\n" +
                    " ore-5.3.3.jar my-jars/snakeyaml-1.27.jar my-jars/logback-classic-1.2.3.\r\n" +
                    " jar my-jars/log4j-to-slf4j-2.13.3.jar my-jars/jul-to-slf4j-1.7.30.jar m\r\n" +
                    " y-jars/spring-jcl-5.3.3.jar my-jars/logback-core-1.2.3.jar my-jars/slf4\r\n" +
                    " j-api-1.7.30.jar my-jars/log4j-api-2.13.3.jar"
            )
    }
}
