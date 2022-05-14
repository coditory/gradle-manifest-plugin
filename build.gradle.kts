import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    id("jacoco")
    id("com.github.kt3k.coveralls") version "2.12.0"
    id("com.gradle.plugin-publish") version "1.0.0-rc-2"
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

repositories {
    mavenCentral()
}

ktlint {
    version.set("0.45.2")
}

dependencies {
    implementation(gradleApi())
    // implementation(kotlin("stdlib-jdk8"))
    // implementation(kotlin("reflect"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.9.0.202009080501-r")

    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

group = "com.coditory.gradle"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        setExceptionFormat("full")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        allWarningsAsErrors = true
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

coveralls {
    sourceDirs = listOf("src/main/kotlin")
}

// Marking new version (incrementPatch [default], incrementMinor, incrementMajor)
// ./gradlew markNextVersion -Prelease.incrementer=incrementMinor
// Releasing the plugin:
// ./gradlew release && ./gradlew publishPlugins
gradlePlugin {
    plugins {
        create("manifestPlugin") {
            id = "com.coditory.manifest"
            implementationClass = "com.coditory.gradle.manifest.ManifestPlugin"
            displayName = "Manifest plugin"
            description = "Writes project metadata to META-INF/MANIFEST.MF"
        }
    }
}

pluginBundle {
    website = "https://github.com/coditory/gradle-manifest-plugin"
    vcsUrl = "https://github.com/coditory/gradle-manifest-plugin"
    tags = listOf("project manifest plugin", "manifest")
}
