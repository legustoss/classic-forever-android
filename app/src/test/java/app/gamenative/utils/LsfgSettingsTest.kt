package app.gamenative.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class LsfgSettingsTest {

    @Test
    fun defaultFlowScaleIsHalf() {
        assertEquals(0.50f, LsfgQuickMenuHelper.DEFAULT_FLOW_SCALE)
        assertEquals(0.50f, LsfgQuickMenuHelper.flowScaleOrDefault(null))
        assertEquals(0.50f, LsfgQuickMenuHelper.flowScaleOrDefault(""))
        assertEquals(0.50f, LsfgQuickMenuHelper.flowScaleOrDefault("not-a-number"))
    }

    @Test
    fun storedFlowScaleIsSanitized() {
        assertEquals(0.85f, LsfgQuickMenuHelper.flowScaleOrDefault("0.85"))
        assertEquals(0.25f, LsfgQuickMenuHelper.flowScaleOrDefault("0.10"))
        assertEquals(1.0f, LsfgQuickMenuHelper.flowScaleOrDefault("2.0"))
    }

    @Test
    fun sanitizeFlowScaleClampsToRange() {
        assertEquals(0.25f, LsfgQuickMenuHelper.sanitizeFlowScale(0.1f))
        assertEquals(0.25f, LsfgQuickMenuHelper.sanitizeFlowScale(0.25f))
        assertEquals(1.0f, LsfgQuickMenuHelper.sanitizeFlowScale(1.0f))
        assertEquals(1.0f, LsfgQuickMenuHelper.sanitizeFlowScale(1.2f))
    }

    @Test
    fun sanitizeFlowScaleDoesNotRoundInRange() {
        assertEquals(0.67f, LsfgQuickMenuHelper.sanitizeFlowScale(0.67f))
        assertEquals(0.505f, LsfgQuickMenuHelper.sanitizeFlowScale(0.505f))
    }
}
