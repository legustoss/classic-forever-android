package app.gamenative.utils

import android.content.Context
import android.view.KeyEvent
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ComponentCatalogTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val builtIn = ComponentStore.catalog.first { it.id == "proton" }
    private val v16 = ComponentStore.catalog.first { it.id == "proton-v16" }

    @get:Rule
    val tmp = TemporaryFolder()

    @After
    fun tearDown() {
        ComponentStore.remove(context, "proton")
    }

    private fun installTempFile(name: String, content: String): String {
        val temp = tmp.newFile(name)
        temp.writeBytes(content.toByteArray())
        val hash = ComponentStore.sha256(temp)
        val target = ComponentStore.file(context, "proton")
        target.parentFile?.mkdirs()
        temp.copyTo(target, overwrite = true)
        return hash
    }

    @Test
    fun catalogIdsAreUnique() {
        val ids = ComponentStore.catalog.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun catalogSlotsMatchRuntimeSlots() {
        val slots = ComponentStore.catalog.map { it.slot }
        assertEquals(setOf("proton", "dxvk", "turnip740", "turnip8xx"), slots.toSet())
        assertEquals(4, slots.toSet().size)
        val v16 = ComponentStore.catalog.first { it.id == "proton-v16" }
        assertEquals("proton", v16.slot)
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

    @Test
    fun builtInIsSelectedWithoutOverride() {
        assertNull(ComponentStore.activeSha256(context, "proton"))
        assertTrue(ComponentStore.isSelected(context, builtIn))
        assertFalse(ComponentStore.isSelected(context, v16))
    }

    @Test
    fun downloadableEntryIsSelectedWhenItsExactFileIsInstalled() {
        val hash = installTempFile("v16.wcp", "v16-file-content")
        assertEquals(hash, ComponentStore.activeSha256(context, "proton"))
        // The catalog v16 entry pins the real release checksum. Use a copy carrying the
        // checksum of the exact installed bytes so no large .wcp fixture is needed.
        val installedV16 = v16.copy(sha256 = hash.uppercase())
        assertTrue(ComponentStore.isSelected(context, installedV16))
        assertFalse(ComponentStore.isSelected(context, builtIn))
    }

    @Test
    fun customFileMatchesNoCatalogEntry() {
        val hash = installTempFile("custom.wcp", "custom-file-content")
        assertEquals(hash, ComponentStore.activeSha256(context, "proton"))
        assertFalse(ComponentStore.catalog.filter { it.slot == "proton" }.any { ComponentStore.isSelected(context, it) })
    }
}
