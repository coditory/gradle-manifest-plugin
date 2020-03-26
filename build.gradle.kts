import pl.allegro.tech.build.axion.release.domain.hooks.HookContext
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig

plugins {
    kotlin("jvm") version "1.3.70"
    id("jacoco")
    id("pl.allegro.tech.build.axion-release") version "1.11.0"
    id("com.github.kt3k.coveralls") version "2.10.1"
    id("com.gradle.plugin-publish") version "0.10.1"
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

repositories {
    jcenter()
}

ktlint {
    version.set("0.36.0")
    enableExperimentalRules.set(true)
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.7.0.202003110725-r")

    testImplementation("org.assertj:assertj-core:3.15.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

scmVersion {
    versionCreator("versionWithBranch")
    hooks = HooksConfig().also {
        it.pre(
            "fileUpdate",
            mapOf(
                "files" to listOf("readme.md"),
                "pattern" to KotlinClosure2({ v: String, _: HookContext -> v }),
                "replacement" to KotlinClosure2({ v: String, _: HookContext -> v })
            )
        )
        it.pre("commit")
    }
}

group = "com.coditory.gradle"
version = scmVersion.version

tasks {
    withType<Test> {
        testLogging {
            events("passed", "failed", "skipped")
            setExceptionFormat("full")
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }
    jacocoTestReport {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }
    coveralls {
        sourceDirs = listOf("src/main/kotlin")
    }
}

gradlePlugin {
    plugins {
        create("manifestPlugin") {
            id = "com.coditory.manifest"
            implementationClass = "com.coditory.gradle.manifest.ManifestPlugin"
        }
    }
}

// Marking new version (incrementPatch [default], incrementMinor, incrementMajor)
// ./gradlew markNextVersion -Prelease.incrementer=incrementMinor
// Releasing the plugin:
// ./gradlew release && ./gradlew publishPlugins
pluginBundle {
    website = "https://github.com/coditory/gradle-manifest-plugin"
    vcsUrl = "https://github.com/coditory/gradle-manifest-plugin"
    description = "Writes project metadata to META-INF/MANIFEST.MF"
    tags = listOf("project manifest plugin", "metadata manifest")

    (plugins) {
        "manifestPlugin" {
            displayName = "Manifest plugin"
        }
    }
}
