import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
    id("jacoco")
    id("com.github.kt3k.coveralls") version "2.10.2"
    id("com.gradle.plugin-publish") version "0.12.0"
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

repositories {
    jcenter()
}

ktlint {
    version.set("0.39.0")
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.9.0.202009080501-r")

    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

group = "com.coditory.gradle"

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
        xml.isEnabled = true
        html.isEnabled = true
    }
}

coveralls {
    sourceDirs = listOf("src/main/kotlin")
}

gradlePlugin {
    plugins {
        create("manifestPlugin") {
            id = "com.coditory.manifest"
            implementationClass = "com.coditory.gradle.manifest.ManifestPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/coditory/gradle-manifest-plugin"
    vcsUrl = "https://github.com/coditory/gradle-manifest-plugin"
    description = "Writes project metadata to META-INF/MANIFEST.MF"
    tags = listOf("project manifest plugin", "manifest")

    (plugins) {
        "manifestPlugin" {
            displayName = "Manifest plugin"
        }
    }
}
