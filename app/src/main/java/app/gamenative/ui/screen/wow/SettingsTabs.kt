package app.gamenative.ui.screen.wow

internal enum class SettingsTab { LAUNCHER, GAME, GRAPHICS }

internal enum class SettingsSection { FILES, UPDATES, DISPLAY, ACCOUNT, LSFG, COMPONENTS }

/** Sections rendered by each internal Settings tab, in display order. */
internal fun sectionsFor(tab: SettingsTab): List<SettingsSection> = when (tab) {
    SettingsTab.LAUNCHER -> listOf(SettingsSection.FILES, SettingsSection.UPDATES)
    SettingsTab.GAME -> listOf(SettingsSection.DISPLAY, SettingsSection.ACCOUNT)
    SettingsTab.GRAPHICS -> listOf(SettingsSection.LSFG, SettingsSection.COMPONENTS)
}
