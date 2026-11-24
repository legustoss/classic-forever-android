package app.gamenative.ui.screen.wow

import app.gamenative.utils.DeviceCompatibility
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsComponentGroupsTest {

    @Test
    fun `740 driver shows only 740 slot`() {
        assertEquals(listOf("turnip740"), driverSlotsFor(DeviceCompatibility.DRIVER_740))
    }

    @Test
    fun `8xx driver shows only 8xx slot`() {
        assertEquals(listOf("turnip8xx"), driverSlotsFor(DeviceCompatibility.DRIVER_8XX))
    }

    @Test
    fun `unknown driver shows both slots`() {
        assertEquals(listOf("turnip740", "turnip8xx"), driverSlotsFor("unknown"))
    }

    @Test
    fun `groups always put proton and dxvk first`() {
        assertEquals(
            listOf("proton", "dxvk", "turnip8xx"),
            componentGroupSlots(DeviceCompatibility.DRIVER_8XX),
        )
    }
}
