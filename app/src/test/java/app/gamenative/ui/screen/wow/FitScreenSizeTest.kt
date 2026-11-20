package app.gamenative.ui.screen.wow

import org.junit.Assert.assertEquals
import org.junit.Test

class FitScreenSizeTest {

    @Test
    fun sixteenByNineWindow_keepsBaseSize() {
        assertEquals("1920x1080", fitScreenSize("1920x1080", 2400, 1350))
    }

    @Test
    fun foldInnerScreen_keepsPixelBudget() {
        assertEquals("1104x834", fitScreenSize("1280x720", 2448, 1848))
    }

    @Test
    fun portraitWindow_usesLandscapeAspect() {
        assertEquals("1104x834", fitScreenSize("1280x720", 1848, 2448))
    }

    @Test
    fun adreno740Base_onFoldInnerScreen() {
        assertEquals("1658x1252", fitScreenSize("1920x1080", 2448, 1848))
    }

    @Test
    fun widePhone_widensAndShortens() {
        assertEquals("2146x966", fitScreenSize("1920x1080", 2400, 1080))
    }

    @Test
    fun unknownWindow_keepsBaseSize() {
        assertEquals("1280x720", fitScreenSize("1280x720", 0, 0))
    }

    @Test
    fun nativeResolution_isLandscapeAndEven() {
        assertEquals("2772x1280", nativeScreenSize(1280, 2772))
        assertEquals("2772x1280", nativeScreenSize(2772, 1280))
        assertEquals("1920x1080", nativeScreenSize(0, 0))
    }
}
