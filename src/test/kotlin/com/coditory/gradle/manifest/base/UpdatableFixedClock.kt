package com.coditory.gradle.manifest.base

import java.time.Clock
import java.time.Duration
import java.time.Duration.ofDays
import java.time.Duration.ofNanos
import java.time.Instant
import java.time.ZoneId

class UpdatableFixedClock(
    private var fixedTime: Instant = DEFAULT_FIXED_TIME,
    private var zoneId: ZoneId = DEFAULT_ZONE_ID,
) : Clock() {

    override fun getZone(): ZoneId {
        return zoneId
    }

    override fun withZone(zone: ZoneId): UpdatableFixedClock {
        return UpdatableFixedClock(this.fixedTime, zoneId)
    }

    override fun instant(): Instant {
        return fixedTime
    }

    fun futureInstant(duration: Duration = ofDays(1)): Instant {
        return this.fixedTime + duration
    }

    fun pastInstant(duration: Duration = ofDays(1)): Instant {
        return this.fixedTime - duration
    }

    fun reset() {
        this.fixedTime = DEFAULT_FIXED_TIME
    }

    fun tick(duration: Duration = ofNanos(1)) {
        this.fixedTime = this.fixedTime + duration
    }

    fun setup(instant: Instant) {
        this.fixedTime = instant
    }

    companion object {
        private val DEFAULT_FIXED_TIME = Instant.parse("2015-12-03T10:15:30.123456Z")
        private val DEFAULT_ZONE_ID = ZoneId.of("Europe/Warsaw")
    }
}
