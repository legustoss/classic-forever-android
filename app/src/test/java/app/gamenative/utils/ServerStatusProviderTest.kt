package app.gamenative.utils

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerStatusProviderTest {

    @Test
    fun config_defaultsToNoProvider() {
        val config = ServerStatusConfig.parse(null)
        assertEquals(ServerStatusConfig.Source.NONE, config.source)
        assertEquals(ServerStatusConfig.DEFAULT_GUILD_ID, config.guildId)
        assertEquals("", config.url)
        assertTrue(ServerStatusFactory.create(config, OkHttpClient()) is NoOpProvider)
    }

    @Test
    fun config_parsesDiscordAndHttpSources() {
        val discord = ServerStatusConfig.parse(
            "${ServerStatusConfig.KEY_SOURCE}=discord\n" +
                "${ServerStatusConfig.KEY_GUILD_ID}=123456789\n",
        )
        assertEquals(ServerStatusConfig.Source.DISCORD, discord.source)
        assertEquals("123456789", discord.guildId)
        assertTrue(ServerStatusFactory.create(discord, OkHttpClient()) is DiscordWidgetProvider)

        val http = ServerStatusConfig.parse(
            "${ServerStatusConfig.KEY_SOURCE}=http\n" +
                "${ServerStatusConfig.KEY_URL}=https://example.com/api/status\n",
        )
        assertEquals(ServerStatusConfig.Source.HTTP, http.source)
        assertEquals("https://example.com/api/status", http.url)
        assertTrue(ServerStatusFactory.create(http, OkHttpClient()) is HttpStatusProvider)
    }

    @Test
    fun config_unknownSourceFallsBackToNone() {
        val config = ServerStatusConfig.parse("${ServerStatusConfig.KEY_SOURCE}=something")
        assertEquals(ServerStatusConfig.Source.NONE, config.source)
        assertTrue(ServerStatusFactory.create(config, OkHttpClient()) is NoOpProvider)
    }

    @Test
    fun noOpProvider_returnsUnknownWithoutCount() = runBlocking {
        val status = NoOpProvider.fetch()
        assertEquals(ServerStatusKind.UNKNOWN, status.kind)
        assertNull(status.count)
    }
}
