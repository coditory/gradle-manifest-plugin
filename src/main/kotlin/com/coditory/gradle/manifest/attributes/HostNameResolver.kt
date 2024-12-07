package com.coditory.gradle.manifest.attributes

import java.net.InetAddress

interface HostNameResolver {
    fun resolveHostName(): String

    companion object {
        val INET_HOST_NAME_RESOLVER = object : HostNameResolver {
            override fun resolveHostName(): String {
                return InetAddress.getLocalHost().hostName
            }
        }
    }
}
