package app.gamenative.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber

enum class ServerStatusKind { DISCORD_ONLINE, IN_GAME_ONLINE, UNKNOWN }

data class ServerStatus(val kind: ServerStatusKind, val count: Int? = null) {
    companion object {
        val UNKNOWN = ServerStatus(ServerStatusKind.UNKNOWN)
    }
}

interface ServerStatusProvider {
    suspend fun fetch(): ServerStatus
}

/** Discord widget presence. Label must be "Discord online", never "players in game". */
internal class DiscordWidgetProvider(
    private val guildId: String,
    private val http: OkHttpClient,
) : ServerStatusProvider {
    override suspend fun fetch(): ServerStatus = withContext(Dispatchers.IO) {
        if (guildId.isBlank()) return@withContext ServerStatus.UNKNOWN
        try {
            val request = Request.Builder()
                .url("https://discord.com/api/guilds/$guildId/widget.json")
                .build()
            http.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Timber.w("Discord widget returned HTTP ${response.code}")
                    return@use ServerStatus.UNKNOWN
                }
                val body = response.body?.string() ?: return@use ServerStatus.UNKNOWN
                val count = JSONObject(body).readCount("presence_count")
                if (count == null) ServerStatus.UNKNOWN else ServerStatus(ServerStatusKind.DISCORD_ONLINE, count)
            }
        } catch (t: Throwable) {
            Timber.w(t, "Discord widget fetch failed")
            ServerStatus.UNKNOWN
        }
    }
}

/** Optional HTTP status endpoint; the response is treated as in-game concurrency. */
internal class HttpStatusProvider(
    private val url: String,
    private val http: OkHttpClient,
) : ServerStatusProvider {
    override suspend fun fetch(): ServerStatus = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext ServerStatus.UNKNOWN
        try {
            val request = Request.Builder().url(url).build()
            http.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Timber.w("Server status endpoint returned HTTP ${response.code}")
                    return@use ServerStatus.UNKNOWN
                }
                val body = response.body?.string() ?: return@use ServerStatus.UNKNOWN
                val count = JSONObject(body).readCount("players", "online", "in_game", "count")
                if (count == null) ServerStatus.UNKNOWN else ServerStatus(ServerStatusKind.IN_GAME_ONLINE, count)
            }
        } catch (t: Throwable) {
            Timber.w(t, "Server status endpoint fetch failed")
            ServerStatus.UNKNOWN
        }
    }
}

internal object NoOpProvider : ServerStatusProvider {
    override suspend fun fetch(): ServerStatus = ServerStatus.UNKNOWN
}

internal data class ServerStatusConfig(
    val source: Source = Source.NONE,
    val guildId: String = DEFAULT_GUILD_ID,
    val url: String = "",
) {
    enum class Source { DISCORD, HTTP, NONE }

    companion object {
        const val KEY_SOURCE = "server.status.source"
        const val KEY_GUILD_ID = "server.status.guild.id"
        const val KEY_URL = "server.status.url"

        /** Classic Forever guild id, resolved from the public invite; widget must still be enabled. */
        const val DEFAULT_GUILD_ID = "1553031052669493359"

        internal fun parse(properties: String?): ServerStatusConfig {
            var source = Source.NONE
            var guildId = DEFAULT_GUILD_ID
            var url = ""
            properties?.lineSequence()?.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
                val parts = trimmed.split('=', limit = 2)
                if (parts.size != 2) return@forEach
                when (parts[0].trim()) {
                    KEY_SOURCE -> source = when (parts[1].trim().lowercase()) {
                        "discord" -> Source.DISCORD
                        "http" -> Source.HTTP
                        else -> Source.NONE
                    }
                    KEY_GUILD_ID -> guildId = parts[1].trim().ifBlank { guildId }
                    KEY_URL -> url = parts[1].trim()
                }
            }
            return ServerStatusConfig(source, guildId, url)
        }
    }
}

internal object ServerStatusFactory {
    fun create(config: ServerStatusConfig, http: OkHttpClient): ServerStatusProvider =
        when (config.source) {
            ServerStatusConfig.Source.DISCORD -> DiscordWidgetProvider(config.guildId, http)
            ServerStatusConfig.Source.HTTP -> HttpStatusProvider(config.url, http)
            ServerStatusConfig.Source.NONE -> NoOpProvider
        }
}

private fun JSONObject.readCount(vararg keys: String): Int? = keys.firstNotNullOfOrNull { key ->
    when (val value = opt(key)) {
        null -> null
        is Number -> value.toInt()
        else -> value.toString().toIntOrNull()
    }
}
