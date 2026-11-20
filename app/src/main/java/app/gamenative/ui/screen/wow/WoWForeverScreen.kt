package app.gamenative.ui.screen.wow

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import app.gamenative.PrefManager
import app.gamenative.R
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.window.layout.FoldingFeature
import app.gamenative.BuildConfig
import app.gamenative.PluviaApp
import app.gamenative.events.AndroidEvent
import app.gamenative.ui.screen.wow.WowClientDownloader.BUILD_INFO
import app.gamenative.ui.screen.wow.WowClientDownloader.EXE_NAME
import app.gamenative.ui.screen.wow.WowClientDownloader.FLAVOR_DIR
import app.gamenative.ui.screen.wow.WowClientDownloader.LEGACY_FLAVOR_DIR
import app.gamenative.ui.screen.wow.WowClientDownloader.LEGACY_PRODUCT
import app.gamenative.ui.screen.wow.WowClientDownloader.TARGET_PRODUCT
import app.gamenative.ui.theme.WowBackgroundGradient
import app.gamenative.ui.theme.WowBorder
import app.gamenative.ui.theme.WowNeon
import app.gamenative.ui.theme.WowNeonDim
import app.gamenative.ui.theme.WowPanel
import app.gamenative.ui.theme.WowPanelElevated
import app.gamenative.ui.theme.WowSubtle
import app.gamenative.ui.util.ScreenEffectsConfig
import app.gamenative.ui.util.SnackbarManager
import app.gamenative.utils.AppUpdater
import app.gamenative.utils.ClientCompatibility
import app.gamenative.utils.ClassicForeverPatcher
import app.gamenative.utils.ClientVersionChecker
import app.gamenative.utils.DeviceCompatibility
import app.gamenative.utils.DeviceCompatibilityResult
import app.gamenative.utils.DeviceStatus
import app.gamenative.utils.LocaleHelper
import app.gamenative.utils.Net
import app.gamenative.utils.ServerStatus
import app.gamenative.utils.ServerStatusConfig
import app.gamenative.utils.ServerStatusFactory
import app.gamenative.utils.ServerStatusKind
import app.gamenative.utils.StorageUtils
import app.gamenative.utils.renderReleaseNotes
import com.winlator.container.Container
import com.winlator.container.ContainerManager
import com.winlator.contents.ContentProfile
import com.winlator.contents.ContentsManager
import com.winlator.core.FileUtils
import com.winlator.core.GPUInformation
import com.winlator.core.TarCompressorUtils
import com.winlator.xenvironment.ImageFs
import com.winlator.xenvironment.ImageFsInstaller
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.IOException

object WoWLauncherState {
    var shouldAutoLaunch = true
}

private const val CONTAINER_ID = "wow_forever"


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun WoWForeverScreen(
    windowSizeClass: WindowSizeClass,
    foldingFeature: FoldingFeature? = null,
    onLaunch: (appId: String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var gamePath by remember { mutableStateOf(GamePath.load(context)) }
    val gpu = remember(gamePath) { GpuProfile.detect(context, File(gamePath)) }
    val wide = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    var files by remember { mutableStateOf(GamePath.Status()) }
    var isLaunching by remember { mutableStateOf(false) }
    var launchJob by remember { mutableStateOf<Job?>(null) }
    var hasSavedLogin by remember { mutableStateOf(BattleNetSignIn.load(context) != null) }
    var resolution by remember { mutableStateOf(RenderResolution.current(context)) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf(context.getString(R.string.wow_ready)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showStoragePermissionDialog by remember { mutableStateOf(false) }
    var versionStatus by remember { mutableStateOf<WowClientDownloader.VersionCheckResult?>(null) }
    var compatibility by remember { mutableStateOf<ClientCompatibility?>(null) }
    var serverStatus by remember { mutableStateOf(ServerStatus.UNKNOWN) }
    var isCheckingVersion by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }
    var updateStatusText by remember { mutableStateOf("") }
    var appUpdate by remember { mutableStateOf<AppUpdater.Release?>(null) }
    var isDownloadingAppUpdate by remember { mutableStateOf(false) }
    var appUpdateProgress by remember { mutableFloatStateOf(0f) }
    var showAppUpdateDialog by remember { mutableStateOf(false) }
    var showAppUpdatePermissionDialog by remember { mutableStateOf(false) }
    var pendingInstallPermission by remember { mutableStateOf(false) }
    var downloadedApk by remember { mutableStateOf<File?>(null) }
    var showUpgradeDialog by remember { mutableStateOf(false) }

    suspend fun fetchAppUpdate(): AppUpdater.Release? {
        val release = AppUpdater.check()
        appUpdate = release
        if (release != null) {
            showAppUpdateDialog = true
        }
        return release
    }

    fun checkAppUpdate() {
        scope.launch { fetchAppUpdate() }
    }

    fun installOrRequestPermission(apk: File) {
        if (AppUpdater.canInstall(context)) {
            AppUpdater.install(context, apk)
        } else {
            pendingInstallPermission = true
            showAppUpdatePermissionDialog = true
        }
    }

    fun performAppUpdate() {
        val target = appUpdate ?: return
        if (isDownloadingAppUpdate) return
        downloadedApk?.takeIf { it.exists() && it.length() == target.sizeBytes }?.let { apk ->
            installOrRequestPermission(apk)
            return
        }
        isDownloadingAppUpdate = true
        appUpdateProgress = 0f
        errorMessage = null
        scope.launch {
            try {
                val apk = AppUpdater.download(context, target) { progress ->
                    appUpdateProgress = progress
                }
                downloadedApk = apk
                isDownloadingAppUpdate = false
                installOrRequestPermission(apk)
            } catch (e: Exception) {
                Timber.e(e, "Error downloading app update")
                errorMessage = context.getString(R.string.wow_error_download, e.message)
                isDownloadingAppUpdate = false
            }
        }
    }

    suspend fun fetchVersionStatus() {
        isCheckingVersion = true
        withContext(Dispatchers.IO) {
            val root = File(gamePath)
            val status = WowClientDownloader.checkVersion(root, WowClientDownloader.regionFor(root))
            // A skipped remote version must not block launches; a newer remote version still will.
            versionStatus = if (status != null && status.remoteVersion == skippedUpdateVersion(context)) {
                status.copy(isOutdated = false)
            } else {
                status
            }
            compatibility = ClientVersionChecker.check(root)
        }
        isCheckingVersion = false
    }

    fun skipUpdate() {
        val version = versionStatus?.remoteVersion ?: return
        skipGameUpdate(context, version)
        versionStatus = versionStatus?.copy(isOutdated = false)
    }

    fun checkVersionStatus() {
        if (!File(gamePath, BUILD_INFO).exists()) return
        scope.launch { fetchVersionStatus() }
    }

    fun checkFiles(): Boolean {
        files = GamePath.status(context, gamePath)
        if (files.isWrongGame) {
            errorMessage = context.getString(
                R.string.wow_error_incompatible,
                files.products.joinToString(),
                TARGET_PRODUCT,
            )
        }
        return files.isReady
    }

    fun denyStorageAccess() {
        files = files.copy(hasStorageAccess = false)
        showStoragePermissionDialog = true
    }

    fun resetLaunch() {
        launchJob?.cancel()
        launchJob = null
        ClassicForeverPatcher.stop()
        isLaunching = false
        statusText = context.getString(R.string.wow_ready)
    }

    fun launchGame() {
        if (isLaunching || files.isWrongGame) return
        if (!files.hasStorageAccess) {
            showStoragePermissionDialog = true
            return
        }
        isLaunching = true
        errorMessage = null
        statusText = context.getString(R.string.wow_preparing)

        launchJob = scope.launch {
            try {
                val screenSize = resolveScreenSize(context, resolution)
                Timber.i("WoW screen size: $screenSize (resolution option: ${resolution.name})")
                val containerId = withContext(Dispatchers.IO) {
                    prepareLaunch(context, File(gamePath), gpu, screenSize) { msg ->
                        scope.launch(Dispatchers.Main) { statusText = msg }
                    }
                }
                statusText = context.getString(R.string.wow_booting)
                onLaunch(containerId)
                // Follows the game process and patches the private-realm key store once the client is up.
                ClassicForeverPatcher.start(File(gamePath))
            } catch (e: CancellationException) {
                throw e
            } catch (_: StorageAccessDeniedException) {
                isLaunching = false
                statusText = context.getString(R.string.wow_ready)
                denyStorageAccess()
            } catch (e: Exception) {
                Timber.e(e, "Error launching WoW Forever")
                errorMessage = e.message ?: context.getString(R.string.wow_launch_failed)
                statusText = context.getString(R.string.wow_launch_failed)
                isLaunching = false
            }
        }
    }

    LifecycleResumeEffect(gamePath) {
        // Storage access may have just been granted, which can reveal an install that was invisible before.
        if (!isUpdating && !isLaunching) gamePath = GamePath.load(context)
        checkFiles()
        isLaunching = false
        statusText = context.getString(R.string.wow_ready)
        BattleNetSignIn.removeLoginFile(File(gamePath))
        if (versionStatus == null && !WoWLauncherState.shouldAutoLaunch) {
            checkVersionStatus()
        }
        if (appUpdate == null && !WoWLauncherState.shouldAutoLaunch) {
            checkAppUpdate()
        }
        if (pendingInstallPermission && AppUpdater.canInstall(context)) {
            pendingInstallPermission = false
            downloadedApk?.takeIf { it.exists() }?.let { AppUpdater.install(context, it) }
        }
        onPauseOrDispose {
            launchJob?.cancel()
            launchJob = null
        }
    }

    DisposableEffect(Unit) {
        val reset: (Any) -> Unit = {
            resetLaunch()
            BattleNetSignIn.removeLoginFile(File(gamePath))
        }
        PluviaApp.events.on<AndroidEvent.ForceCloseApp, Unit>(reset)
        PluviaApp.events.on<AndroidEvent.GuestProgramTerminated, Unit>(reset)
        onDispose {
            PluviaApp.events.off<AndroidEvent.ForceCloseApp, Unit>(reset)
            PluviaApp.events.off<AndroidEvent.GuestProgramTerminated, Unit>(reset)
        }
    }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        val path = StorageUtils.getPathFromTreeUri(context, uri) ?: return@rememberLauncherForActivityResult
        val root = GamePath.findGameRoot(File(path)) ?: GamePath.installDirFor(File(path)).also { it.mkdirs() }
        errorMessage = null
        gamePath = root.absolutePath
        GamePath.save(context, gamePath)
        versionStatus = null
        compatibility = null
        val ok = checkFiles()
        if (!files.hasStorageAccess) {
            showStoragePermissionDialog = true
        } else if (ok) {
            checkVersionStatus()
        }
    }

    val addonPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        errorMessage = null
        scope.launch {
            try {
                val installed = withContext(Dispatchers.IO) {
                    val zip = File(context.cacheDir, "addon-import.zip")
                    try {
                        context.contentResolver.openInputStream(uri)?.use { input -> zip.outputStream().use { input.copyTo(it) } }
                            ?: throw IOException("Couldn't open the selected file")
                        AddonImporter.install(zip, File(gamePath, "$FLAVOR_DIR/Interface/AddOns"))
                    } finally {
                        zip.delete()
                    }
                }
                SnackbarManager.show(context.getString(R.string.wow_addon_installed, installed.joinToString()))
            } catch (e: Exception) {
                Timber.e(e, "Add-on import failed")
                if (e.isPermissionDenied()) denyStorageAccess()
                errorMessage = context.getString(R.string.wow_addon_failed, e.message)
            }
        }
    }

    val canPlay = !isLaunching && !isUpdating && !files.needsInstall && !files.isWrongGame

    val view = LocalView.current
    DisposableEffect(isUpdating) {
        view.keepScreenOn = isUpdating
        onDispose { view.keepScreenOn = false }
    }

    // Progress shows under the cards, which is below the fold on a landscape handheld.
    val scrollState = rememberScrollState()
    LaunchedEffect(isUpdating, isLaunching) {
        if (isUpdating || isLaunching) scrollState.animateScrollTo(scrollState.maxValue)
    }

    fun downloadGameFiles(failure: String, work: (File, (String) -> Unit) -> Unit, onSuccess: () -> Unit) {
        if (isUpdating || isLaunching) return
        if (!files.hasStorageAccess) {
            showStoragePermissionDialog = true
            return
        }
        isUpdating = true
        updateStatusText = context.getString(R.string.wow_connecting_cdn)
        errorMessage = null
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    work(File(gamePath)) { msg -> scope.launch(Dispatchers.Main) { updateStatusText = msg } }
                }
                isUpdating = false
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, failure)
                if (e.isPermissionDenied()) denyStorageAccess()
                errorMessage = "$failure: ${e.message}"
                isUpdating = false
            }
        }
    }

    fun performInstall() {
        showUpgradeDialog = false
        downloadGameFiles(context.getString(R.string.wow_install_failed), { root, onStatus ->
            WowClientDownloader.install(root, WowClientDownloader.regionFor(root), onStatus)
        }) {
            versionStatus = null
            compatibility = null
            if (checkFiles()) {
                checkVersionStatus()
                if (hasSavedLogin) launchGame()
            }
        }
    }

    fun performUpdate() {
        val target = versionStatus ?: return
        downloadGameFiles(context.getString(R.string.wow_update_failed), { root, onStatus -> WowClientDownloader.updateGame(root, target, onStatus) }) {
            checkFiles()
            clearSkippedUpdate(context)
            versionStatus = target.copy(isOutdated = false, localVersion = target.remoteVersion)
            compatibility = ClientVersionChecker.check(File(gamePath))
            launchGame()
        }
    }

    // Non-blocking: a missing or failing data source must never hold up the launcher.
    LaunchedEffect(gamePath) {
        val config = withContext(Dispatchers.IO) {
            val properties = File(gamePath, "classic-forever.properties")
            ServerStatusConfig.parse(if (properties.isFile) properties.readText() else null)
        }
        serverStatus = withContext(Dispatchers.IO) {
            ServerStatusFactory.create(config, Net.http).fetch()
        }
    }

    LaunchedEffect(gamePath) {
        val ready = checkFiles()
        // Every setup path reads or writes shared storage, so ask up front rather than after the user picks one.
        if (!files.hasStorageAccess) {
            showStoragePermissionDialog = true
        }
        Timber.i("WoWForeverScreen LaunchedEffect: gamePath=$gamePath, ready=$ready, hasSavedLogin=$hasSavedLogin, shouldAutoLaunch=${WoWLauncherState.shouldAutoLaunch}")
        when {
            !ready || !hasSavedLogin -> {
                WoWLauncherState.shouldAutoLaunch = false
                PluviaApp.events.emit(AndroidEvent.ClearBootingSplash)
                if (files.isLegacyBeta) showUpgradeDialog = true
                if (ready) {
                    checkVersionStatus()
                    checkAppUpdate()
                }
            }
            !WoWLauncherState.shouldAutoLaunch -> {
                checkVersionStatus()
                checkAppUpdate()
            }
            else -> {
                val updateJob = async { fetchAppUpdate() }
                fetchVersionStatus()
                val update = updateJob.await()
                Timber.i(
                    "WoWForeverScreen LaunchedEffect: isOutdated=${versionStatus?.isOutdated}, " +
                        "appUpdate=$update, compatibility=$compatibility",
                )
                WoWLauncherState.shouldAutoLaunch = false
                if (versionStatus?.isOutdated == true || update != null || compatibility is ClientCompatibility.TooOld) {
                    PluviaApp.events.emit(AndroidEvent.ClearBootingSplash)
                } else {
                    launchGame()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(WowBackgroundGradient))
    ) {
        LauncherLayout(
            windowSizeClass = windowSizeClass,
            foldingFeature = foldingFeature,
            header = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(if (wide) 2.dp else 8.dp))
                Text(
                    text = context.getString(R.string.wow_title),
                    fontSize = if (wide) 18.sp else 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = if (wide) 2.sp else 3.sp,
                    color = WowNeon,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = context.getString(R.string.wow_subtitle),
                    fontSize = if (wide) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = if (wide) 1.sp else 2.sp,
                    color = WowNeon,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(if (wide) 2.dp else 6.dp))
                Text(
                    text = context.getString(R.string.wow_edition, gpu.edition),
                    fontSize = if (wide) 9.sp else 11.sp,
                    color = WowSubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = context.getString(R.string.wow_check_app, BuildConfig.VERSION_NAME),
                    fontSize = if (wide) 9.sp else 10.sp,
                    color = WowSubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (gpu.compatibility.status != DeviceStatus.VALIDATED) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val warning = context.getString(
                        if (gpu.compatibility.status == DeviceStatus.UNKNOWN) {
                            R.string.wow_driver_unknown
                        } else {
                            R.string.wow_driver_pending
                        },
                    )
                    Text(
                        text = "${gpu.compatibility.gpuModel}: $warning",
                        fontSize = if (wide) 9.sp else 11.sp,
                        color = WowNeon,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            },
            status = {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neonGlow()
                    .border(1.dp, WowBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = WowPanel.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(if (wide) 12.dp else 20.dp)) {
                    SectionTitle(context.getString(R.string.wow_readiness), WowNeon)
                    Spacer(modifier = Modifier.height(if (wide) 8.dp else 14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(if (wide) 5.dp else 8.dp)) {
                        CheckItem(label = if (files.exeExists) context.getString(R.string.wow_check_arm64, EXE_NAME) else context.getString(R.string.wow_check_arm64_download), ready = files.exeExists, dense = wide)
                        CheckItem(label = context.getString(R.string.wow_check_data), ready = files.dataExists, dense = wide)
                        CheckItem(
                            label = when {
                                files.isWrongGame -> context.getString(R.string.wow_check_info_wrong, BUILD_INFO, TARGET_PRODUCT)
                                files.isLegacyBeta -> context.getString(R.string.wow_check_info_beta, BUILD_INFO)
                                else -> context.getString(R.string.wow_check_info, BUILD_INFO)
                            },
                            ready = files.hasForever,
                            dense = wide,
                        )
                        CheckItem(label = context.getString(R.string.wow_check_storage), ready = files.hasStorageAccess, dense = wide)
                        CheckItem(
                            label = if (hasSavedLogin) context.getString(R.string.wow_check_login_configured) else context.getString(R.string.wow_check_login_missing),
                            ready = hasSavedLogin,
                            dense = wide,
                        )
                        appUpdate?.let {
                            CheckItem(
                                label = context.getString(R.string.wow_check_app_update, BuildConfig.VERSION_NAME, it.version),
                                ready = false,
                                dense = wide,
                            )
                        } ?: CheckItem(
                            label = context.getString(R.string.wow_check_app, BuildConfig.VERSION_NAME),
                            ready = true,
                            dense = wide,
                        )
                        versionStatus?.let { version ->
                            CheckItem(
                                label = if (version.isOutdated) {
                                    context.getString(R.string.wow_check_client_update, version.localVersion, version.remoteVersion)
                                } else {
                                    context.getString(R.string.wow_check_client, version.localVersion)
                                },
                                ready = !version.isOutdated,
                                dense = wide,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = WowBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = context.getString(R.string.wow_target_path, gamePath), fontSize = 11.sp, color = WowSubtle, maxLines = 2)

                    longUptimeDays()?.let { days ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = context.getString(R.string.wow_uptime, days),
                            fontSize = 12.sp,
                            color = WowNeon,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = context.getString(R.string.wow_error_prefix, it), fontSize = 12.sp, color = WowNeon, fontWeight = FontWeight.Medium)
                    }
                }
            }
            },
            notices = {
            if (files.needsInstall && files.hasStorageAccess) {
                Spacer(modifier = Modifier.height(16.dp))
                InstallCard(
                    isUpgrade = files.isLegacyBeta,
                    gamePath = gamePath,
                    freeSpaceBytes = remember(gamePath, isUpdating) { GamePath.freeSpaceBytes(gamePath) },
                    enabled = !isUpdating && !isLaunching,
                    onChangeLocation = { folderPicker.launch(null) },
                )
            }

            compatibility?.let { state ->
                val message = when (state) {
                    is ClientCompatibility.TooOld -> context.getString(
                        R.string.wow_client_too_old,
                        state.build,
                        state.requirement.minBuild ?: state.build,
                    )
                    is ClientCompatibility.TooNew -> if (state.build == null) {
                        context.getString(R.string.wow_client_unknown)
                    } else {
                        context.getString(
                            R.string.wow_client_too_new,
                            state.build,
                            state.requirement.maxBuild ?: state.build,
                        )
                    }
                    is ClientCompatibility.Compatible -> null
                }
                if (message != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neonGlow()
                            .border(1.dp, WowNeon.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = WowPanelElevated.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = WowNeon, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                SectionTitle(context.getString(R.string.wow_game_update_title), WowNeon)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(message, fontSize = 12.sp, color = WowNeon, lineHeight = 16.sp)
                        }
                    }
                }
            }

            versionStatus?.takeIf { it.isOutdated && !files.needsInstall }?.let { version ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neonGlow()
                        .border(1.dp, WowNeon.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = WowPanelElevated.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WowNeon, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            SectionTitle(context.getString(R.string.wow_game_update_title), WowNeon)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = context.getString(R.string.wow_game_update_body, version.remoteVersion, version.localVersion),
                            fontSize = 12.sp,
                            color = WowNeon,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = context.getString(R.string.wow_game_update_hint),
                            fontSize = 11.sp,
                            color = WowNeonDim
                        )
                    }
                }
            }

            appUpdate?.let { update ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neonGlow()
                        .border(1.dp, WowNeon.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = WowPanel.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = WowNeon, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            SectionTitle(context.getString(R.string.wow_app_update_title, update.version), WowNeon)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = context.getString(R.string.wow_app_update_body),
                            fontSize = 12.sp,
                            color = WowNeonDim,
                            lineHeight = 16.sp,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PrimaryButton(dense = wide,
                            text = when {
                                isDownloadingAppUpdate -> context.getString(R.string.wow_downloading_update)
                                downloadedApk?.exists() == true -> context.getString(R.string.wow_install_update, update.version)
                                else -> context.getString(R.string.wow_update_app, update.version)
                            },
                            icon = Icons.Default.Refresh,
                            enabled = !isDownloadingAppUpdate && !isLaunching && !isUpdating,
                            onClick = {
                                if (downloadedApk?.exists() == true) {
                                    performAppUpdate()
                                } else {
                                    showAppUpdateDialog = true
                                }
                            },
                        )
                    }
                }
            }

            },
            actions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLaunching || isUpdating || (isCheckingVersion && WoWLauncherState.shouldAutoLaunch)) {
                    CircularProgressIndicator(color = WowNeon, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when {
                            isUpdating -> updateStatusText
                            isLaunching -> statusText
                            else -> context.getString(R.string.wow_checking_updates)
                        },
                        fontSize = 13.sp,
                        color = WowNeonDim,
                        textAlign = TextAlign.Center
                    )
                } else {
                    when {
                        !files.hasStorageAccess -> PrimaryButton(dense = wide,
                            text = context.getString(R.string.wow_grant_storage),
                            icon = Icons.Default.FolderOpen,
                            enabled = !isLaunching && !isUpdating,
                            onClick = { StorageUtils.requestManageExternalStoragePermission(context) },
                        )
                        files.needsInstall -> PrimaryButton(dense = wide,
                            text = if (files.isLegacyBeta) context.getString(R.string.wow_upgrade_release) else context.getString(R.string.wow_download_install),
                            icon = Icons.Default.Download,
                            enabled = !isLaunching && !isUpdating,
                            onClick = ::performInstall,
                        )
                        compatibility is ClientCompatibility.TooOld -> {
                            // Too old for this server: the only way forward is to update the client.
                            PrimaryButton(dense = wide,
                                text = context.getString(R.string.wow_update_game),
                                icon = Icons.Default.Refresh,
                                enabled = canPlay && versionStatus != null,
                                onClick = ::performUpdate,
                            )
                        }
                        versionStatus?.isOutdated == true && compatibility !is ClientCompatibility.TooOld -> {
                            PrimaryButton(dense = wide,
                                text = context.getString(R.string.wow_update_to, versionStatus?.remoteVersion),
                                icon = Icons.Default.Refresh,
                                enabled = canPlay,
                                onClick = ::performUpdate,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = ::launchGame,
                                enabled = canPlay,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(text = context.getString(R.string.wow_launch_outdated), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WowNeon)
                            }
                        }
                        else -> PrimaryButton(dense = wide,
                            text = if (files.isWrongGame) {
                                context.getString(R.string.wow_wrong_game)
                            } else if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                                context.getString(R.string.wow_play)
                            } else {
                                context.getString(R.string.wow_play_short)
                            },
                            icon = Icons.Default.PlayArrow,
                            enabled = canPlay,
                            onClick = ::launchGame,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showSettings = true },
                        enabled = !isLaunching && !isUpdating,
                        modifier = Modifier.fillMaxWidth().height(if (wide) 40.dp else 48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WowNeon),
                        border = BorderStroke(1.dp, WowNeon.copy(alpha = 0.6f)),
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(context.getString(R.string.wow_settings), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            },
            details = {
                CommunityCard(serverStatus, dense = wide)
            },
        )
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text(text = context.getString(R.string.wow_settings), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WowNeon) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle(context.getString(R.string.wow_section_display), WowNeon)
                    OptionMenu(
                        context.getString(R.string.wow_resolution, resolution.label(context)),
                        Icons.Default.AspectRatio,
                        RenderResolution.entries,
                        resolution,
                        { it.label(context) },
                        !isLaunching && !isUpdating,
                    ) {
                        RenderResolution.save(context, it)
                        resolution = it
                    }

                    val savedLanguage = PrefManager.appLanguage
                    OptionMenu(
                        context.getString(R.string.wow_language, LocaleHelper.getLanguageDisplayName(savedLanguage)),
                        Icons.Default.Public,
                        LocaleHelper.getSupportedLanguageCodes(),
                        savedLanguage,
                        { LocaleHelper.getLanguageDisplayName(it) },
                        !isLaunching && !isUpdating,
                    ) { code ->
                        PrefManager.appLanguage = code
                        generateSequence(context) { (it as? android.content.ContextWrapper)?.baseContext }
                            .filterIsInstance<Activity>()
                            .firstOrNull()
                            ?.recreate()
                    }

                    SectionTitle(context.getString(R.string.wow_section_files), WowNeon)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                        LinkButton(context.getString(R.string.wow_refresh), Icons.Default.Refresh) {
                            checkFiles()
                            checkVersionStatus()
                            checkAppUpdate()
                        }
                        LinkButton(context.getString(R.string.wow_location), Icons.Default.FolderOpen) { folderPicker.launch(null) }
                        LinkButton(context.getString(R.string.wow_addon), Icons.Default.Extension, enabled = !isLaunching && !isUpdating) {
                            addonPicker.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream"))
                        }
                    }

                    SectionTitle(context.getString(R.string.wow_group_account), WowSubtle)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                        LinkButton(context.getString(R.string.wow_password_login), Icons.Default.Key) {
                            showSettings = false
                            showLoginDialog = true
                        }
                        if (hasSavedLogin) {
                            LinkButton(context.getString(R.string.wow_forget_login), Icons.Default.Delete) {
                                BattleNetSignIn.forget(context)
                                BattleNetSignIn.removeLoginFile(File(gamePath))
                                hasSavedLogin = false
                            }
                        }
                    }

                    if (versionStatus?.isOutdated == true && compatibility !is ClientCompatibility.TooOld) {
                        SectionTitle(context.getString(R.string.wow_section_updates), WowSubtle)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                            LinkButton(context.getString(R.string.wow_launch_outdated), Icons.Default.PlayArrow) {
                                showSettings = false
                                launchGame()
                            }
                            LinkButton(context.getString(R.string.wow_skip_update), Icons.Default.Refresh) { skipUpdate() }
                        }
                    }

                    CommunityCard(serverStatus)
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text(context.getString(R.string.wow_cancel), color = WowNeon)
                }
            },
            containerColor = WowPanelElevated,
            shape = RoundedCornerShape(16.dp),
        )
    }

    if (showLoginDialog) {
        BattleNetCredentialDialog(
            onDismiss = { showLoginDialog = false },
            onConfirm = { login ->
                showLoginDialog = false
                BattleNetSignIn.save(context, login)
                hasSavedLogin = true
            },
        )
    }

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = { Text(text = context.getString(R.string.wow_upgrade_dialog_title), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WowNeon) },
            text = {
                Text(
                    text = context.getString(R.string.wow_upgrade_dialog_body, FLAVOR_DIR),
                    fontSize = 13.sp,
                    color = WowNeonDim,
                    lineHeight = 18.sp,
                )
            },
            confirmButton = {
                Button(
                    onClick = ::performInstall,
                    colors = ButtonDefaults.buttonColors(containerColor = WowNeon, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(context.getString(R.string.wow_upgrade_now), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showUpgradeDialog = false
                        folderPicker.launch(null)
                    }) {
                        Text(context.getString(R.string.wow_choose_folder), color = WowNeonDim)
                    }
                    TextButton(onClick = { showUpgradeDialog = false }) {
                        Text(context.getString(R.string.wow_later), color = WowNeonDim)
                    }
                }
            },
            containerColor = WowPanelElevated,
            shape = RoundedCornerShape(16.dp),
        )
    }

    if (showStoragePermissionDialog) {
        AlertDialog(
            onDismissRequest = { showStoragePermissionDialog = false },
            title = { Text(text = context.getString(R.string.wow_storage_title), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WowNeon) },
            text = {
                Text(
                    text = context.getString(R.string.wow_storage_body),
                    fontSize = 13.sp,
                    color = WowNeonDim,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStoragePermissionDialog = false
                        StorageUtils.requestManageExternalStoragePermission(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WowNeon, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(context.getString(R.string.wow_open_settings), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStoragePermissionDialog = false }) {
                    Text(context.getString(R.string.wow_cancel), color = WowNeonDim)
                }
            },
            containerColor = WowPanelElevated,
            shape = RoundedCornerShape(16.dp),
        )
    }

    if (showAppUpdateDialog) {
        appUpdate?.let { update ->
            AlertDialog(
                onDismissRequest = {
                    if (!isDownloadingAppUpdate) showAppUpdateDialog = false
                },
                title = {
                    Text(
                        text = context.getString(R.string.wow_app_update_dialog_title, update.version),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = WowNeon,
                    )
                },
                text = {
                    Column {
                        if (update.notes.isNotBlank()) {
                            Text(
                                text = renderReleaseNotes(update.notes),
                                fontSize = 12.sp,
                                color = WowNeonDim,
                                lineHeight = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .verticalScroll(rememberScrollState()),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        if (isDownloadingAppUpdate) {
                            Text(
                                text = context.getString(R.string.wow_downloading_percent, (appUpdateProgress * 100).toInt()),
                                fontSize = 12.sp,
                                color = WowNeon,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { appUpdateProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = WowNeon,
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = ::performAppUpdate,
                        enabled = !isDownloadingAppUpdate,
                        colors = ButtonDefaults.buttonColors(containerColor = WowNeon, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(
                            text = when {
                                isDownloadingAppUpdate -> context.getString(R.string.wow_downloading)
                                downloadedApk?.exists() == true -> context.getString(R.string.wow_install_now)
                                else -> context.getString(R.string.wow_update_now)
                            },
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                dismissButton = {
                    if (!isDownloadingAppUpdate) {
                        TextButton(onClick = { showAppUpdateDialog = false }) {
                            Text(context.getString(R.string.wow_later), color = WowNeonDim)
                        }
                    }
                },
                containerColor = WowPanelElevated,
                shape = RoundedCornerShape(16.dp),
            )
        }
    }

    if (showAppUpdatePermissionDialog) {
        AlertDialog(
            onDismissRequest = { showAppUpdatePermissionDialog = false },
            title = {
                Text(
                    text = context.getString(R.string.wow_install_perm_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = WowNeon,
                )
            },
            text = {
                Text(
                    text = context.getString(R.string.wow_install_perm_body),
                    fontSize = 13.sp,
                    color = WowNeonDim,
                    lineHeight = 18.sp,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAppUpdatePermissionDialog = false
                        pendingInstallPermission = true
                        AppUpdater.openInstallPermissionSettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WowNeon, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(context.getString(R.string.wow_open_settings), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppUpdatePermissionDialog = false }) {
                    Text(context.getString(R.string.wow_cancel), color = WowNeonDim)
                }
            },
            containerColor = WowPanelElevated,
            shape = RoundedCornerShape(16.dp),
        )
    }
}

private fun prefs(context: Context) = context.getSharedPreferences("wow_forever", Context.MODE_PRIVATE)

private const val KEY_SKIPPED_UPDATE_VERSION = "skipped_game_update_version"

private fun skippedUpdateVersion(context: Context): String? =
    prefs(context).getString(KEY_SKIPPED_UPDATE_VERSION, null)

private fun skipGameUpdate(context: Context, remoteVersion: String) {
    prefs(context).edit().putString(KEY_SKIPPED_UPDATE_VERSION, remoteVersion).apply()
}

private fun clearSkippedUpdate(context: Context) {
    prefs(context).edit().remove(KEY_SKIPPED_UPDATE_VERSION).apply()
}

/**
 * Bump when the display defaults change. Installs that were created by an older version keep their
 * resolution/RenderScale, but they get the new defaults exactly once — without touching any later
 * changes the player makes in-game.
 */
private const val DISPLAY_DEFAULTS_VERSION = 5
private const val KEY_DISPLAY_DEFAULTS_VERSION = "display_defaults_version"

private fun displayDefaultsNeedMigration(context: Context): Boolean =
    prefs(context).getInt(KEY_DISPLAY_DEFAULTS_VERSION, 0) != DISPLAY_DEFAULTS_VERSION

private fun markDisplayDefaultsMigrated(context: Context) {
    prefs(context).edit().putInt(KEY_DISPLAY_DEFAULTS_VERSION, DISPLAY_DEFAULTS_VERSION).apply()
}

/**
 * Wine's tick count follows the device's uptime, and once it passes 2^32 ms (about 49.7 days) WoW's frame time stops
 * advancing: the login UI never fades in and auto-login never starts (#36). A restart fixes it, so the launcher says so.
 */
internal fun longUptimeDays(uptimeMs: Long = SystemClock.elapsedRealtime()): Long? =
    if (uptimeMs >= UPTIME_WARNING_MS) uptimeMs / 86_400_000L else null

// A little under 2^32 ms, so the warning shows before the clock stops rather than after.
private const val UPTIME_WARNING_MS = 48L * 86_400_000L

/** Native uses the device's real screen size; presets keep the old 16:9 render budget. */
enum class RenderResolution(val size: String, private val labelRes: Int? = null) {
    NATIVE("native", R.string.wow_resolution_native),
    HD("1280x720"), HD_PLUS("1600x900"), FULL_HD("1920x1080");

    fun label(context: Context): String =
        labelRes?.let { context.getString(it) } ?: "${size.substringAfter('x')}p"

    companion object {
        private const val KEY_RESOLUTION = "resolution"

        /** New installs start at the device's native resolution. Saved choices always win. */
        fun current(context: Context): RenderResolution =
            prefs(context).getString(KEY_RESOLUTION, null)?.let { saved -> entries.firstOrNull { it.name == saved } }
                ?: NATIVE

        fun save(context: Context, resolution: RenderResolution) {
            prefs(context).edit().putString(KEY_RESOLUTION, resolution.name).apply()
        }
    }
}

object GamePath {
    private const val KEY_GAME_PATH = "game_path"

    private val defaultPath = File(Environment.getExternalStorageDirectory(), "WoW Forever")

    data class Status(
        val exeExists: Boolean = false,
        val dataExists: Boolean = false,
        val buildInfoExists: Boolean = false,
        val products: Set<String> = emptySet(),
        val hasStorageAccess: Boolean = true,
    ) {
        val hasForever get() = TARGET_PRODUCT in products
        val isLegacyBeta get() = !hasForever && LEGACY_PRODUCT in products
        val isWrongGame get() = buildInfoExists && products.isNotEmpty() && !hasForever && !isLegacyBeta
        val needsInstall get() = !isWrongGame && !(hasForever && dataExists)
        val isReady get() = exeExists && dataExists && hasForever && hasStorageAccess
    }

    fun status(context: Context, path: String): Status {
        val root = File(path)
        return Status(
            exeExists = File(root, "$FLAVOR_DIR/$EXE_NAME").exists(),
            dataExists = File(root, "Data").listFiles()?.isNotEmpty() == true,
            buildInfoExists = File(root, BUILD_INFO).isFile,
            products = productsOf(root),
            hasStorageAccess = StorageUtils.hasStoragePermission(context, path),
        )
    }

    /**
     * The folder the user picked, as long as it still exists. Otherwise the first default location (internal storage, then
     * each SD card) that holds an install, preferring the full release over the beta.
     */
    fun load(context: Context): String {
        val saved = prefs(context).getString(KEY_GAME_PATH, null)?.let(::File)
        if (saved != null && saved.isDirectory) return saved.absolutePath
        val defaults = listOf(defaultPath) + context.getExternalFilesDirs(null).drop(1).filterNotNull()
            .map { File(it.path.substringBefore("/Android/"), defaultPath.name) }
        val usable = defaults.firstOrNull { TARGET_PRODUCT in productsOf(it) } ?: defaults.firstOrNull { LEGACY_PRODUCT in productsOf(it) }
        return (usable ?: saved ?: defaultPath).absolutePath
    }

    /** Installs into [picked] if it's empty, or into a new "WoW Forever" folder inside it (e.g. an SD card's root). */
    fun installDirFor(picked: File): File =
        if (picked.listFiles().isNullOrEmpty()) picked else File(picked, defaultPath.name)

    fun save(context: Context, path: String) {
        prefs(context).edit().putString(KEY_GAME_PATH, path).apply()
    }

    fun isReady(context: Context, path: String = load(context)): Boolean =
        path.isNotEmpty() && status(context, path).isReady

    fun findGameRoot(picked: File): File? =
        sequenceOf(picked, picked.parentFile, File(picked, "WoW Forever"), File(picked, "World of Warcraft"))
            .filterNotNull()
            .firstOrNull { File(it, BUILD_INFO).isFile }

    fun freeSpaceBytes(path: String): Long =
        generateSequence(File(path)) { it.parentFile }.firstOrNull { it.exists() }?.usableSpace ?: 0L

    private fun productsOf(root: File) = WowClientDownloader.installedProducts(File(root, BUILD_INFO))
}

/**
 * Adaptive launcher shell. Breakpoints come from the available window (WindowSizeClass), never from
 * device type. Foldable hinges are kept clear when the feature is separating.
 */
@Composable
private fun LauncherLayout(
    windowSizeClass: WindowSizeClass,
    foldingFeature: FoldingFeature?,
    header: @Composable () -> Unit,
    status: @Composable () -> Unit,
    notices: @Composable () -> Unit,
    actions: @Composable () -> Unit,
    details: @Composable () -> Unit,
) {
    val hinge = if (foldingFeature?.isSeparating == true) {
        with(LocalDensity.current) { foldingFeature.bounds.width().toDp().coerceAtMost(64.dp) }
    } else {
        0.dp
    }
    val gutter = 16.dp + hinge

    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                header()
                status()
                notices()
                actions()
            }
        }
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        ) {
            header()
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(
                    modifier = Modifier.weight(1.15f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    status()
                    notices()
                }
                Spacer(modifier = Modifier.width(gutter))
                Column(modifier = Modifier.weight(0.85f)) {
                    Spacer(modifier = Modifier.weight(1f))
                    details()
                    Spacer(modifier = Modifier.weight(1f))
                    actions()
                }
            }
        }
    }
}

@Composable
private fun CommunityCard(status: ServerStatus, dense: Boolean = false) {
    val context = LocalContext.current
    fun open(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neonGlow()
            .border(1.dp, WowBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = WowPanel.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(if (dense) 12.dp else 16.dp)) {
            SectionTitle(context.getString(R.string.wow_community_title), WowNeon)
            Spacer(modifier = Modifier.height(if (dense) 8.dp else 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { open("http://classicforever.duckdns.org/") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WowNeon),
                    border = BorderStroke(1.dp, WowNeon.copy(alpha = 0.6f)),
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(context.getString(R.string.wow_website), fontSize = if (dense) 11.sp else 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { open("https://discord.gg/zBmq68jby") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WowNeon),
                    border = BorderStroke(1.dp, WowNeon.copy(alpha = 0.6f)),
                ) {
                    Text(context.getString(R.string.wow_discord), fontSize = if (dense) 11.sp else 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val label = when (status.kind) {
                ServerStatusKind.DISCORD_ONLINE -> context.getString(R.string.wow_status_discord_online)
                ServerStatusKind.IN_GAME_ONLINE -> context.getString(R.string.wow_status_ingame_online)
                ServerStatusKind.UNKNOWN -> context.getString(R.string.wow_status_unknown)
            }
            val text = if (status.kind != ServerStatusKind.UNKNOWN && status.count != null) {
                "$label: ${status.count}"
            } else {
                label
            }
            Text(text = text, fontSize = 11.sp, color = WowSubtle)
        }
    }
}

@Composable
private fun InstallCard(isUpgrade: Boolean, gamePath: String, freeSpaceBytes: Long, enabled: Boolean, onChangeLocation: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neonGlow()
            .border(1.dp, WowNeon.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = WowPanel.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, contentDescription = null, tint = WowNeon, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                SectionTitle(if (isUpgrade) context.getString(R.string.wow_install_card_upgrade) else context.getString(R.string.wow_install_card_install), WowNeon)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isUpgrade) {
                    context.getString(R.string.wow_install_card_upgrade_body, FLAVOR_DIR)
                } else {
                    context.getString(R.string.wow_install_card_body)
                },
                fontSize = 12.sp,
                color = WowNeonDim,
                lineHeight = 16.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = context.getString(R.string.wow_install_location, gamePath, "%.1f".format(freeSpaceBytes / 1e9)),
                fontSize = 11.sp,
                color = WowSubtle,
            )
            if (!isUpgrade) {
                LinkButton(context.getString(R.string.wow_change_location), Icons.Default.FolderOpen, enabled = enabled, onClick = onChangeLocation)
                Text(
                    text = context.getString(R.string.wow_pc_hint),
                    fontSize = 11.sp,
                    color = WowSubtle,
                )
            }
        }
    }
}

/** A soft white halo behind panels and buttons — neon glow without blur APIs or extra drawables. */
private fun Modifier.neonGlow(): Modifier = this.drawBehind {
    for (layer in 6 downTo 1) {
        val spread = layer * 2f
        drawRoundRect(
            color = WowNeon.copy(alpha = 0.035f),
            topLeft = Offset(-spread, -spread),
            size = Size(size.width + spread * 2f, size.height + spread * 2f),
            cornerRadius = CornerRadius((16f + spread).dp.toPx()),
        )
    }
}

@Composable
private fun SectionTitle(text: String, color: Color) {
    Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = color)
}

@Composable
private fun PrimaryButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    dense: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(if (dense) 46.dp else 56.dp).neonGlow(),
        colors = ButtonDefaults.buttonColors(containerColor = WowNeon, contentColor = Color.Black),
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(if (dense) 20.dp else 24.dp))
        Spacer(modifier = Modifier.width(if (dense) 8.dp else 10.dp))
        Text(
            text = text,
            fontSize = if (dense) 13.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = if (dense) 0.sp else 1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LinkButton(text: String, icon: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = WowNeonDim)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = WowNeonDim, maxLines = 1)
    }
}

@Composable
private fun <T> OptionMenu(
    text: String,
    icon: ImageVector,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    enabled: Boolean,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        LinkButton(text, icon, enabled) { expanded = true }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    trailingIcon = { if (option == selected) Icon(Icons.Default.Check, contentDescription = null, tint = WowNeon) },
                    onClick = {
                        expanded = false
                        if (option != selected) onSelect(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun CheckItem(label: String, ready: Boolean, dense: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (ready) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (ready) WowNeon else WowSubtle,
            modifier = Modifier.size(if (dense) 15.dp else 18.dp)
        )
        Spacer(modifier = Modifier.width(if (dense) 6.dp else 10.dp))
        Text(
            text = label,
            fontSize = if (dense) 11.sp else 13.sp,
            color = if (ready) WowNeonDim else WowSubtle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private class StorageAccessDeniedException(cause: Throwable) : Exception(cause)

private fun Throwable.isPermissionDenied() =
    message?.let { "EPERM" in it || "Operation not permitted" in it } == true

private const val DEFAULT_DRIVER = DeviceCompatibility.DRIVER_740
private const val ADRENO_8XX_DRIVER = DeviceCompatibility.DRIVER_8XX
private const val PROTON_VERSION = "proton-11.0-90624-arm64ec"
private const val DXVK_VERSION = "2.4.1-wow-aarch64-test"

private data class GpuProfile(
    val driver: String,
    val tuDebug: String,
    val screenSize: String,
    val label: String,
    val edition: String,
    val compatibility: DeviceCompatibilityResult,
) {
    companion object {
        fun detect(context: Context, gameRoot: File): GpuProfile {
            val result = DeviceCompatibility.detect(context, gameRoot)
            val is8xx = result.driverId == DeviceCompatibility.DRIVER_8XX
            return GpuProfile(
                driver = result.driverId,
                tuDebug = if (is8xx) "noconform,sysmem" else "noconform",
                screenSize = if (is8xx) "1280x720" else "1920x1080",
                label = result.gpuModel,
                edition = if (is8xx) "Snapdragon 8 Elite / Adreno 8xx" else "Snapdragon 8 Series / Adreno 7xx",
                compatibility = result,
            )
        }
    }
}

/**
 * Reshapes the GPU's 16:9 render budget to the current window's aspect ratio so the game fills
 * non-16:9 screens (e.g. a foldable's near-square inner display) instead of being letterboxed.
 * The pixel count stays the same as [baseSize], so GPU cost matches the 16:9 default.
 */
private fun fitScreenSizeToWindow(context: Context, baseSize: String): String {
    val (windowWidth, windowHeight) = windowSize(context)
    return fitScreenSize(baseSize, windowWidth, windowHeight)
}

/**
 * Full display resolution in landscape orientation, rounded to an even size.
 *
 * `currentWindowMetrics` can report the launcher's portrait window (or a letterboxed window), which
 * used to give the game a portrait desktop like 1280x2772. The game always runs in landscape, so
 * take the maximum display metrics and put the long edge first.
 */
internal fun nativeScreenSize(pixelWidth: Int, pixelHeight: Int): String {
    if (pixelWidth <= 0 || pixelHeight <= 0) return "1920x1080"
    val width = maxOf(pixelWidth, pixelHeight)
    val height = minOf(pixelWidth, pixelHeight)
    return "${(width / 2f).roundToInt() * 2}x${(height / 2f).roundToInt() * 2}"
}

private fun nativeScreenSize(context: Context): String {
    val (width, height) = windowSize(context)
    return nativeScreenSize(width, height)
}

private fun resolveScreenSize(context: Context, resolution: RenderResolution): String =
    if (resolution == RenderResolution.NATIVE) nativeScreenSize(context)
    else fitScreenSizeToWindow(context, resolution.size)

/**
 * Physical display pixels. `getRealMetrics()` includes the cutout area, unlike window metrics,
 * which can report a smaller safe area and make the renderer letterbox the game.
 */
private fun windowSize(context: Context): Pair<Int, Int> {
    val wm = context.getSystemService(WindowManager::class.java)
    @Suppress("DEPRECATION")
    val metrics = android.util.DisplayMetrics()
    wm.defaultDisplay.getRealMetrics(metrics)
    if (metrics.widthPixels > 0 && metrics.heightPixels > 0) {
        return metrics.widthPixels to metrics.heightPixels
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        wm.maximumWindowMetrics.bounds.let { it.width() to it.height() }
    } else {
        context.resources.displayMetrics.let { it.widthPixels to it.heightPixels }
    }
}

internal fun fitScreenSize(baseSize: String, windowWidth: Int, windowHeight: Int): String {
    if (windowWidth <= 0 || windowHeight <= 0) return baseSize
    val (baseWidth, baseHeight) = baseSize.split("x").map { it.toInt() }
    val aspect = maxOf(windowWidth, windowHeight).toFloat() / minOf(windowWidth, windowHeight)
    val height = sqrt(baseWidth * baseHeight / aspect)
    val width = height * aspect
    return "${(width / 2f).roundToInt() * 2}x${(height / 2f).roundToInt() * 2}"
}

private fun prepareLaunch(context: Context, gameRoot: File, gpu: GpuProfile, screenSize: String, onStatus: (String) -> Unit): String {
    installBundledComponents(context, onStatus)

    check(File(gameRoot, BUILD_INFO).exists()) { "Missing $BUILD_INFO in $gameRoot. Copy it from your WoW install." }
    val migrateDisplay = displayDefaultsNeedMigration(context)
    try {
        ensureGameConfig(gameRoot, screenSize, forceDisplayDefaults = migrateDisplay)
        ClassicForeverPatcher.configure(gameRoot)
        if (migrateDisplay) {
            applyDisplayDefaultsToActiveConfig(gameRoot, screenSize)
            markDisplayDefaultsMigrated(context)
        }
        val activeConfig = File(gameRoot, "$FLAVOR_DIR/WTF/${ClassicForeverPatcher.CONFIG_NAME}")
        if (activeConfig.isFile) {
            Timber.i(
                "Active game display config: " + activeConfig.readLines()
                    .filter { it.startsWith("SET gxFullscreen ") || it.startsWith("SET gxFullscreenResolution ") ||
                        it.startsWith("SET gxWindowedResolution ") || it.startsWith("SET RenderScale ") }
                    .joinToString(" | "),
            )
        }
    } catch (e: Exception) {
        if (e.isPermissionDenied()) throw StorageAccessDeniedException(e)
        throw e
    }

    val arm64Exe = File(gameRoot, "$FLAVOR_DIR/$EXE_NAME")
    if (!arm64Exe.exists()) {
        try {
            WowClientDownloader.download(gameRoot, WowClientDownloader.regionFor(gameRoot), onStatus)
        } catch (e: Exception) {
            if (!arm64Exe.exists()) throw IllegalStateException("Couldn't download $EXE_NAME: ${e.message}", e)
            Timber.w(e, "Client update check failed, launching the existing $EXE_NAME")
        }
    }

    onStatus("Configuring ${gpu.label} container...")
    val containerManager = ContainerManager(context)
    val config = containerConfig(gameRoot, gpu, screenSize, WowMachineIdentity.load(context))
    val container = containerManager.getContainerById(CONTAINER_ID)
    if (container == null) {
        onStatus("Creating prefix environment (first boot)...")
        containerManager.createContainer(CONTAINER_ID, config)
    } else {
        container.loadData(config)
        container.saveData()
    }
    check(containerManager.hasContainer(CONTAINER_ID)) { "Container creation failed. Check system storage and logs." }
    // Fill the whole surface even if the display reports a slightly different aspect than the X server.
    containerManager.getContainerById(CONTAINER_ID)?.let { active ->
        active.putExtra(ScreenEffectsConfig.KEY_SCALING_MODE, ScreenEffectsConfig.SCALING_MODE_STRETCH.toString())
        active.saveData()
    }
    // Written last, so the password isn't on disk during the client download or container setup.
    BattleNetSignIn.load(context)?.let { login ->
        BattleNetSignIn.writeLoginFile(gameRoot, login)
        BattleNetSignIn.startLoginWatcher(gameRoot)
    }
    return CONTAINER_ID
}

private fun containerConfig(gameRoot: File, gpu: GpuProfile, screenSize: String, identity: WowMachineIdentity): JSONObject {
    val samsungEnv = if (Build.MANUFACTURER.equals("samsung", ignoreCase = true)) " FD_DEV_FEATURES=enable_tp_ubwc_flag_hint=1" else ""
    val envVars = "WRAPPER_MAX_IMAGE_COUNT=0 ZINK_DESCRIPTORS=lazy ZINK_DEBUG=compact,deck_emu MESA_SHADER_CACHE_DISABLE=false MESA_SHADER_CACHE_MAX_SIZE=1024MB mesa_glthread=true WINEESYNC=0 MESA_VK_WSI_PRESENT_MODE=mailbox TU_DEBUG=${gpu.tuDebug} VKD3D_SHADER_MODEL=6_0 PULSE_LATENCY_MSEC=144 ${WowMachineIdentity.HOSTNAME_ENV}=${identity.computerName}$samsungEnv"
    return JSONObject().apply {
        put("id", CONTAINER_ID)
        put("name", "WoW Forever")
        put("screenSize", screenSize)
        put("envVars", envVars)
        put("graphicsDriver", "Wrapper")
        put("graphicsDriverVersion", gpu.driver)
        put("graphicsDriverConfig", "version=${gpu.driver},adrenotoolsTurnip=1,resourceType=buffer,bcnEmulation=auto,quality=high")
        put("displayRenderer", "vulkan")
        put("dxwrapper", "dxvk-$DXVK_VERSION-1")
        // Async shader compilation keeps first-time zone visits from stuttering; the async pipeline
        // cache avoids recompiling the same shaders on the next launch. syncInterval=0 matches
        // gxVSync=0 in the game config; maxFrameLatency keeps only a short queue of presented frames.
        put("dxwrapperConfig", "version=$DXVK_VERSION-1,async=1,asyncCache=1,syncInterval=0,maxFrameLatency=2")
        put("wineVersion", "$PROTON_VERSION-1")
        put("containerVariant", "bionic")
        put("fexcoreVersion", "2609-0")
        put("drives", "D:/storage/emulated/0/DownloadE:/data/data/app.wowforever/storageG:${gameRoot.path}")
        put("executablePath", "G:\\$FLAVOR_DIR\\$EXE_NAME")
        put("execArgs", "-d3d11 ${ClassicForeverPatcher.LAUNCH_ARGS}")
        put("showFPS", true)
        // Keep running in the background: a suspended client drops its Battle.net connection, e.g. while the
        // player fetches an authenticator code from another app.
        put("suspendPolicy", Container.SUSPEND_POLICY_NEVER)
        put("startupSelection", Container.STARTUP_SELECTION_AGGRESSIVE.toInt())
        put("wow64Mode", true)
        put("wincomponents", "direct3d=0,directsound=0,directinput8=0,directinput=0,directmusic=0,directshow=0,directplay=0,vcrun2010=0,wmdecoder=0,opengl=0")
    }
}

private val CONFIG_DEFAULTS = linkedMapOf(
    "agentUID" to "\"$TARGET_PRODUCT\"",
    "gxApi" to "\"D3D11\"",
    "gxFullscreen" to "\"1\"",
    "gxVSync" to "\"0\"",
    "textLocale" to "\"enUS\"",
    "audioLocale" to "\"enUS\"",
    "gxMaximize" to "\"1\"",
    "graphicsQuality" to "\"0\"",
    "ResampleQuality" to "\"0\"",
    "RenderScale" to "\"0.5\"",
    "farclip" to "\"1200\"",
    "horizonClip" to "\"1200\"",
    "RAIDfarclip" to "\"1200\"",
    "RAIDhorizonClip" to "\"1200\"",
    "shadowMode" to "\"0\"",
    "graphicsShadowQuality" to "\"0\"",
    "raidGraphicsShadowQuality" to "\"0\"",
    "worldBaseMip" to "\"0\"",
    "RAIDworldBaseMip" to "\"0\"",
    "graphicsTextureResolution" to "\"2\"",
    "raidGraphicsTextureResolution" to "\"2\"",
    "componentTextureLevel" to "\"0\"",
    "RAIDcomponentTextureLevel" to "\"0\"",
    "entityShadowFadeScale" to "\"0\"",
    "refraction" to "\"0\"",
    "groundEffectDensity" to "\"16\"",
    "GamePadEnable" to "\"1\"",
    "InputDeviceInterfaceStyle" to "\"1\"",
    // Answers the first-run Screen Narrator prompt, which otherwise blocks login.txt auto-login.
    "showScreenNarrationDialog" to "\"0\"",
    "accessibilityScreenNarrationEnabled" to "\"0\"",
)

internal fun ensureGameConfig(root: File, screenSize: String, forceDisplayDefaults: Boolean = false) {
    val flavorDir = File(root, FLAVOR_DIR)
    flavorDir.mkdirs()
    val flavorInfo = File(flavorDir, ".flavor.info")
    if (!flavorInfo.exists()) {
        flavorInfo.writeText("Product Flavor!STRING:0\n$TARGET_PRODUCT\n")
    }
    // Resolution and RenderScale are seeded only when the config is first created: the game starts at the
    // device's native resolution with 50% render scale, and anything the player changes later stays changed.
    // gxApi is still normalised (DXVK needs D3D11). The portal is set by the private-realm patcher in
    // BetaSuspendedTest.wtf, which is the config the client actually loads, so in-game edits stay untouched.
    val configWtf = File(root, CONFIG_WTF)
    val firstRun = !configWtf.exists()
    val betaConfigWtf = File(root, "$LEGACY_FLAVOR_DIR/WTF/Config.wtf")
    if (!configWtf.exists() && betaConfigWtf.isFile) {
        // Carry the player's settings over from the beta, minus the lines that pointed it at the beta servers.
        configWtf.parentFile?.mkdirs()
        val betaOnly = listOf("SET portal ", "SET agentUID ", "SET CACHE-")
        val carried = betaConfigWtf.readLines().filterNot { line -> betaOnly.any { line.startsWith(it, ignoreCase = true) } }
        configWtf.writeText((carried + "SET agentUID ${CONFIG_DEFAULTS.getValue("agentUID")}").joinToString("\n") + "\n")
    }
    if (!configWtf.exists()) {
        configWtf.parentFile?.mkdirs()
        configWtf.writeText(CONFIG_DEFAULTS.entries.joinToString("\n") { "SET ${it.key} ${it.value}" } + "\n")
    }

    if (firstRun || forceDisplayDefaults) {
        configWtf.writeText(displayDefaults(configWtf.readLines(), screenSize).joinToString("\n") + "\n")
    }

    val forced = mapOf("gxApi" to CONFIG_DEFAULTS.getValue("gxApi"))
    val forcedLines = forced.map { (key, value) -> "SET $key $value" }
    val original = configWtf.readLines()
    if (!original.containsAll(forcedLines)) {
        val kept = original.filterNot { line -> forced.keys.any { line.startsWith("SET $it ", ignoreCase = true) } }
        configWtf.writeText((kept + forcedLines).joinToString("\n") + "\n")
    }
}

private val RESOLUTION_KEYS = listOf("gxFullscreenResolution", "gxWindowedResolution")

/** Replaces only the display defaults, leaving every other player setting untouched. */
private fun displayDefaults(lines: List<String>, screenSize: String): List<String> {
    val keys = RESOLUTION_KEYS + listOf("RenderScale", "gxFullscreen", "gxVSync")
    val kept = lines.filterNot { line -> keys.any { line.startsWith("SET $it ", ignoreCase = true) } }
    return kept + RESOLUTION_KEYS.map { "SET $it \"$screenSize\"" } +
        "SET RenderScale \"0.5\"" + "SET gxFullscreen \"1\"" + "SET gxVSync \"0\""
}

/** The client actually loads BetaSuspendedTest.wtf, so a one-time migration must update it too. */
private fun applyDisplayDefaultsToActiveConfig(root: File, screenSize: String) {
    val active = File(root, "$FLAVOR_DIR/WTF/${ClassicForeverPatcher.CONFIG_NAME}")
    if (!active.isFile) return
    active.writeText(displayDefaults(active.readLines(), screenSize).joinToString("\n") + "\n")
}

private const val CONFIG_WTF = "$FLAVOR_DIR/WTF/Config.wtf"

private fun installBundledComponents(context: Context, onStatus: (String) -> Unit) {
    installDriver(context, DEFAULT_DRIVER, "turnip-wow-scheduler-test.zip", onStatus)
    installDriver(context, ADRENO_8XX_DRIVER, "turnip-V32-RP6sched.zip", onStatus)

    val protonDir = File(ImageFs.getSharedProtonDir(context), PROTON_VERSION)
    if (!File(protonDir, "profile.json").exists() || !File(protonDir, "bin/wine").exists()) {
        onStatus("Extracting Proton 11 ARM64EC (takes ~10s)...")
        check(extractBundledWcp(context, "$PROTON_VERSION.wcp", protonDir)) { "Failed to extract Proton or graphics components." }
        File(protonDir, "bin").listFiles()?.forEach {
            it.setExecutable(true, false)
            it.setReadable(true, false)
        }
    }

    val dxvkDir = File(ContentsManager.getContentTypeDir(context, ContentProfile.ContentType.CONTENT_TYPE_DXVK), DXVK_VERSION)
    if (!File(dxvkDir, "profile.json").exists()) {
        onStatus("Extracting DXVK ARM64...")
        if (!extractBundledWcp(context, "dxvk-$DXVK_VERSION.wcp", dxvkDir)) Timber.e("Failed to extract DXVK")
    }

    ImageFsInstaller.ensureBionicLib(context, ImageFs.find(context).rootDir)
    ContentsManager(context).syncContents()
}

private fun installDriver(context: Context, driverName: String, assetZip: String, onStatus: (String) -> Unit) {
    val driverDir = File(context.filesDir, "contents/adrenotools/$driverName")
    if (File(driverDir, "meta.json").exists()) return
    onStatus("Installing $driverName driver...")
    driverDir.mkdirs()
    if (!FileUtils.extractZipFromAssets(context, "bundled_components/$assetZip", driverDir)) {
        Timber.e("Error extracting driver $driverName from $assetZip")
    }
}

private fun extractBundledWcp(context: Context, assetName: String, destination: File): Boolean {
    destination.mkdirs()
    return TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, context, "bundled_components/$assetName", destination)
}
