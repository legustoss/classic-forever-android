package app.gamenative.ui.screen.wow

import org.junit.Assert.assertEquals
import org.junit.Test

class BackButtonPlacementTest {

    @Test
    fun `compact shows bottom back`() {
        assertEquals(BackPlacement.BOTTOM, backPlacementFor(true))
    }

    @Test
    fun `non compact shows top back`() {
        assertEquals(BackPlacement.TOP, backPlacementFor(false))
    }
}
