package com.goodtrendltd.HolySongs

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayLyricLaunchGuardsTest {
    @Test
    fun repeatedShareTriggersAllowOnlyOneOutboundEffectUntilFailureResets() {
        val guards = DisplayLyricLaunchGuards()

        assertTrue(guards.tryStartShare())
        assertFalse(guards.tryStartShare())

        // The chooser failed or had no recipient; a later user action is allowed.
        guards.resetShare()
        assertTrue(guards.tryStartShare())
    }

    @Test
    fun repeatedVideoTriggersAllowOnlyOneOutboundEffectUntilCancellationResets() {
        val guards = DisplayLyricLaunchGuards()

        assertTrue(guards.tryStartVideo())
        assertFalse(guards.tryStartVideo())

        // Mobile-data confirmation was cancelled or no network was available.
        guards.resetVideo()
        assertTrue(guards.tryStartVideo())
    }

    @Test
    fun returningFromAnOutboundEffectResetsBothIndependentGuards() {
        val guards = DisplayLyricLaunchGuards()
        assertTrue(guards.tryStartShare())
        assertTrue(guards.tryStartVideo())

        guards.resetOnReturn()

        assertTrue(guards.tryStartShare())
        assertTrue(guards.tryStartVideo())
    }
}
