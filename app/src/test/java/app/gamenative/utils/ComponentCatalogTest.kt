package app.gamenative.utils

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ComponentCatalogTest {
    @Test
    fun catalogIdsAreUnique() {
        val ids = ComponentStore.catalog.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun recommendedComponentsUseBundledAssets() {
        ComponentStore.catalog.filter { it.recommended }.forEach {
            assertNotNull("${it.id} must have a bundled asset", it.bundledAsset)
            assertEquals(null, it.downloadUrl)
        }
    }

    @Test
    fun downloadableComponentsHaveVerifiedChecksums() {
        ComponentStore.catalog.filter { it.downloadUrl != null }.forEach {
            assertNotNull("${it.id} must have a SHA-256", it.sha256)
            assertEquals(64, it.sha256!!.length)
            assertTrue(it.sha256!!.all { c -> c.isDigit() || c in 'a'..'f' })
        }
    }

    @Test
    fun gamepadKeysMapToLauncherActions() {
        assertTrue(isLauncherGamepadConfirm(KeyEvent.KEYCODE_BUTTON_A))
        assertFalse(isLauncherGamepadConfirm(KeyEvent.KEYCODE_BUTTON_B))
        assertTrue(isLauncherGamepadBack(KeyEvent.KEYCODE_BUTTON_B))
        assertFalse(isLauncherGamepadBack(KeyEvent.KEYCODE_BUTTON_A))
    }
}
