package app.gamenative.utils

import app.gamenative.ui.screen.wow.WowClientDownloader.EXE_NAME
import app.gamenative.ui.screen.wow.WowClientDownloader.FLAVOR_DIR
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal data class MemoryRegion(val start: Long, val end: Long)

internal enum class StoreState { PATCHED, ORIGINAL, INVALID }

/**
 * Android port of classic-forever-linux.py.
 *
 * Two things are needed to point the released WoW Forever client at a private realm:
 *
 *  1. A separate WTF config (`BetaSuspendedTest.wtf`) that sets `portal` to the server's auth host,
 *     passed to the client with `-config`. [configure] writes it, [LAUNCH_ARGS] adds the flag.
 *  2. The 12-entry public-key store in the client's anonymous heap is patched: entry 8 is replaced
 *     with the server's public key once the client creates the store (on realm connection). The
 *     [watch] thread does that and re-applies it if the client restores the original key.
 *
 * The game is exec'd by this app as a child process, so it shares our UID and SELinux domain and
 * `/proc/<pid>/mem` is readable/writable without root. No code or files on disk are modified.
 *
 * Server values can be overridden without rebuilding the APK by putting a
 * `classic-forever.properties` file in the game root (next to `Data/`):
 *
 *     portal=auth.gpon.com.co
 *     key=02596F0D0C061A8B30745988FD72C59E29EC367FB0F341F28E0F08D037BAFC69
 */
object ClassicForeverPatcher {
    // Defaults from classic-forever-linux.py. Change here, or via classic-forever.properties.
    private const val DEFAULT_PORTAL = "auth.gpon.com.co"
    internal val DEFAULT_NEW_KEY = hex("02596F0D0C061A8B30745988FD72C59E29EC367FB0F341F28E0F08D037BAFC69")

    const val CONFIG_NAME = "BetaSuspendedTest.wtf"
    const val LAUNCH_ARGS = "-config $CONFIG_NAME"

    private val EXE_NAMES = listOf(EXE_NAME, "Wow.exe", "WowB.exe").map { it.lowercase() }

    // Measured 2026-09-20; identical in builds 69977 and 70009.
    internal val KNOWN_KEYS = listOf(
        hex("9B0671C815DFF513BFD4A2B26AE1F84EC9106841B2FB620DB65F6ADE7C21AD06"), // 1, search anchor
        hex("112451B9843C2799E4750AA3D9B9AFABF536A645B863C4ADB2078B932B354E04"),
        hex("A6B858485748BF38BE193517AB90F8DE169EFF0989EA9360DB346A378B0FFE15"),
        hex("50FFDEB807F8FF73A299A16000AA6575C5945BE875ADFC8795374ED3415249AC"),
        hex("9F842D078755647C008E5FE3E12B839A981DE02A1F520CB2545CC04CD2323E0E"),
        hex("B8CC75864D8EF461D8DD6AA7425E2C63C24D3982066B8D773A1549BFE24E05EE"),
        hex("76BB7BBDD9F34E124A4573C3AA227E3C87CC603506697D054F6FDEC342E56EBF"),
        hex("1FD6DD8FA0EC30D39E3F72E755B8A045BDE0F70449DC71008B767C2EAA89FB9F"), // 8, replaced
        hex("15D618BD7DB577BD9A8D45769C59E4FC631633BF447398A4B489B4C26FBC03AD"),
        hex("B34EC592D2AC4993F5FFC85B15C3DA9078694051CB224345592AF7136D796C99"),
        hex("9E91586DD4B115AB0568B21959E181F6545910BC5E2F30B8975CA09D7BC3EFCE"),
        hex("769C824A1DADD19AEEFA420414D1732DAF44537F98AA229FC5477BCA55F9D59A"),
    )
    internal const val TARGET_GROUP = 8
    internal const val ENTRY_SIZE = 40
    internal const val ENTRY_COUNT = 12
    internal const val BLOCK_SIZE = ENTRY_SIZE * ENTRY_COUNT
    internal val EXPECTED_FLAGS = intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1)
    private const val CHUNK = 8 * 1024 * 1024
    private const val MAX_WAIT_MS = 5 * 60 * 1000L

    private data class Settings(val portal: String, val newKey: ByteArray)

    @Volatile
    private var stopped = false
    @Volatile
    private var worker: Thread? = null

    @Volatile
    var lastStatus: String = "Idle"
        private set

    private fun log(message: String, throwable: Throwable? = null) {
        lastStatus = message
        Timber.tag("ClassicForever").i(throwable, message)
        FileLogger.log("ClassicForever", message, throwable)
    }

    private fun settings(gameRoot: File): Settings {
        var portal = DEFAULT_PORTAL
        var key = DEFAULT_NEW_KEY
        val file = File(gameRoot, "classic-forever.properties")
        if (file.isFile) {
            runCatching {
                file.readLines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
                    val parts = trimmed.split('=', limit = 2)
                    if (parts.size != 2) return@forEach
                    when (parts[0].trim().lowercase()) {
                        "portal" -> portal = parts[1].trim().ifBlank { portal }
                        "key" -> {
                            val value = parts[1].trim()
                            if (isValidPublicKeyHex(value)) key = hex(value)
                        }
                    }
                }
            }.onFailure { log("Could not read classic-forever.properties", it) }
        }
        return Settings(portal, key)
    }

    /** Pure validation of the 64-hex public key, usable in unit tests without a device. */
    internal fun isValidPublicKeyHex(value: String): Boolean {
        val clean = value.trim()
        if (clean.length != 64) return false
        return clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    /** Pure parser for a /proc/<pid>/maps dump; filters anonymous private read/write regions. */
    internal fun parseHeapRegions(maps: String): List<MemoryRegion> {
        val regions = mutableListOf<MemoryRegion>()
        maps.lineSequence().forEach { line ->
            val parts = line.split(Regex("\\s+"), limit = 6)
            if (parts.size < 5) return@forEach
            val perms = parts[1]
            val inode = parts[4]
            val path = if (parts.size > 5) parts[5].trim() else ""
            if (perms != "rw-p" || inode != "0" || path.startsWith("[v") || path == "[stack]") return@forEach
            val bounds = parts[0].split("-")
            if (bounds.size != 2) return@forEach
            regions += MemoryRegion(bounds[0].toLong(16), bounds[1].toLong(16))
        }
        return regions
    }

    /** Pure validation of a 12-entry store block; no /proc access. */
    internal fun validateStoreBlock(raw: ByteArray, key: ByteArray): StoreState {
        if (raw.size < BLOCK_SIZE) return StoreState.INVALID
        for (e in 0 until ENTRY_COUNT) {
            val o = e * ENTRY_SIZE
            val ident = ByteBuffer.wrap(raw, o, 4).order(ByteOrder.LITTLE_ENDIAN).int
            if (ident != e + 1) return StoreState.INVALID
            val stored = raw.copyOfRange(o + 4, o + 36)
            val ok = if (e == TARGET_GROUP - 1) {
                stored contentEquals KNOWN_KEYS[e] || stored contentEquals key
            } else {
                stored contentEquals KNOWN_KEYS[e]
            }
            if (!ok || raw[o + 36].toInt() != EXPECTED_FLAGS[e]) return StoreState.INVALID
        }
        val o8 = (TARGET_GROUP - 1) * ENTRY_SIZE + 4
        return if (raw.copyOfRange(o8, o8 + 32) contentEquals key) StoreState.PATCHED else StoreState.ORIGINAL
    }

    /** Writes `WTF/BetaSuspendedTest.wtf` from the normal Config.wtf with the private portal. */
    fun configure(gameRoot: File) {
        val wtfDir = File(gameRoot, "$FLAVOR_DIR/WTF")
        wtfDir.mkdirs()
        val target = File(wtfDir, CONFIG_NAME)
        val base = File(wtfDir, "Config.wtf")
        if (!target.exists() && base.isFile) {
            base.copyTo(target, overwrite = false)
            log("Created WTF/$CONFIG_NAME from Config.wtf")
        }
        val original = if (target.isFile) target.readLines() else emptyList()
        val portal = settings(gameRoot).portal
        val wanted = "SET portal \"$portal\""
        val lines = mutableListOf<String>()
        var hadPortal = false
        var hadLocale = false
        for (line in original) {
            when {
                line.startsWith("SET portal ", ignoreCase = true) -> {
                    hadPortal = true
                    lines += wanted
                }
                line.startsWith("SET textLocale ", ignoreCase = true) -> {
                    hadLocale = true
                    lines += line
                }
                else -> lines += line
            }
        }
        if (!hadPortal) lines += wanted
        if (!hadLocale) lines += "SET textLocale \"enUS\""
        if (!target.isFile || lines != original) {
            target.writeText(lines.joinToString("\r\n") + "\r\n")
            log("Configured WTF/$CONFIG_NAME (portal $portal)")
        }
    }

    /** Starts the background memory watcher. Safe to call repeatedly; replaces any previous run. */
    fun start(gameRoot: File) {
        stop()
        stopped = false
        FileLogger.init(gameRoot)
        val cfg = settings(gameRoot)
        worker = Thread({
            try {
                watch(cfg)
            } catch (_: InterruptedException) {
                // Normal shutdown via stop().
                lastStatus = "Stopped"
            } catch (t: Throwable) {
                log("Patcher thread failed", t)
                lastStatus = "Patcher error: ${t.message}"
            }
        }, "classic-forever-patcher").apply {
            isDaemon = true
            start()
        }
    }

    fun stop() {
        stopped = true
        worker?.interrupt()
        worker = null
    }

    // -------------------------------------------------------------------------------- process discovery

    private fun cmdline(pid: Int): List<String> = try {
        String(File("/proc/$pid/cmdline").readBytes(), Charsets.UTF_8)
            .split('\u0000')
            .filter { it.isNotEmpty() }
    } catch (_: IOException) {
        emptyList()
    }

    private fun comm(pid: Int): String = try {
        File("/proc/$pid/comm").readText().trim().lowercase()
    } catch (_: IOException) {
        ""
    }

    private fun mapsText(pid: Int): String = try {
        File("/proc/$pid/maps").readText().lowercase().replace('\\', '/')
    } catch (_: IOException) {
        ""
    }

    private fun mentionsExe(text: String): Boolean = EXE_NAMES.any { text.contains(it) }

    private fun rss(pid: Int): Long = try {
        File("/proc/$pid/statm").readText().split(Regex("\\s+"))[1].toLong()
    } catch (_: Exception) {
        0L
    }

    private fun alive(pid: Int): Boolean = File("/proc/$pid").exists() && rss(pid) > 0

    /**
     * Wine under Proton can present the game under many names: the .exe may be in argv[0], in comm,
     * only in the mapped files, or nowhere at all. Accept any mention and log what we saw so a
     * missed process is diagnosable from the log file.
     */
    private fun clientInfo(pid: Int): String? {
        val name = comm(pid)
        val cmd = cmdline(pid).joinToString(" ").lowercase()
        val byName = mentionsExe(name) || name.contains("wow")
        val byCmd = mentionsExe(cmd) || cmd.contains("wow") || cmd.contains("wine")
        if (!byName && !byCmd) return null
        val maps = mapsText(pid)
        val byMaps = mentionsExe(maps)
        if (!byName && !byCmd && !byMaps) return null
        return "pid=$pid comm=$name rss=${rss(pid) * 4 / 1024}MB mapsExe=$byMaps cmd=${cmd.take(180)}"
    }

    private fun procPids(): List<Int> =
        File("/proc").listFiles { f -> f.isDirectory && f.name.all { it.isDigit() } }
            ?.mapNotNull { it.name.toIntOrNull() }
            ?: emptyList()

    private fun findClients(): List<Int> {
        val me = android.os.Process.myPid()
        return procPids().mapNotNull { pid ->
            if (pid == me || clientInfo(pid) == null) null else pid to rss(pid)
        }.sortedByDescending { it.second }.map { it.first }
    }

    /** Written to the log file while waiting, so we can see which processes were considered. */
    private fun describeCandidates() {
        val me = android.os.Process.myPid()
        val lines = procPids().mapNotNull { pid ->
            if (pid == me) null else clientInfo(pid)
        }
        log("Processes mentioning the game:\n" + (lines.joinToString("\n").ifBlank { "  (none)" }))
    }

    private fun betterClient(pid: Int): Int? {
        val mine = rss(pid)
        return findClients().firstOrNull { it != pid && rss(it) > maxOf(mine * 2, 50 * 1024) }
    }

    // -------------------------------------------------------------------------------- memory access

    private fun heapRegions(pid: Int): List<MemoryRegion> = try {
        parseHeapRegions(File("/proc/$pid/maps").readText())
    } catch (_: IOException) {
        // Process went away while reading maps.
        emptyList()
    }

    private fun regionOf(pid: Int, address: Long, size: Int): MemoryRegion? =
        heapRegions(pid).firstOrNull { it.start <= address && address + size <= it.end }

    private fun read(pid: Int, address: Long, size: Int): ByteArray {
        RandomAccessFile("/proc/$pid/mem", "r").use { mem ->
            mem.seek(address)
            val buf = ByteArray(size)
            var off = 0
            while (off < size) {
                val n = mem.read(buf, off, size - off)
                if (n <= 0) throw IOException("short read at 0x${address.toString(16)}")
                off += n
            }
            return buf
        }
    }

    private fun scan(pid: Int, needle: ByteArray): List<Long> {
        val hits = HashSet<Long>()
        try {
            RandomAccessFile("/proc/$pid/mem", "r").use { mem ->
                for ((lo, hi) in heapRegions(pid)) {
                    var off = lo
                    while (off < hi) {
                        val take = (minOf(CHUNK.toLong(), hi - off)).toInt()
                        try {
                            mem.seek(off)
                            val buf = ByteArray(take)
                            var read = 0
                            while (read < take) {
                                val n = mem.read(buf, read, take - read)
                                if (n <= 0) break
                                read += n
                            }
                            if (read == 0) break
                            var i = indexOf(buf, needle, 0)
                            while (i >= 0) {
                                if ((off + i) % 4L == 0L) hits.add(off + i.toLong())
                                i = indexOf(buf, needle, i + 1)
                            }
                            if (read < take) break
                        } catch (_: IOException) {
                            break
                        }
                        off += CHUNK - needle.size
                    }
                }
            }
        } catch (_: IOException) {
            throw IOException("Cannot access /proc/$pid/mem (the game must be a child of this app)")
        }
        return hits.sorted()
    }

    private fun storeState(pid: Int, array: Long, key: ByteArray): String {
        if (regionOf(pid, array, BLOCK_SIZE) == null) return "invalid"
        val raw = try {
            read(pid, array, BLOCK_SIZE)
        } catch (_: IOException) {
            return "invalid"
        }
        return when (validateStoreBlock(raw, key)) {
            StoreState.PATCHED -> "patched"
            StoreState.ORIGINAL -> "original"
            StoreState.INVALID -> "invalid"
        }
    }

    private fun findStores(pid: Int, key: ByteArray): Pair<List<Pair<Long, String>>, String> {
        val start = System.currentTimeMillis()
        val hits = scan(pid, KNOWN_KEYS[0])
        val found = hits.mapNotNull { hit ->
            val array = hit - 4
            val state = storeState(pid, array, key)
            if (state == "invalid") null else array to state
        }
        val info = "${System.currentTimeMillis() - start} ms, ${hits.size} key copies, ${found.size} store(s)"
        return found to info
    }

    private fun patch(pid: Int, array: Long, key: ByteArray) {
        val address = array + (TARGET_GROUP - 1) * ENTRY_SIZE + 4
        // Idempotency: a repeated call on an already patched store must not wait or rewrite.
        if (storeState(pid, array, key) == "patched") {
            log("Store 0x${array.toString(16)} is already patched; nothing to write")
            return
        }
        val original = KNOWN_KEYS[TARGET_GROUP - 1]
        var written = false
        for (attempt in 0 until 20) {
            if (regionOf(pid, address, 32) == null) {
                throw IOException("pid=$pid addr=0x${address.toString(16)}: key is not in anonymous rw memory")
            }
            if (read(pid, address, 32) contentEquals original) {
                RandomAccessFile("/proc/$pid/mem", "rw").use { mem ->
                    mem.seek(address)
                    mem.write(key)
                }
                if (!(read(pid, address, 32) contentEquals key)) {
                    throw IOException("pid=$pid addr=0x${address.toString(16)}: read-back verification failed")
                }
                written = true
                break
            }
            Thread.sleep((100L * attempt).coerceAtMost(300L))
        }
        if (!written) throw IOException("pid=$pid addr=0x${address.toString(16)}: key bytes changed before write")
        if (storeState(pid, array, key) != "patched") {
            throw IOException("pid=$pid addr=0x${address.toString(16)}: store did not validate after patch")
        }
        log("Server public key written at 0x${address.toString(16)}")
    }

    private fun watch(cfg: Settings) {
        var pid: Int? = null
        var store: Long? = null
        var everPatched = false
        var waitingSaid = false
        var lastReport = 0L
        var waited = 0L

        while (!stopped) {
            if (pid != null && !alive(pid!!)) {
                log("Game closed.")
                return
            }
            if (pid == null) {
                pid = findClients().firstOrNull()
                if (pid == null) {
                    waited += 500
                    if (waited > MAX_WAIT_MS) {
                        log("Game process not found. Start the game and try again.")
                        return
                    }
                    if (!waitingSaid) {
                        log("Waiting for ${EXE_NAME}...")
                        waitingSaid = true
                        lastReport = System.currentTimeMillis()
                    }
                    if (System.currentTimeMillis() - lastReport > 10_000) {
                        describeCandidates()
                        lastReport = System.currentTimeMillis()
                    }
                    Thread.sleep(500)
                    continue
                }
                log("Found game: ${clientInfo(pid!!)}")
                log("Game PID $pid. Log in and enter a realm.")
                lastReport = System.currentTimeMillis()
            }

            val current = pid!!
            if (store != null) {
                when (storeState(current, store!!, cfg.newKey)) {
                    "patched" -> {
                        Thread.sleep(5000)
                        continue
                    }
                    "original" -> {
                        log("Client restored the original key; re-applying.")
                        patch(current, store!!, cfg.newKey)
                        ready()
                        continue
                    }
                    else -> {
                        log("Key store was freed; searching again.")
                        store = null
                    }
                }
            }

            // A launcher (wine/proton wrapper) can be the first match; move to the real client.
            if (System.currentTimeMillis() - lastReport > 10_000) {
                val other = betterClient(current)
                if (other != null) {
                    log("Switching from PID $current to game PID $other")
                    pid = other
                    store = null
                    lastReport = System.currentTimeMillis()
                    continue
                }
            }

            val (found, info) = try {
                findStores(current, cfg.newKey)
            } catch (e: IOException) {
                log("Memory access denied: ${e.message}")
                return
            }
            when {
                found.size == 1 -> {
                    val (address, state) = found[0]
                    store = address
                    if (state == "patched") {
                        log("Server key already applied.")
                    } else {
                        try {
                            patch(current, address, cfg.newKey)
                        } catch (e: Exception) {
                            log("Patch failed (${e.message}); retrying.")
                            store = null
                            Thread.sleep(1000)
                            continue
                        }
                    }
                    everPatched = true
                    ready()
                }
                found.size > 1 -> log("${found.size} valid stores at once; not writing until one remains.")
                else -> {
                    val now = System.currentTimeMillis()
                    if (!waitingSaid || now - lastReport > 10_000) {
                        log("Waiting for realm connection ($info)")
                        waitingSaid = true
                        lastReport = now
                    }
                }
            }
            // A full heap scan is expensive; do it at most once a second while looking for the store.
            Thread.sleep(if (everPatched) 5000 else 1000)
        }
    }

    private fun ready() {
        log("READY. If the first realm login failed, retry WITHOUT closing the game.")
    }

    /** ByteArray.indexOf only accepts a single Byte, so search for the 32-byte anchor by hand. */
    private fun indexOf(buf: ByteArray, needle: ByteArray, from: Int): Int {
        val last = buf.size - needle.size
        var i = maxOf(from, 0)
        outer@ while (i <= last) {
            var j = 0
            while (j < needle.size) {
                if (buf[i + j] != needle[j]) {
                    i++
                    continue@outer
                }
                j++
            }
            return i
        }
        return -1
    }

    private fun hex(value: String): ByteArray {
        val clean = value.trim().replace(" ", "")
        require(clean.length % 2 == 0) { "Invalid hex key" }
        return ByteArray(clean.length / 2) { i -> clean.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
    }
}
