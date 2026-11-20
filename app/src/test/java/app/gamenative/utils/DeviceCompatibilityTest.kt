package app.gamenative.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceCompatibilityTest {

    @Test
    fun parseGpuModel_readsAdrenoNumber() {
        assertEquals(740, DeviceCompatibility.parseGpuModel("Adreno (TM) 740"))
        assertEquals(830, DeviceCompatibility.parseGpuModel("adreno 830"))
        assertNull(DeviceCompatibility.parseGpuModel("Mali-G720"))
        assertNull(DeviceCompatibility.parseGpuModel(null))
    }

    @Test
    fun resolve_validatedGpusUseMatchingDriver() {
        val a740 = DeviceCompatibility.resolve("Adreno (TM) 740")
        assertEquals(DeviceCompatibility.DRIVER_740, a740.driverId)
        assertEquals(DeviceStatus.VALIDATED, a740.status)

        val a840 = DeviceCompatibility.resolve("Adreno (TM) 840")
        assertEquals(DeviceCompatibility.DRIVER_8XX, a840.driverId)
        assertEquals(DeviceStatus.VALIDATED, a840.status)
    }

    @Test
    fun resolve_pendingGpusUseClosestDriverButAreFlagged() {
        val a750 = DeviceCompatibility.resolve("Adreno (TM) 750")
        assertEquals(DeviceCompatibility.DRIVER_740, a750.driverId)
        assertEquals(DeviceStatus.PENDING_VALIDATION, a750.status)

        val a825 = DeviceCompatibility.resolve("Adreno (TM) 825")
        assertEquals(DeviceCompatibility.DRIVER_8XX, a825.driverId)
        assertEquals(DeviceStatus.PENDING_VALIDATION, a825.status)
    }

    @Test
    fun resolve_unknownGpuWarnsButStillSelectsFallback() {
        val result = DeviceCompatibility.resolve("Mali-G720")
        assertEquals(DeviceCompatibility.DRIVER_740, result.driverId)
        assertEquals(DeviceStatus.UNKNOWN, result.status)
    }

    @Test
    fun resolve_forcedDriverOverridesDetection() {
        val result = DeviceCompatibility.resolve("Adreno (TM) 740", forcedDriver = "8xx")
        assertEquals(DeviceCompatibility.DRIVER_8XX, result.driverId)
        assertTrue(result.forced)
        assertEquals(DeviceStatus.VALIDATED, result.status)

        val invalid = DeviceCompatibility.resolve("Adreno (TM) 740", forcedDriver = "nonsense")
        assertEquals(DeviceCompatibility.DRIVER_740, invalid.driverId)
        assertTrue(!invalid.forced)
    }

    @Test
    fun resolve_sysfsModelUsedWhenRendererIsMissing() {
        val result = DeviceCompatibility.resolve(null, sysfsModel = "Adreno (TM) 830")
        assertEquals(DeviceCompatibility.DRIVER_8XX, result.driverId)
        assertEquals(DeviceStatus.VALIDATED, result.status)
    }
}
