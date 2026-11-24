package app.gamenative.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import okhttp3.Request
import timber.log.Timber

enum class ComponentKind { WCP, ZIP }

data class LauncherComponent(
    val id: String,
    val slot: String,
    val displayName: String,
    val bundledAsset: String?,
    val kind: ComponentKind,
    val recommended: Boolean,
    val downloadUrl: String? = null,
    val sha256: String? = null,
)

/** User-selectable runtime components. Bundled files stay as the fallback; overrides live in app-private storage. */
object ComponentStore {
    private const val DIR = "components"

    val catalog: List<LauncherComponent> = listOf(
        LauncherComponent(
            id = "proton",
            slot = "proton",
            displayName = "Proton 11 (components-v1)",
            bundledAsset = "proton-11.0-90624-arm64ec.wcp",
            kind = ComponentKind.WCP,
            recommended = true,
        ),
        LauncherComponent(
            id = "proton-v16",
            slot = "proton",
            displayName = "Proton 11.0-12.8 arm64ec (v16 test)",
            bundledAsset = null,
            kind = ComponentKind.WCP,
            recommended = false,
            downloadUrl = "https://github.com/The412Banner/proton-wine/releases/download/build-bionic-layers-20261030-v16/DroidDeck-proton-11.0-12.8-arm64ec.wcp",
            sha256 = "a5e05e35214f34603f8cf9a362bb6b4bce9f18d773ee689ecfaff6136a1e9003",
        ),
        LauncherComponent(
            id = "dxvk",
            slot = "dxvk",
            displayName = "DXVK 2.4.1 aarch64",
            bundledAsset = "dxvk-2.4.1-wow-aarch64-test.wcp",
            kind = ComponentKind.WCP,
            recommended = true,
        ),
        LauncherComponent(
            id = "turnip740",
            slot = "turnip740",
            displayName = "Turnip Adreno 740",
            bundledAsset = "turnip-wow-scheduler-test.zip",
            kind = ComponentKind.ZIP,
            recommended = true,
        ),
        LauncherComponent(
            id = "turnip8xx",
            slot = "turnip8xx",
            displayName = "Turnip Adreno 8xx V32",
            bundledAsset = "turnip-V32-RP6sched.zip",
            kind = ComponentKind.ZIP,
            recommended = true,
        ),
    )

    /** Runtime slot backing each catalog id; unknown ids keep their old behavior. */
    private fun slotFor(id: String): String = catalog.firstOrNull { it.id == id }?.slot ?: id

    fun file(context: Context, id: String): File {
        val ext = if (id.startsWith("turnip")) "zip" else "wcp"
        return File(File(context.filesDir, DIR), "$id.$ext")
    }

    /** The installed override for a runtime slot, or null when only the bundled asset is available. */
    fun activeFile(context: Context, slot: String): File? = file(context, slot).takeIf { it.isFile }

    fun isOverridden(context: Context, id: String): Boolean = activeFile(context, slotFor(id)) != null

    fun remove(context: Context, id: String): Boolean {
        val target = file(context, slotFor(id))
        return if (target.exists()) target.delete() else true
    }

    fun download(context: Context, entry: LauncherComponent): Boolean {
        val url = entry.downloadUrl ?: return false
        return try {
            val request = Request.Builder().url(url).build()
            Net.http.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return false
                val body = response.body ?: return false
                body.byteStream().use { input -> install(context, entry.slot, input, entry.sha256) }
            }
        } catch (t: Throwable) {
            Timber.e(t, "Component download failed: ${entry.id}")
            false
        }
    }

    fun import(context: Context, id: String, uri: Uri): Boolean = try {
        val entry = catalog.firstOrNull { it.id == id }
        val expected = entry?.sha256
        val slot = entry?.slot ?: id
        context.contentResolver.openInputStream(uri)?.use { input -> install(context, slot, input, expected) } ?: false
    } catch (t: Throwable) {
        Timber.e(t, "Component import failed: $id")
        false
    }

    private fun install(context: Context, slot: String, input: InputStream, expectedSha: String?): Boolean {
        val target = file(context, slot)
        val tmp = File(target.parentFile, "${target.name}.tmp")
        return try {
            target.parentFile?.mkdirs()
            tmp.outputStream().use { output -> input.copyTo(output) }
            if (tmp.length() <= 0L) {
                tmp.delete()
                return false
            }
            val actual = sha256(tmp)
            if (expectedSha != null && !actual.equals(expectedSha, ignoreCase = true)) {
                Timber.e("Component checksum mismatch: $slot expected=$expectedSha actual=$actual")
                tmp.delete()
                return false
            }
            if (target.exists() && !target.delete()) {
                tmp.delete()
                return false
            }
            val ok = tmp.renameTo(target)
            if (!ok) tmp.delete()
            Timber.i("Component installed: $slot sha256=$actual")
            ok
        } catch (t: Throwable) {
            Timber.e(t, "Component install failed: $slot")
            tmp.delete()
            false
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
