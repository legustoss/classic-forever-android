package app.gamenative.ui.theme

import androidx.compose.ui.graphics.Color
import com.alorma.compose.settings.ui.base.internal.SettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults

// Your custom color scheme
val customBackground = Color(0xFF000000)
val customForeground = Color(0xFFFAFAFA)
val customCard = Color(0xFF000000)
val customCardForeground = Color(0xFFFAFAFA)
val customPrimary = Color(0xFFFFFFFF)
val customPrimaryForeground = Color(0xFFFAFAFA)
val customSecondary = Color(0xFF272727)
val customSecondaryForeground = Color(0xFFFAFAFA)
val customMuted = Color(0xFF272727)
val customMutedForeground = Color(0xFF94969C)
val customAccent = Color(0xFFFFFFFF)
val customAccentForeground = Color(0xFFFAFAFA)
val customDestructive = Color(0xFF7F1D1D)

val pluviaSeedColor = Color(0xFFFFFFFF)

/**
 * Raw color primitives for the Pluvia app.
 * These are the base colors used to construct theme palettes.
 */

// Brand
val PluviaPrimary = Color(0xFFFFFFFF)
val PluviaSeed = Color(0xFFFFFFFF)

// Backgrounds
val PluviaBackground = Color(0xFF000000)
val PluviaSurface = Color(0xFF111111)
val PluviaSurfaceElevated = Color(0xFF1A1A1A)
val PluviaCard = Color(0xFF000000)

// Foregrounds
val PluviaForeground = Color(0xFFFAFAFA)
val PluviaForegroundMuted = Color(0xFF94969C)

// Secondary
val PluviaSecondary = Color(0xFF272727)

// Accents — monochrome neon (names kept for source compatibility)
val PluviaCyan = Color(0xFFFFFFFF)
val PluviaPurple = Color(0xFFD4D4D4)
val PluviaPink = Color(0xFFA3A3A3)

// Semantic
val PluviaSuccess = Color(0xFF10B981)
val PluviaWarning = Color(0xFFF59E0B)
val PluviaDanger = Color(0xFFEF4444)
val PluviaDestructive = Color(0xFF7F1D1D)

// Border
val PluviaBorder = Color(0xFF3A3A3A)

// Status - Installed/Download states
val StatusInstalled = Color(0xFF4CAF50)
val StatusDownloading = Color(0xFF00BCD4)
val StatusAvailable = Color(0xFF2196F3)
val StatusAway = Color(0xFFFF9800)
val StatusOffline = Color(0xFF9E9E9E)

// Friend states
val FriendOnline = Color(0xFF6DCFF6)
val FriendOffline = Color(0xFF7A7A7A)
val FriendInGame = Color(0xFF90BA3C)
val FriendAwayOrSnooze = Color(0x806DCFF6)
val FriendInGameAwayOrSnooze = Color(0x8090BA3C)
val FriendBlocked = Color(0xFF983D3D)

// WoW Forever — monochrome neon: white light on black, no other hues.
val WowBackgroundGradient = listOf(Color(0xFF000000), Color(0xFF0B0B0B), Color(0xFF000000))
val WowNeon = Color(0xFFFFFFFF)
val WowNeonDim = Color(0xFFB4B4B4)
val WowSubtle = Color(0xFF7A7A7A)
val WowPanel = Color(0xFF0D0D0D)
val WowPanelElevated = Color(0xFF171717)
val WowBorder = Color(0xFF3A3A3A)
val WowNeonGradient = listOf(Color(0xFF7A7A7A), Color(0xFFFFFFFF), Color(0xFF9A9A9A))

// Compatibility
val CompatibilityGood = Color(0xFF4CAF50)
val CompatibilityGoodBg = Color(0xFF1B5E20)
val CompatibilityPartial = Color(0xFF8BC34A)
val CompatibilityPartialBg = Color(0xFF33691E)
val CompatibilityUnknown = Color(0xFF9E9E9E)
val CompatibilityUnknownBg = Color(0xFF424242)
val CompatibilityBad = Color(0xFFEF5350)
val CompatibilityBadBg = Color(0xFFB71C1C)
