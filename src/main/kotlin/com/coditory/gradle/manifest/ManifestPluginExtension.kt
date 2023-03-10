package com.coditory.gradle.manifest

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

interface ManifestPluginExtension {
    val classpathPrefix: Property<String>
    val attributes: MapProperty<String, Any?>
    val implementationAttributes: Property<Boolean>
    val buildAttributes: Property<Boolean>
    val scmAttributes: Property<Boolean>
}
