package app.gamenative.ui.screen.wow

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsTabsTest {

    @Test
    fun `launcher tab contains files and updates`() {
        assertEquals(
            listOf(SettingsSection.FILES, SettingsSection.UPDATES),
            sectionsFor(SettingsTab.LAUNCHER),
        )
    }

    @Test
    fun `game tab contains display and account`() {
        assertEquals(
            listOf(SettingsSection.DISPLAY, SettingsSection.ACCOUNT),
            sectionsFor(SettingsTab.GAME),
        )
    }

    @Test
    fun `graphics tab contains lsfg and components`() {
        assertEquals(
            listOf(SettingsSection.LSFG, SettingsSection.COMPONENTS),
            sectionsFor(SettingsTab.GRAPHICS),
        )
    }
}
