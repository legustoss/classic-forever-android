package app.gamenative.utils

import android.content.Context
import com.winlator.core.GPUInformation
import timber.log.Timber
import java.io.File

enum class DeviceStatus { VALIDATED, PENDING_VALIDATION, UNKNOWN }

data class DeviceCompatibilityResult(
    val gpuModel: String,
    val driverId: String,
    val status: DeviceStatus,
    val forced: Boolean = false,
)

/**
 * Maps the reported Adreno GPU to one of the bundled Turnip drivers. Models that are not validated
 * yet still launch with the closest driver, but the result is flagged so the launcher can warn.
 */
object DeviceCompatibility {
    const val DRIVER_740 = "Turnip-WoW-scheduler-test"
    const val DRIVER_8XX = "Turnip-V32-RP6sched"
    const val PROP_DRIVER_FORCE = "driver.force"

    private const val SYSFS_GPU_MODEL = "/sys/class/kgsl/kgsl-3d0/gpu_model"
    private val ADRENO_MODEL = Regex("""adreno[^0-9]*([0-9]{3})""", RegexOption.IGNORE_CASE)

    internal fun parseGpuModel(renderer: String?): Int? {
        if (renderer.isNullOrBlank()) return null
        return ADRENO_MODEL.find(renderer)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun readSysfsModel(): String? = try {
        File(SYSFS_GPU_MODEL).takeIf { it.isFile }?.readText()?.trim()?.takeIf { it.isNotBlank() }
    } catch (t: Throwable) {
        Timber.w(t, "Could not read $SYSFS_GPU_MODEL")
        null
    }

    internal fun resolve(
        renderer: String?,
        sysfsModel: String? = null,
        forcedDriver: String? = null,
    ): DeviceCompatibilityResult {
        val model = parseGpuModel(renderer) ?: parseGpuModel(sysfsModel)
        val label = when {
            model != null -> "Adreno $model"
            !sysfsModel.isNullOrBlank() -> sysfsModel
            !renderer.isNullOrBlank() -> renderer
            else -> "Unknown"
        }

        val forced = forcedDriver?.trim().orEmpty()
        if (forced.isNotBlank() && !forced.equals("auto", ignoreCase = true)) {
            val forcedId = when {
                forced.equals("740", ignoreCase = true) ||
                    forced.equals(DRIVER_740, ignoreCase = true) ||
                    forced.equals("turnip-740", ignoreCase = true) -> DRIVER_740
                forced.equals("8xx", ignoreCase = true) ||
                    forced.equals(DRIVER_8XX, ignoreCase = true) ||
                    forced.equals("turnip-8xx", ignoreCase = true) -> DRIVER_8XX
                else -> null
            }
            if (forcedId != null) {
                return DeviceCompatibilityResult(label, forcedId, DeviceStatus.VALIDATED, forced = true)
            }
            Timber.w("Ignoring invalid $PROP_DRIVER_FORCE value: $forced")
        }

        val (driver, status) = when (model) {
            740 -> DRIVER_740 to DeviceStatus.VALIDATED
            830, 840 -> DRIVER_8XX to DeviceStatus.VALIDATED
            750, 735 -> DRIVER_740 to DeviceStatus.PENDING_VALIDATION
            825, 850 -> DRIVER_8XX to DeviceStatus.PENDING_VALIDATION
            else -> DRIVER_740 to DeviceStatus.UNKNOWN
        }
        return DeviceCompatibilityResult(label, driver, status)
    }

    fun loadForcedDriver(gameRoot: File): String? {
        val file = File(gameRoot, "classic-forever.properties")
        if (!file.isFile) return null
        return runCatching {
            file.readLines().firstNotNullOfOrNull { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("#") || !trimmed.startsWith("$PROP_DRIVER_FORCE=")) null
                else trimmed.substringAfter('=').trim().ifBlank { null }
            }
        }.getOrNull()
    }

    fun detect(context: Context, gameRoot: File): DeviceCompatibilityResult {
        val renderer = runCatching { GPUInformation.getRenderer(context) }.getOrNull()
        val result = resolve(renderer, readSysfsModel(), loadForcedDriver(gameRoot))
        Timber.i(
            "Device compatibility: gpu=${result.gpuModel} driver=${result.driverId} " +
                "status=${result.status} forced=${result.forced}",
        )
        return result
    }
}
