package com.coditory.gradle.manifest.acceptance

import com.coditory.gradle.manifest.base.SpecProjectBuilder
import com.coditory.gradle.manifest.base.SpecProjectBuilder.Companion.projectWithPlugins
import com.coditory.gradle.manifest.base.readFile
import com.coditory.gradle.manifest.base.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.regex.Pattern.DOTALL

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
                    classpathPrefix = "my/jars"
                }

                dependencies {
                    compile 'org.springframework.boot:spring-boot-starter:2.4.2'
                    implementation 'com.github.slugify:slugify:2.4'
                    runtime 'org.hashids:hashids:1.0.3'
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
        val manifest = project.readFile("build/resources/main/META-INF/MANIFEST.MF")
        assertThat(manifest.replace("\r\n ", ""))
            .contains("my/jars/slugify-2.4.jar")
            .contains("my/jars/hashids-1.0.3.jar")
            .contains("my/jars/spring-boot-starter-2.4.2.jar")

        // and:
        assertThat(manifest)
            .contains(
                "Class-Path: my/jars/spring-boot-starter-2.4.2.jar my/jars/slugify-2.4.\r\n" +
                    " jar my/jars/hashids-1.0.3.jar my/jars/spring-boot-autoconfigure-2.4.2\r\n" +
                    " .jar my/jars/spring-boot-2.4.2.jar my/jars/spring-boot-starter-loggin\r\n" +
                    " g-2.4.2.jar my/jars/jakarta.annotation-api-1.3.5.jar my/jars/spring-c\r\n" +
                    " ontext-5.3.3.jar my/jars/spring-aop-5.3.3.jar my/jars/spring-beans-5.\r\n" +
                    " 3.3.jar my/jars/spring-expression-5.3.3.jar my/jars/spring-core-5.3.3\r\n" +
                    " .jar my/jars/snakeyaml-1.27.jar my/jars/icu4j-64.2.jar my/jars/logbac\r\n" +
                    " k-classic-1.2.3.jar my/jars/log4j-to-slf4j-2.13.3.jar my/jars/jul-to-\r\n" +
                    " slf4j-1.7.30.jar my/jars/spring-jcl-5.3.3.jar my/jars/logback-core-1.\r\n" +
                    " 2.3.jar my/jars/slf4j-api-1.7.30.jar my/jars/log4j-api-2.13.3.jar\r\n"
            )
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
                    compile 'com.github.slugify:slugify:2.4'
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
