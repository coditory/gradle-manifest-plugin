package com.coditory.gradle.manifest

open class ManifestPluginExtension {
    var classpathPrefix: String? = null
    var attributes: Map<String, Any?>? = null
    var implementationAttributes: Boolean = true
    var buildAttributes: Boolean = true
    var scmAttributes: Boolean = true
}
