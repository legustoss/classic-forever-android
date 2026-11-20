package app.gamenative.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ClassicForeverPatcherTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun wtf(root: File) = File(root, "_forever_/WTF")

    @Test
    fun configure_copiesConfigAndOverridesPortal() {
        val root = tempFolder.newFolder()
        wtf(root).mkdirs()
        File(wtf(root), "Config.wtf").writeText(
            "SET gxApi \"D3D11\"\nSET portal \"US\"\nSET gxMaximize \"1\"\n",
        )

        ClassicForeverPatcher.configure(root)

        val lines = File(wtf(root), ClassicForeverPatcher.CONFIG_NAME).readLines()
        assertEquals(1, lines.count { it.startsWith("SET portal ") })
        assertTrue(lines.contains("SET portal \"auth.gpon.com.co\""))
        assertTrue(lines.contains("SET gxApi \"D3D11\""))
        assertTrue(lines.contains("SET gxMaximize \"1\""))
        assertTrue(lines.contains("SET textLocale \"enUS\""))
        // The normal config is left alone: the patcher only owns the -config file.
        assertTrue(File(wtf(root), "Config.wtf").readLines().contains("SET portal \"US\""))
    }

    @Test
    fun configure_propertiesFileOverridesPortal() {
        val root = tempFolder.newFolder()
        wtf(root).mkdirs()
        File(wtf(root), "Config.wtf").writeText("SET portal \"US\"\n")
        File(root, "classic-forever.properties").writeText(
            "# my server\nportal=auth.example.net\nkey=02596F0D0C061A8B30745988FD72C59E29EC367FB0F341F28E0F08D037BAFC69\n",
        )

        ClassicForeverPatcher.configure(root)

        val lines = File(wtf(root), ClassicForeverPatcher.CONFIG_NAME).readLines()
        assertEquals(listOf("SET portal \"auth.example.net\"", "SET textLocale \"enUS\""), lines)
    }

    @Test
    fun configure_isIdempotent() {
        val root = tempFolder.newFolder()
        wtf(root).mkdirs()
        File(wtf(root), "Config.wtf").writeText("SET gxApi \"D3D11\"\nSET portal \"US\"\n")

        ClassicForeverPatcher.configure(root)
        val first = File(wtf(root), ClassicForeverPatcher.CONFIG_NAME).readBytes()
        ClassicForeverPatcher.configure(root)

        assertTrue(first.contentEquals(File(wtf(root), ClassicForeverPatcher.CONFIG_NAME).readBytes()))
        assertEquals("-config ${ClassicForeverPatcher.CONFIG_NAME}", ClassicForeverPatcher.LAUNCH_ARGS)
    }
}
