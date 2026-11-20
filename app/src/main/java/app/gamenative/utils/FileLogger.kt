package app.gamenative.utils

import android.util.Log
import app.gamenative.BuildConfig
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Plain-text logs under `<game root>/logs/`, next to `Data/`. This is the same place the Linux
 * script used (`Logs/launcher-linux.log`), so the patcher can be inspected without adb.
 *
 * - `classic-forever.log` — patcher status, one line per event.
 * - `app.log` — everything logged through Timber by the app (release builds keep INFO and above).
 *
 * Each file is capped at 2 MB and rotates into a single `.old` backup. Logging failures are
 * swallowed: diagnostics must never break the launcher or the game.
 */
object FileLogger {
    private const val PATCHER_FILE = "classic-forever.log"
    private const val APP_FILE = "app.log"
    private const val MAX_FILE_BYTES = 2L * 1024 * 1024
    private val lock = Any()
    private val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    @Volatile
    private var logDir: File? = null

    val directory: File?
        get() = logDir

    /** Points logging at the current game folder. Safe to call again after the user changes it. */
    fun init(gameRoot: File) {
        val dir = File(gameRoot, "logs")
        synchronized(lock) {
            logDir = dir
            try {
                dir.mkdirs()
                append(PATCHER_FILE, "----- session ${stamp.format(Date())} -----")
            } catch (_: IOException) {
                // No storage access yet (or SD card not writable): keep logging to logcat only.
            }
        }
    }

    fun log(tag: String, message: String, throwable: Throwable? = null) {
        synchronized(lock) {
            try {
                append(PATCHER_FILE, "${stamp.format(Date())}  [$tag]  $message")
                if (throwable != null) append(PATCHER_FILE, throwable.stackTraceToString())
            } catch (_: IOException) {
            }
        }
    }

    fun appLog(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        synchronized(lock) {
            try {
                val level = when (priority) {
                    Log.VERBOSE -> "V"
                    Log.DEBUG -> "D"
                    Log.INFO -> "I"
                    Log.WARN -> "W"
                    Log.ERROR -> "E"
                    Log.ASSERT -> "A"
                    else -> "?"
                }
                val prefix = if (tag.isNullOrBlank()) "" else "[$tag] "
                append(APP_FILE, "${stamp.format(Date())}  $level  $prefix$message")
                if (throwable != null) append(APP_FILE, throwable.stackTraceToString())
            } catch (_: IOException) {
            }
        }
    }

    private fun append(name: String, line: String) {
        val dir = logDir ?: return
        val file = File(dir, name)
        if (file.exists() && file.length() > MAX_FILE_BYTES) {
            val backup = File(dir, "$name.old")
            backup.delete()
            file.renameTo(backup)
        }
        file.appendText(line + "\n")
    }
}

/** Timber tree that mirrors app logs into `logs/app.log`. */
class FileLogTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean =
        BuildConfig.DEBUG || priority >= Log.INFO

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        FileLogger.appLog(priority, tag, message, t)
    }
}
