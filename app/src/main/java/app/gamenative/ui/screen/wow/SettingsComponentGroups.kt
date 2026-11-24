package app.gamenative.ui.screen.wow

import app.gamenative.utils.DeviceCompatibility

/** Runtime slots shown for the detected GPU; both Turnip slots only when the GPU is unknown. */
internal fun driverSlotsFor(driver: String): List<String> = when {
    driver.equals(DeviceCompatibility.DRIVER_740, ignoreCase = true) -> listOf("turnip740")
    driver.equals(DeviceCompatibility.DRIVER_8XX, ignoreCase = true) -> listOf("turnip8xx")
    else -> listOf("turnip740", "turnip8xx")
}

/** Component groups in display order: Proton, DXVK, then the relevant GPU driver. */
internal fun componentGroupSlots(driver: String): List<String> =
    listOf("proton", "dxvk") + driverSlotsFor(driver)

/** Only one group stays expanded at a time; tapping the open one collapses it. */
internal fun toggleExpandedGroup(current: String?, slot: String): String? =
    if (current == slot) null else slot
