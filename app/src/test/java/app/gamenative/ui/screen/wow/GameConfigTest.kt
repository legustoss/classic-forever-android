package app.gamenative.ui.screen.wow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GameConfigTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun configWtf(root: File) = File(root, "${WowClientDownloader.FLAVOR_DIR}/WTF/Config.wtf")

    private fun install(branch: String, config: String? = null): File {
        val root = tempFolder.newFolder()
        File(root, WowClientDownloader.BUILD_INFO).writeText(
            "Branch!STRING:0|Product!STRING:0\n$branch|${WowClientDownloader.TARGET_PRODUCT}\n",
        )
        config?.let { configWtf(root).apply { parentFile?.mkdirs() }.writeText(it) }
        return root
    }

    @Test
    fun regionFor_usesInstallBranch() {
        assertEquals("eu", WowClientDownloader.regionFor(install("eu")))
        assertEquals("kr", WowClientDownloader.regionFor(install("kr")))
    }

    @Test
    fun config_keepsExistingPortalUntouched() {
        val root = install("us", "SET portal \"US\"\nSET gxApi \"D3D11\"\n")
        ensureGameConfig(root, "1920x1080")
        assertTrue(configWtf(root).readLines().contains("SET portal \"US\""))
    }

    @Test
    fun firstRun_nativeResolutionAndHalfRenderScale() {
        val root = install("us")
        ensureGameConfig(root, "2772x1280")
        assertEquals(
            listOf("SET gxFullscreenResolution \"2772x1280\"", "SET gxWindowedResolution \"2772x1280\""),
            configWtf(root).readLines().filter { it.contains("Resolution ") },
        )
        assertTrue(configWtf(root).readLines().contains("SET RenderScale \"0.5\""))
        assertTrue(configWtf(root).readLines().contains("SET gxFullscreen \"1\""))
        assertTrue(configWtf(root).readLines().contains("SET gxVSync \"0\""))
    }

    @Test
    fun laterRuns_keepPlayerChangedResolutionAndRenderScale() {
        val root = install("us", "SET gxFullscreenResolution \"1280x720\"\nSET gxWindowedResolution \"1280x720\"\nSET RenderScale \"0.8\"\n")
        ensureGameConfig(root, "2772x1280")
        val lines = configWtf(root).readLines()
        assertTrue(lines.contains("SET gxFullscreenResolution \"1280x720\""))
        assertTrue(lines.contains("SET gxWindowedResolution \"1280x720\""))
        assertTrue(lines.contains("SET RenderScale \"0.8\""))
    }

    @Test
    fun forcedMigration_reseedsDisplayOnly() {
        val root = install("us", "SET gxFullscreenResolution \"2120x978\"\nSET gxWindowedResolution \"2120x978\"\nSET RenderScale \"1\"\nSET gxApi \"D3D11\"\n")
        ensureGameConfig(root, "2772x1280", forceDisplayDefaults = true)
        val lines = configWtf(root).readLines()
        assertTrue(lines.contains("SET gxFullscreenResolution \"2772x1280\""))
        assertTrue(lines.contains("SET gxWindowedResolution \"2772x1280\""))
        assertTrue(lines.contains("SET RenderScale \"0.5\""))
        assertTrue(lines.contains("SET gxApi \"D3D11\""))
    }
}
