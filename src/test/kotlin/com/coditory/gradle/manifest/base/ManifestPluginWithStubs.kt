package com.coditory.gradle.manifest.base

import com.coditory.gradle.manifest.HostNameResolver
import com.coditory.gradle.manifest.ParameterizedManifestPlugin

class ManifestPluginWithStubs : ParameterizedManifestPlugin(clock, hostNameProvider) {
    companion object {
        val clock: UpdatableFixedClock = UpdatableFixedClock()
        val hostNameProvider = object : HostNameResolver {
            override fun resolveHostName(): String {
                return "localhost"
            }
        }
    }
}
