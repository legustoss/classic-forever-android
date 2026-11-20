package app.gamenative.utils

import app.gamenative.ui.screen.wow.WowClientDownloader.BUILD_INFO
import timber.log.Timber
import java.io.File

/** Optional server-side build window from classic-forever.properties. */
internal data class ServerBuildRequirement(
    val minBuild: Int? = null,
    val maxBuild: Int? = null,
)

internal sealed class ClientCompatibility {
    data class Compatible(val build: Int, val requirement: ServerBuildRequirement) : ClientCompatibility()
    data class TooOld(val build: Int, val requirement: ServerBuildRequirement) : ClientCompatibility()
    data class TooNew(val build: Int?, val requirement: ServerBuildRequirement) : ClientCompatibility()
}

/**
 * Reads the active `wowf` row of .build.info and compares its build number with the optional
 * server bounds. Does not change any game files or the updater's download logic.
 */
object ClientVersionChecker {
    const val PROP_MIN_BUILD = "server.min.build"
    const val PROP_MAX_BUILD = "server.max.build"
    private const val PRODUCT = "wowf"
    private const val PROPERTIES = "classic-forever.properties"

    /** Extracts the numeric build from a version string such as 1.60.1.70450. */
    internal fun parseBuildNumber(version: String): Int? =
        version.trim().split(".").lastOrNull()?.toIntOrNull()?.takeIf { it > 0 }

    /** Locates the wowf line in a PSV .build.info dump and returns its build number. */
    internal fun parseBuildInfo(text: String): Int? {
        val lines = text.lines().filter { it.isNotBlank() && !it.startsWith("#") }
        val header = lines.firstOrNull()?.split("|")?.map { it.substringBefore("!") } ?: return null
        val productIndex = header.indexOf(PRODUCT_HEADER).takeIf { it >= 0 } ?: return null
        val versionIndex = header.indexOf(VERSION_HEADER).takeIf { it >= 0 } ?: return null

        return lines.drop(1).mapNotNull { line ->
            val cells = line.split("|")
            if (productIndex >= cells.size || versionIndex >= cells.size) return@mapNotNull null
            if (cells[productIndex].trim() != PRODUCT) return@mapNotNull null
            parseBuildNumber(cells[versionIndex])
        }.firstOrNull()
    }

    internal fun parseRequirement(properties: String?): ServerBuildRequirement {
        var min: Int? = null
        var max: Int? = null
        properties?.lineSequence()?.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
            val parts = trimmed.split('=', limit = 2)
            if (parts.size != 2) return@forEach
            val value = parts[1].trim().toIntOrNull()?.takeIf { it > 0 } ?: return@forEach
            when (parts[0].trim()) {
                PROP_MIN_BUILD -> min = value
                PROP_MAX_BUILD -> max = value
            }
        }
        if (min != null && max != null && min!! > max!!) {
            Timber.w("Invalid $PROP_MIN_BUILD/$PROP_MAX_BUILD ($min > $max); ignoring bounds")
            return ServerBuildRequirement()
        }
        return ServerBuildRequirement(min, max)
    }

    internal fun validateAgainstServer(
        currentBuild: Int?,
        requirement: ServerBuildRequirement,
    ): ClientCompatibility {
        if (currentBuild == null) return ClientCompatibility.TooNew(null, requirement)
        requirement.minBuild?.let { min ->
            if (currentBuild < min) return ClientCompatibility.TooOld(currentBuild, requirement)
        }
        requirement.maxBuild?.let { max ->
            if (currentBuild > max) return ClientCompatibility.TooNew(currentBuild, requirement)
        }
        return ClientCompatibility.Compatible(currentBuild, requirement)
    }

    internal fun check(gameRoot: File): ClientCompatibility {
        val buildInfoText = runCatching {
            File(gameRoot, BUILD_INFO).takeIf { it.isFile }?.readText()
        }.getOrNull()
        val properties = runCatching {
            File(gameRoot, PROPERTIES).takeIf { it.isFile }?.readText()
        }.getOrNull()
        return validateAgainstServer(buildInfoText?.let(::parseBuildInfo), parseRequirement(properties))
    }

    private const val PRODUCT_HEADER = "Product"
    private const val VERSION_HEADER = "Version"
}
