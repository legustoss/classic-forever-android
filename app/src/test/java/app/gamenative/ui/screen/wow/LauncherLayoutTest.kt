package app.gamenative.ui.screen.wow

import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherLayoutTest {
    @Test
    fun short_content_is_centered_by_half_the_unused_height() {
        assertEquals(120f, measuredTopSpacerPx(600, 360))
    }

    @Test
    fun taller_content_is_not_pushed_down() {
        assertEquals(0f, measuredTopSpacerPx(600, 720))
    }

    @Test
    fun content_that_exactly_fits_gets_no_spacer() {
        assertEquals(0f, measuredTopSpacerPx(600, 600))
    }

    @Test
    fun unmeasured_content_gets_no_spacer() {
        assertEquals(0f, measuredTopSpacerPx(600, 0))
    }
}
