package com.coditory.gradle.manifest.acceptance

import com.coditory.gradle.manifest.base.TestProjectBuilder
import com.coditory.gradle.manifest.base.TestRepository.Companion.COMMIT_MESSAGE
import com.coditory.gradle.manifest.base.readFile
import com.coditory.gradle.manifest.base.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class MultiModuleAcceptanceTest {
    private val parentProject = TestProjectBuilder.project("parent-project")
        .withBuildGradle(
            """
            plugins {
                id 'com.coditory.manifest' apply false
            }

            allprojects {
                group = 'com.coditory.sandbox'
                version = '0.0.1-SNAPSHOT'
            }

            subprojects {
                apply plugin: 'com.coditory.manifest'
            }
            """.trimIndent()
        )
        .withFile(
            "settings.gradle",
            """
            rootProject.name = 'parent-project'

            include 'project-a'
            include 'project-b'
            """.trimIndent()
        )
        .withGitRepository()
        .build()

    private val projectA = TestProjectBuilder.project("project-a", parentProject.projectDir.resolve("project-a"))
        .withBuildGradle(
            """
            plugins {
                id 'java'
                id 'application'
            }

            repositories {
                jcenter()
            }

            application {
                mainClassName = 'com.coditory.ProjectA'
            }
            """.trimIndent()
        )
        .withFile(
            "src/main/java/com/coditory/ProjectA.java",
            """
            package com.coditory;

            public class ProjectA {
                public static void main(String[] args) {
                    System.out.println(">>> ProjectA");
                }
            }
            """.trimIndent()
        )
        .build()

    private val projectB = TestProjectBuilder.project("project-b", parentProject.projectDir.resolve("project-b"))
        .withBuildGradle(
            """
            plugins {
                id 'java'
                id 'application'
            }

            repositories {
                jcenter()
            }

            application {
                mainClassName = 'com.coditory.ProjectB'
            }
            """.trimIndent()
        )
        .withFile(
            "src/main/java/com/coditory/ProjectB.java",
            """
            package com.coditory;

            public class ProjectB {
                public static void main(String[] args) {
                    System.out.println(">>> ProjectB");
                }
            }
            """.trimIndent()
        )
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
        TestProjectBuilder.removeProjectDirs()
    }

    @Test
    fun `should generate manifest on processResources command in sub project`() {
        // when
        parentProject.runGradle(listOf("processResources"))

        // then
        assertThat(projectA.readFile("build/resources/main/META-INF/MANIFEST.MF"))
            .contains(expectedManifestKeys)
            .contains("Implementation-Title: project-a")
            .contains("Implementation-Group: com.coditory")
            .contains("Implementation-Version: 0.0.1-SNAPSHOT")
            .contains("Main-Class: com.coditory.ProjectA")
            .contains("SCM-Commit-Message: $COMMIT_MESSAGE")

        // and
        assertThat(projectB.readFile("build/resources/main/META-INF/MANIFEST.MF"))
            .contains(expectedManifestKeys)
            .contains("Implementation-Title: project-b")
            .contains("Implementation-Group: com.coditory")
            .contains("Implementation-Version: 0.0.1-SNAPSHOT")
            .contains("Main-Class: com.coditory.ProjectB")
            .contains("SCM-Commit-Message: $COMMIT_MESSAGE")
    }
}
