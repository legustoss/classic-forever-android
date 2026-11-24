package app.gamenative.utils

import com.winlator.container.Container
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/** Helpers for Quick Menu LSFG state persistence and runtime hot-reload. */
object LsfgQuickMenuHelper {
    private const val TAG = "LsfgQuickMenuHelper"

    /** Lower flow scale reduces frame-generation artifacts on first install. */
    const val DEFAULT_FLOW_SCALE = 0.50f

    data class Settings(
        val multiplier: Int,
        val flowScale: Float,
        val performanceMode: Boolean,
    )

    fun isAvailable(container: Container): Boolean =
        LsfgVkManager.isSupported(container) && LsfgVkManager.isArmed(container)

    fun readSettings(container: Container): Settings = Settings(
        multiplier = LsfgVkManager.multiplier(container),
        flowScale = flowScaleOrDefault(container.getExtra(LsfgVkManager.EXTRA_FLOW_SCALE, "")),
        performanceMode = LsfgVkManager.performanceMode(container),
    )

    private val applyExecutor =
        Executors.newSingleThreadScheduledExecutor { r -> Thread(r, "lsfg-apply").apply { isDaemon = true } }
    private var pendingApply: ScheduledFuture<*>? = null

    fun presentMode(container: Container): String = LsfgVkManager.presentMode(container)

    /** Hot-apply an adaptive-cap step without persisting it: the user's saved
     *  limiter target survives interrupted sessions. */
    fun applyLiveFpsCap(container: Container, capFps: Int) {
        applyExecutor.execute {
            val settings = readSettings(container)
            LsfgVkManager.updateConfigAtRuntime(
                container,
                settings.multiplier >= 2,
                if (settings.multiplier >= 2) settings.multiplier else 2,
                settings.flowScale,
                settings.performanceMode,
                fpsLimitOverride = capFps.coerceAtLeast(0),
            )
        }
    }

    /** Persist the backend and hot-apply it. */
    fun applyBackend(container: Container, backend: String) {
        applyExecutor.execute {
            container.putExtra(LsfgVkManager.EXTRA_BACKEND, backend)
            container.saveData()
            applySettingsNow(container, readSettings(container))
        }
    }

    /** Persist the present mode and hot-apply it. */
    fun applyPresentMode(container: Container, mode: String) {
        applyExecutor.execute {
            container.putExtra(LsfgVkManager.EXTRA_PRESENT_MODE, mode)
            container.saveData()
            applySettingsNow(container, readSettings(container))
        }
    }

    fun sanitizeMultiplier(multiplier: Int): Int =
        if (multiplier < 2) 0 else multiplier.coerceIn(2, 4)

    fun sanitizeFlowScale(flowScale: Float): Float =
        flowScale.coerceIn(0.25f, 1.0f)

    /** Resolve a stored flow-scale string, falling back to the stable default. */
    fun flowScaleOrDefault(raw: String?): Float =
        raw?.toFloatOrNull()?.let(::sanitizeFlowScale) ?: DEFAULT_FLOW_SCALE

    /** Persist and hot-apply settings off the UI thread, coalescing rapid FPS-step changes. */
    @Synchronized
    fun applySettings(container: Container, settings: Settings) {
        val multiplier = sanitizeMultiplier(settings.multiplier)
        val flowScale = sanitizeFlowScale(settings.flowScale)
        val target = Settings(multiplier, flowScale, settings.performanceMode)
        pendingApply?.cancel(false)
        pendingApply = applyExecutor.schedule(
            { applySettingsNow(container, target) },
            APPLY_DEBOUNCE_MS,
            TimeUnit.MILLISECONDS,
        )
    }

    private fun applySettingsNow(
        container: Container,
        settings: Settings,
        fpsLimitOverride: Int? = null,
    ) {
        val multiplier = sanitizeMultiplier(settings.multiplier)
        val flowScale = sanitizeFlowScale(settings.flowScale)
        val presentMode = LsfgVkManager.presentMode(container)
        Timber.tag(TAG).i(
            "Applied LSFG config: multiplier=%d, flowScale=%.2f, performanceMode=%s, presentMode=%s, fpsLimitOverride=%s",
            multiplier,
            flowScale,
            settings.performanceMode,
            presentMode,
            fpsLimitOverride?.toString() ?: "none",
        )

        container.putExtra(LsfgVkManager.EXTRA_MULTIPLIER, multiplier.toString())
        container.putExtra(LsfgVkManager.EXTRA_FLOW_SCALE, String.format(Locale.US, "%.2f", flowScale))
        container.putExtra(LsfgVkManager.EXTRA_PERFORMANCE_MODE, settings.performanceMode.toString())
        container.saveData()

        val effectiveEnabled = multiplier >= 2
        val effectiveMultiplier = if (effectiveEnabled) multiplier else 2
        LsfgVkManager.updateConfigAtRuntime(
            container,
            effectiveEnabled,
            effectiveMultiplier,
            flowScale,
            settings.performanceMode,
            fpsLimitOverride = fpsLimitOverride,
        )
        LsfgVkManager.refreshNativeRuntime(container)
    }

    private const val APPLY_DEBOUNCE_MS = 180L
}
