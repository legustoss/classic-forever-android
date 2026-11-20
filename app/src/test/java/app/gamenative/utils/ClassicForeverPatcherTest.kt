package app.gamenative.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

    private fun fakeStoreBlock(group8Key: ByteArray): ByteArray {
        val raw = ByteArray(ClassicForeverPatcher.BLOCK_SIZE)
        for (e in 0 until ClassicForeverPatcher.ENTRY_COUNT) {
            val o = e * ClassicForeverPatcher.ENTRY_SIZE
            ByteBuffer.wrap(raw, o, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(e + 1)
            val key = if (e == ClassicForeverPatcher.TARGET_GROUP - 1) {
                group8Key
            } else {
                ClassicForeverPatcher.KNOWN_KEYS[e]
            }
            key.copyInto(raw, o + 4)
            raw[o + 36] = ClassicForeverPatcher.EXPECTED_FLAGS[e].toByte()
        }
        return raw
    }

    @Test
    fun publicKeyHex_isStrictlyValidated() {
        val valid = "02596F0D0C061A8B30745988FD72C59E29EC367FB0F341F28E0F08D037BAFC69"
        assertTrue(ClassicForeverPatcher.isValidPublicKeyHex(valid))
        assertTrue(ClassicForeverPatcher.isValidPublicKeyHex(valid.lowercase()))
        assertFalse(ClassicForeverPatcher.isValidPublicKeyHex(valid.dropLast(1)))
        assertFalse(ClassicForeverPatcher.isValidPublicKeyHex(valid.replaceRange(0, 1, "Z")))
    }

    @Test
    fun parseHeapRegions_filtersAnonymousWritableRegions() {
        val maps = """
            7f000000-7f001000 rw-p 00000000 00:00 0
            7f002000-7f003000 rw-p 00000000 00:00 0 [anon:libc_malloc]
            7f004000-7f005000 r--p 00000000 00:00 0 /system/lib/libc.so
            7f006000-7f007000 rw-p 00000000 00:00 0 [stack]
            7f008000-7f009000 rw-p 00000000 00:00 0 [vdso]
        """.trimIndent()
        assertEquals(
            listOf(
                MemoryRegion(0x7f000000L, 0x7f001000L),
                MemoryRegion(0x7f002000L, 0x7f003000L),
            ),
            ClassicForeverPatcher.parseHeapRegions(maps),
        )
    }

    @Test
    fun validateStoreBlock_recognizesOriginalPatchedAndInvalid() {
        val key = ClassicForeverPatcher.DEFAULT_NEW_KEY
        val group8 = ClassicForeverPatcher.TARGET_GROUP - 1

        val original = fakeStoreBlock(ClassicForeverPatcher.KNOWN_KEYS[group8])
        assertEquals(StoreState.ORIGINAL, ClassicForeverPatcher.validateStoreBlock(original, key))

        val patched = fakeStoreBlock(key)
        assertEquals(StoreState.PATCHED, ClassicForeverPatcher.validateStoreBlock(patched, key))

        val corrupted = patched.copyOf()
        ByteBuffer.wrap(corrupted, 0, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(99)
        assertEquals(StoreState.INVALID, ClassicForeverPatcher.validateStoreBlock(corrupted, key))
    }
}
