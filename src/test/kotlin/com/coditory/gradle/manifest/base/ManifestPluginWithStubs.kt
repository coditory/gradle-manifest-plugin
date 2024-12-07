package com.coditory.gradle.manifest.base

import com.coditory.gradle.manifest.ManifestParameterizedPlugin
import com.coditory.gradle.manifest.attributes.HostNameResolver

class ManifestPluginWithStubs : ManifestParameterizedPlugin(clock, hostNameProvider) {
    companion object {
        val clock: UpdatableFixedClock = UpdatableFixedClock()
        val hostNameProvider = object : HostNameResolver {
            override fun resolveHostName(): String {
                return "localhost"
            }
        }
    }
}
