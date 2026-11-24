package app.gamenative.ui.screen.wow

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherLayoutTest {
    @Test
    fun portrait_main_gets_top_spacer() {
        assertEquals(64.dp, launcherTopPadding(isMain = true, isWide = false))
    }

    @Test
    fun other_screens_have_no_extra_top_spacer() {
        assertEquals(0.dp, launcherTopPadding(isMain = false, isWide = false))
        assertEquals(0.dp, launcherTopPadding(isMain = true, isWide = true))
        assertEquals(0.dp, launcherTopPadding(isMain = false, isWide = true))
    }
}
