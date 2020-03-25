package com.coditory.gradle.manifest.base

import java.util.Properties

object SystemProperties {
    fun <T> withSystemProperties(properties: Map<String, String>, call: () -> T): T {
        val backup = toMap(System.getProperties())
        setSystemProperties(properties)
        try {
            return call()
        } finally {
            setSystemProperties(backup)
        }
    }

    fun <T> withoutSystemProperties(call: () -> T): T {
        val backup = toMap(System.getProperties())
        System.getProperties().clear()
        try {
            return call()
        } finally {
            setSystemProperties(backup)
        }
    }

    private fun toMap(properties: Properties): Map<String, String> {
        return properties.toMap()
            .map { it.key.toString() to it.value.toString() }
            .toMap()
    }

    private fun setSystemProperties(map: Map<String, String>) {
        map.forEach {
            System.getProperties().setProperty(it.key, it.value)
        }
    }
}
