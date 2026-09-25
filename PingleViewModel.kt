package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PingleDatabase
import com.example.data.ScoreEntity
import com.example.data.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.R

enum class Screen {
    HOME, PLAY, HIGH_SCORES, OPTIONS, MANUAL, PINGUI_SETUP, PINGUI_GAME_EDIT
}

class PingleViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PingleDatabase.getDatabase(application)
    private val repository = ScoreRepository(database.scoreDao())
    private val prefs = application.getSharedPreferences("pingle_prefs", Context.MODE_PRIVATE)

    val currentScreen = MutableStateFlow(Screen.HOME)

    val topScores: StateFlow<List<ScoreEntity>> = repository.topScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pingleSpeed = MutableStateFlow(prefs.getFloat("pingle_speed", 1.0f))
    val pingleFriction = MutableStateFlow(prefs.getFloat("pingle_friction", 15.0f))
    val isManualUnlocked = MutableStateFlow(true)
    val totalSpinDuration = MutableStateFlow(prefs.getLong("total_spin_duration", 0L))
    val pingleTint = MutableStateFlow(prefs.getString("pingle_tint", "none") ?: "none")
    val pingleCustomColor = MutableStateFlow(prefs.getInt("pingle_custom_color", 0xFF00FFCC.toInt()))
    val pingleFoldAngleThreshold = MutableStateFlow(prefs.getFloat("pingle_fold_angle_threshold", 120.0f))
    val pingleCustomImageUri = MutableStateFlow(prefs.getString("pingle_custom_image_uri", null))
    val useCustomImage = MutableStateFlow(prefs.getBoolean("use_custom_image", false))
    val customMediaType = MutableStateFlow(prefs.getString("custom_media_type", "image") ?: "image")

    val loadEverything = MutableStateFlow(prefs.getBoolean("load_everything", false))

    val isActivated = MutableStateFlow(prefs.getBoolean("is_activated", false))
    val activationKey = MutableStateFlow<String?>(prefs.getString("activation_key", null))
    val activationTimestamp = MutableStateFlow<Long>(prefs.getLong("activation_timestamp", 0L))
    val activeKeyPrefix = MutableStateFlow<String?>(prefs.getString("active_key_prefix", null))

    val isV2xUnlocked = MutableStateFlow(false)
    val isTrialActive = MutableStateFlow(false)
    val isDevKeyUnlocked = MutableStateFlow(prefs.getBoolean("debug_unlocked", false))
    val isPlusEEUnlocked = MutableStateFlow(false)
    val isPlusMSUnlocked = MutableStateFlow(false)
    val trialRemainingMs = MutableStateFlow(0L)

    // Key file checking states
    val isDeviceKilled = MutableStateFlow(prefs.getBoolean("device_permanently_killed", false))
    val dbonFound = MutableStateFlow(prefs.getBoolean("dbon_found", false))
    val x86Found = MutableStateFlow(prefs.getBoolean("x86_found", false))
    val bootHasX86Key = MutableStateFlow(false)

    var preloadedPurePringle: ImageBitmap? = null
    var preloadedSinglePringle: ImageBitmap? = null
    var preloadedPringle: ImageBitmap? = null
    var preloadedAppIcon: ImageBitmap? = null



    fun findKeyFileEverywhere(): java.io.File? {
        val externalDir = android.os.Environment.getExternalStorageDirectory() ?: return null
        
        // 1. Check standard path first to be super fast
        val standardDir = java.io.File(externalDir, "psdevcom")
        val standardFile = java.io.File(standardDir, "key.txt")
        if (standardFile.exists()) {
            return standardFile
        }

        // 2. Otherwise, recursively search up to 5 levels deep to find any folder named "psdevcom" or file named "key.txt"
        val queue = java.util.ArrayDeque<java.io.File>()
        queue.add(externalDir)
        val depthMap = mutableMapOf<java.io.File, Int>()
        depthMap[externalDir] = 0

        var fallbackKeyFile: java.io.File? = null
        var scannedDirs = 0
        val maxDirs = 2000

        while (queue.isNotEmpty() && scannedDirs < maxDirs) {
            val dir = queue.poll() ?: break
            val depth = depthMap[dir] ?: 0
            if (depth > 5) continue

            scannedDirs++
            val files = dir.listFiles() ?: continue
            for (file in files) {
                if (file.isDirectory) {
                    val name = file.name
                    if (name.equals("psdevcom", ignoreCase = true)) {
                        val keyInDir = java.io.File(file, "key.txt")
                        if (keyInDir.exists()) {
                            return keyInDir
                        }
                    }
                    if (!name.startsWith(".") && !name.equals("Android", ignoreCase = true) && !name.equals("self", ignoreCase = true)) {
                        queue.add(file)
                        depthMap[file] = depth + 1
                    }
                } else {
                    if (file.name.equals("key.txt", ignoreCase = true)) {
                        fallbackKeyFile = file
                    }
                }
            }
        }
        return fallbackKeyFile
    }

    fun checkKeyFile(context: Context) {
        if (prefs.getBoolean("device_permanently_killed", false)) {
            isDeviceKilled.value = true
            return
        }

        val keyFile = findKeyFileEverywhere()

        if (keyFile != null && keyFile.exists()) {
            try {
                val content = keyFile.readText()

                // 1. Check for killsave
                if (content.contains("killsave")) {
                    prefs.edit().putBoolean("device_permanently_killed", true).apply()
                    isDeviceKilled.value = true
                    return
                }

                // 2. Check for dbon
                if (content.contains("dbon")) {
                    dbonFound.value = true
                    prefs.edit().putBoolean("dbon_found", true).apply()
                    setDebugUnlocked(true)
                } else {
                    dbonFound.value = false
                    prefs.edit().putBoolean("dbon_found", false).apply()
                    setDebugUnlocked(false)
                }

                // 3. Check for x86av5648ee=-1
                if (content.contains("x86av5648ee=-1")) {
                    x86Found.value = true
                    prefs.edit().putBoolean("x86_found", true).apply()
                    bootHasX86Key.value = true
                } else {
                    x86Found.value = false
                    prefs.edit().putBoolean("x86_found", false).apply()
                    bootHasX86Key.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            dbonFound.value = false
            prefs.edit().putBoolean("dbon_found", false).apply()
            setDebugUnlocked(false)
            x86Found.value = false
            prefs.edit().putBoolean("x86_found", false).apply()
            bootHasX86Key.value = false
        }
    }

    fun writeSuccessToKeyFile(): Boolean {
        val keyFile = findKeyFileEverywhere() ?: run {
            val externalDir = android.os.Environment.getExternalStorageDirectory()
            val psdevcomDir = java.io.File(externalDir, "psdevcom")
            if (!psdevcomDir.exists()) {
                psdevcomDir.mkdirs()
            }
            java.io.File(psdevcomDir, "key.txt")
        }

        try {
            val parentDir = keyFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }
            if (!keyFile.exists()) {
                keyFile.createNewFile()
            }
            val currentContent = keyFile.readText()
            val separator = if (currentContent.isNotEmpty() && !currentContent.endsWith("\n")) "\n" else ""
            keyFile.writeText(currentContent + separator + "XS3567sdSUCESS\n")
            return true
        } catch (e: java.io.IOException) {
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun preloadAllAssets(context: Context) {
        viewModelScope.launch {
            try {
                val res = context.resources
                preloadedPurePringle = BitmapFactory.decodeResource(res, R.drawable.img_pure_pringle_1780498841768)?.asImageBitmap()
                preloadedSinglePringle = BitmapFactory.decodeResource(res, R.drawable.img_single_pringle_1780498663196)?.asImageBitmap()
                preloadedPringle = BitmapFactory.decodeResource(res, R.drawable.img_pringle_1780497785206)?.asImageBitmap()
                preloadedAppIcon = BitmapFactory.decodeResource(res, R.drawable.img_app_icon_1780497806731)?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearPreloadedAssets() {
        preloadedPurePringle = null
        preloadedSinglePringle = null
        preloadedPringle = null
        preloadedAppIcon = null
    }

    fun setLoadEverything(enabled: Boolean, context: Context): Boolean {
        if (enabled) {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            activityManager?.getMemoryInfo(memoryInfo)
            val totalRamBytes = memoryInfo.totalMem
            val thresholdBytes = 6L * 1024L * 1024L * 1024L // 6 GB
            
            if (totalRamBytes < thresholdBytes) {
                return false
            }
        }
        loadEverything.value = enabled
        prefs.edit().putBoolean("load_everything", enabled).apply()
        if (enabled) {
            preloadAllAssets(context)
        } else {
            clearPreloadedAssets()
        }
        return true
    }

    val normalLayout = MutableStateFlow(loadLayoutConfig("normal"))
    val psmLayout = MutableStateFlow(loadLayoutConfig("psm"))
    val unfoldedNormalLayout = MutableStateFlow(loadLayoutConfig("unfolded_normal"))
    val foldedNormalLayout = MutableStateFlow(loadLayoutConfig("folded_normal"))
    val unfoldedPsmLayout = MutableStateFlow(loadLayoutConfig("unfolded_psm"))
    val foldedPsmLayout = MutableStateFlow(loadLayoutConfig("folded_psm"))

    private fun loadLayoutConfig(config: String): LayoutConfig {
        val fallbackPingleScale = prefs.getFloat("pingle_scale", 1.0f)
        val fallbackPingleOffsetX = prefs.getFloat("pingle_offset_x", 0f)
        val fallbackPingleOffsetY = prefs.getFloat("pingle_offset_y", 0f)
        val fallbackPingleTilt = prefs.getFloat("pingle_tilt", 0f)
        
        val fallbackTimerScale = prefs.getFloat("timer_scale", 1.0f)
        val fallbackTimerOffsetX = prefs.getFloat("timer_offset_x", 0f)
        val fallbackTimerOffsetY = prefs.getFloat("timer_offset_y", 0f)
        val fallbackTimerTilt = prefs.getFloat("timer_tilt", 0f)
        
        val fallbackSpotifyScale = prefs.getFloat("spotify_scale", 1.0f)
        val fallbackSpotifyOffsetX = prefs.getFloat("spotify_offset_x", 0f)
        val fallbackSpotifyOffsetY = prefs.getFloat("spotify_offset_y", 0f)
        val fallbackSpotifyTilt = prefs.getFloat("spotify_tilt", 0f)

        val fallbackDiscScale = prefs.getFloat("disc_scale", 1.0f)
        val fallbackDiscOffsetX = prefs.getFloat("disc_offset_x", 0f)
        val fallbackDiscOffsetY = prefs.getFloat("disc_offset_y", 0f)
        val fallbackDiscTilt = prefs.getFloat("disc_tilt", 0f)

        return LayoutConfig(
            pingleScale = prefs.getFloat("${config}_pingle_scale", fallbackPingleScale),
            pingleOffsetX = prefs.getFloat("${config}_pingle_offset_x", fallbackPingleOffsetX),
            pingleOffsetY = prefs.getFloat("${config}_pingle_offset_y", fallbackPingleOffsetY),
            pingleTilt = prefs.getFloat("${config}_pingle_tilt", fallbackPingleTilt),
            
            timerScale = prefs.getFloat("${config}_timer_scale", fallbackTimerScale),
            timerOffsetX = prefs.getFloat("${config}_timer_offset_x", fallbackTimerOffsetX),
            timerOffsetY = prefs.getFloat("${config}_timer_offset_y", fallbackTimerOffsetY),
            timerTilt = prefs.getFloat("${config}_timer_tilt", fallbackTimerTilt),
            
            spotifyScale = prefs.getFloat("${config}_spotify_scale", fallbackSpotifyScale),
            spotifyOffsetX = prefs.getFloat("${config}_spotify_offset_x", fallbackSpotifyOffsetX),
            spotifyOffsetY = prefs.getFloat("${config}_spotify_offset_y", fallbackSpotifyOffsetY),
            spotifyTilt = prefs.getFloat("${config}_spotify_tilt", fallbackSpotifyTilt),
            
            discScale = prefs.getFloat("${config}_disc_scale", fallbackDiscScale),
            discOffsetX = prefs.getFloat("${config}_disc_offset_x", fallbackDiscOffsetX),
            discOffsetY = prefs.getFloat("${config}_disc_offset_y", fallbackDiscOffsetY),
            discTilt = prefs.getFloat("${config}_disc_tilt", fallbackDiscTilt)
        )
    }

    fun updateLayoutConfig(config: String, layout: LayoutConfig) {
        when (config) {
            "normal" -> normalLayout.value = layout
            "psm" -> psmLayout.value = layout
            "unfolded_normal" -> unfoldedNormalLayout.value = layout
            "folded_normal" -> foldedNormalLayout.value = layout
            "unfolded_psm" -> unfoldedPsmLayout.value = layout
            "folded_psm" -> foldedPsmLayout.value = layout
        }
        
        prefs.edit()
            .putFloat("${config}_pingle_scale", layout.pingleScale)
            .putFloat("${config}_pingle_offset_x", layout.pingleOffsetX)
            .putFloat("${config}_pingle_offset_y", layout.pingleOffsetY)
            .putFloat("${config}_pingle_tilt", layout.pingleTilt)
            
            .putFloat("${config}_timer_scale", layout.timerScale)
            .putFloat("${config}_timer_offset_x", layout.timerOffsetX)
            .putFloat("${config}_timer_offset_y", layout.timerOffsetY)
            .putFloat("${config}_timer_tilt", layout.timerTilt)
            
            .putFloat("${config}_spotify_scale", layout.spotifyScale)
            .putFloat("${config}_spotify_offset_x", layout.spotifyOffsetX)
            .putFloat("${config}_spotify_offset_y", layout.spotifyOffsetY)
            .putFloat("${config}_spotify_tilt", layout.spotifyTilt)
            
            .putFloat("${config}_disc_scale", layout.discScale)
            .putFloat("${config}_disc_offset_x", layout.discOffsetX)
            .putFloat("${config}_disc_offset_y", layout.discOffsetY)
            .putFloat("${config}_disc_tilt", layout.discTilt)
            .apply()
    }

    fun resetLayoutConfig(config: String) {
        updateLayoutConfig(config, LayoutConfig())
    }

    fun loadActiveLayoutForConfig(config: String) {
        val layout = when (config) {
            "normal" -> normalLayout.value
            "psm" -> psmLayout.value
            "unfolded_normal" -> unfoldedNormalLayout.value
            "folded_normal" -> foldedNormalLayout.value
            "unfolded_psm" -> unfoldedPsmLayout.value
            "folded_psm" -> foldedPsmLayout.value
            else -> LayoutConfig()
        }
        setPingleScale(layout.pingleScale)
        setPingleOffsetX(layout.pingleOffsetX)
        setPingleOffsetY(layout.pingleOffsetY)
        setPingleTilt(layout.pingleTilt)
        
        setTimerScale(layout.timerScale)
        setTimerOffsetX(layout.timerOffsetX)
        setTimerOffsetY(layout.timerOffsetY)
        setTimerTilt(layout.timerTilt)
        
        setSpotifyScale(layout.spotifyScale)
        setSpotifyOffsetX(layout.spotifyOffsetX)
        setSpotifyOffsetY(layout.spotifyOffsetY)
        setSpotifyTilt(layout.spotifyTilt)
        
        setDiscScale(layout.discScale)
        setDiscOffsetX(layout.discOffsetX)
        setDiscOffsetY(layout.discOffsetY)
        setDiscTilt(layout.discTilt)
    }

    fun saveActiveLayoutForConfig(config: String) {
        val layout = LayoutConfig(
            pingleScale = pingleScale.value,
            pingleOffsetX = pingleOffsetX.value,
            pingleOffsetY = pingleOffsetY.value,
            pingleTilt = pingleTilt.value,
            
            timerScale = timerScale.value,
            timerOffsetX = timerOffsetX.value,
            timerOffsetY = timerOffsetY.value,
            timerTilt = timerTilt.value,
            
            spotifyScale = spotifyScale.value,
            spotifyOffsetX = spotifyOffsetX.value,
            spotifyOffsetY = spotifyOffsetY.value,
            spotifyTilt = spotifyTilt.value,
            
            discScale = discScale.value,
            discOffsetX = discOffsetX.value,
            discOffsetY = discOffsetY.value,
            discTilt = discTilt.value
        )
        updateLayoutConfig(config, layout)
    }

    val pingleScale = MutableStateFlow(prefs.getFloat("pingle_scale", 1.0f))
    val pingleOffsetX = MutableStateFlow(prefs.getFloat("pingle_offset_x", 0f))
    val pingleOffsetY = MutableStateFlow(prefs.getFloat("pingle_offset_y", 0f))
    val pingleTilt = MutableStateFlow(prefs.getFloat("pingle_tilt", 0f))

    val timerScale = MutableStateFlow(prefs.getFloat("timer_scale", 1.0f))
    val timerOffsetX = MutableStateFlow(prefs.getFloat("timer_offset_x", 0f))
    val timerOffsetY = MutableStateFlow(prefs.getFloat("timer_offset_y", 0f))
    val timerTilt = MutableStateFlow(prefs.getFloat("timer_tilt", 0f))

    val spotifyScale = MutableStateFlow(prefs.getFloat("spotify_scale", 1.0f))
    val spotifyOffsetX = MutableStateFlow(prefs.getFloat("spotify_offset_x", 0f))
    val spotifyOffsetY = MutableStateFlow(prefs.getFloat("spotify_offset_y", 0f))
    val spotifyTilt = MutableStateFlow(prefs.getFloat("spotify_tilt", 0f))

    val discScale = MutableStateFlow(prefs.getFloat("disc_scale", 1.0f))
    val discOffsetX = MutableStateFlow(prefs.getFloat("disc_offset_x", 0f))
    val discOffsetY = MutableStateFlow(prefs.getFloat("disc_offset_y", 0f))
    val discTilt = MutableStateFlow(prefs.getFloat("disc_tilt", 0f))

    fun setPingleScale(value: Float) {
        pingleScale.value = value
        prefs.edit().putFloat("pingle_scale", value).apply()
    }
    fun setPingleOffsetX(value: Float) {
        pingleOffsetX.value = value
        prefs.edit().putFloat("pingle_offset_x", value).apply()
    }
    fun setPingleOffsetY(value: Float) {
        pingleOffsetY.value = value
        prefs.edit().putFloat("pingle_offset_y", value).apply()
    }
    fun setPingleTilt(value: Float) {
        pingleTilt.value = value
        prefs.edit().putFloat("pingle_tilt", value).apply()
    }

    fun setTimerScale(value: Float) {
        timerScale.value = value
        prefs.edit().putFloat("timer_scale", value).apply()
    }
    fun setTimerOffsetX(value: Float) {
        timerOffsetX.value = value
        prefs.edit().putFloat("timer_offset_x", value).apply()
    }
    fun setTimerOffsetY(value: Float) {
        timerOffsetY.value = value
        prefs.edit().putFloat("timer_offset_y", value).apply()
    }
    fun setTimerTilt(value: Float) {
        timerTilt.value = value
        prefs.edit().putFloat("timer_tilt", value).apply()
    }

    fun setSpotifyScale(value: Float) {
        spotifyScale.value = value
        prefs.edit().putFloat("spotify_scale", value).apply()
    }
    fun setSpotifyOffsetX(value: Float) {
        spotifyOffsetX.value = value
        prefs.edit().putFloat("spotify_offset_x", value).apply()
    }
    fun setSpotifyOffsetY(value: Float) {
        spotifyOffsetY.value = value
        prefs.edit().putFloat("spotify_offset_y", value).apply()
    }
    fun setSpotifyTilt(value: Float) {
        spotifyTilt.value = value
        prefs.edit().putFloat("spotify_tilt", value).apply()
    }

    fun setDiscScale(value: Float) {
        discScale.value = value
        prefs.edit().putFloat("disc_scale", value).apply()
    }
    fun setDiscOffsetX(value: Float) {
        discOffsetX.value = value
        prefs.edit().putFloat("disc_offset_x", value).apply()
    }
    fun setDiscOffsetY(value: Float) {
        discOffsetY.value = value
        prefs.edit().putFloat("disc_offset_y", value).apply()
    }
    fun setDiscTilt(value: Float) {
        discTilt.value = value
        prefs.edit().putFloat("disc_tilt", value).apply()
    }

    fun resetAllPingui() {
        setPingleScale(1.0f)
        setPingleOffsetX(0f)
        setPingleOffsetY(0f)
        setPingleTilt(0f)
        setTimerScale(1.0f)
        setTimerOffsetX(0f)
        setTimerOffsetY(0f)
        setTimerTilt(0f)
        setSpotifyScale(1.0f)
        setSpotifyOffsetX(0f)
        setSpotifyOffsetY(0f)
        setSpotifyTilt(0f)
        setDiscScale(1.0f)
        setDiscOffsetX(0f)
        setDiscOffsetY(0f)
        setDiscTilt(0f)
    }

    val isDebugUnlocked = MutableStateFlow(prefs.getBoolean("debug_unlocked", false))
    val easterRainbowNeon = MutableStateFlow(false)
    val easterMatrixBg = MutableStateFlow(false)
    val easterReverseSpin = MutableStateFlow(false)
    val easterSpaceStars = MutableStateFlow(false)

    val isRainbowNeonUnlocked = MutableStateFlow(false)
    val isMatrixBgUnlocked = MutableStateFlow(false)
    val isReverseSpinUnlocked = MutableStateFlow(false)
    val isSpaceStarsUnlocked = MutableStateFlow(false)

    val invisiblePingleEnabled = MutableStateFlow(false)
    val isInvisiblePingleUnlocked = MutableStateFlow(false)

    fun setInvisiblePingleEnabled(enabled: Boolean) { invisiblePingleEnabled.value = enabled }
    fun setEasterRainbowNeon(enabled: Boolean) { easterRainbowNeon.value = enabled }
    fun setEasterMatrixBg(enabled: Boolean) { easterMatrixBg.value = enabled }
    fun setEasterReverseSpin(enabled: Boolean) { easterReverseSpin.value = enabled }
    fun setEasterSpaceStars(enabled: Boolean) { easterSpaceStars.value = enabled }

    fun setRainbowNeonUnlocked(unlocked: Boolean) { isRainbowNeonUnlocked.value = unlocked }
    fun setMatrixBgUnlocked(unlocked: Boolean) { isMatrixBgUnlocked.value = unlocked }
    fun setReverseSpinUnlocked(unlocked: Boolean) { isReverseSpinUnlocked.value = unlocked }
    fun setSpaceStarsUnlocked(unlocked: Boolean) { isSpaceStarsUnlocked.value = unlocked }
    fun setInvisiblePingleUnlocked(unlocked: Boolean) { isInvisiblePingleUnlocked.value = unlocked }

    fun unlockAllNonChineseEasterEggs() {
        setRainbowNeonUnlocked(true)
        setMatrixBgUnlocked(true)
        setReverseSpinUnlocked(true)
        setSpaceStarsUnlocked(true)
        setInvisiblePingleUnlocked(true)
    }

    private object SecureKeyValidator {
        private fun sha256(s: String): String {
            val bytes = java.security.MessageDigest.getInstance("SHA-256")
                .digest(s.lowercase().trim().toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        enum class KeyKind { V2X, TRIAL_1HR, DEVKEY }

        fun validateKey(rawKey: String): KeyKind? {
            val hash = sha256(rawKey)
            return when (hash) {
                "3bfcb0a8bcb1247546ab7c0e66d9e3f547341c4a314c73ff4a77e9b2f6d27293",
                "6f9f036c47d089e73e44cd3a091ef157b80497e99be7b6ed520cf124f66ae982",
                "363999cbb6a0bd9cc9495520e2dbd5c04da328a926576da64435e1767322e2c2",
                "1bb76fab888f45195fa1524fe540ad832f4d3c1c95e8ae6e731bb5a865d09160" -> KeyKind.V2X

                "72a11a2deaced0694475c4283b2336755b84961a76bed19a2d32d5406c68b26e" -> KeyKind.TRIAL_1HR

                "924b483cb0a71f7a1f40991d36e24b8980c1bcb1e1e98b676b442d266e1d7d44" -> KeyKind.DEVKEY

                else -> null
            }
        }
    }

    fun getFileNameForKeyPrefix(prefix: String): String {
        return when (prefix.lowercase()) {
            "psv2x" -> "PSV2Xunl.txt"
            "ps2t_" -> "PSV2Tunl.txt"
            "psdev" -> "PSDEVunl.txt"
            else -> "PSV2Xunl.txt"
        }
    }

    fun applyKeyPrefix(rawKey: String, timestamp: Long) {
        val trimmed = rawKey.trim()
        val kind = SecureKeyValidator.validateKey(trimmed)

        if (kind == null) {
            isActivated.value = false
            isV2xUnlocked.value = false
            isTrialActive.value = false
            isDevKeyUnlocked.value = false
            isPlusEEUnlocked.value = false
            isPlusMSUnlocked.value = false
            activeKeyPrefix.value = null
            setDebugUnlocked(false)
            prefs.edit().putBoolean("is_activated", false).remove("activation_key").apply()
            return
        }

        activationKey.value = trimmed
        activationTimestamp.value = timestamp

        val now = System.currentTimeMillis()
        val elapsed = maxOf(0L, now - timestamp)
        val trialDuration = 3600_000L // 1 hour

        when (kind) {
            SecureKeyValidator.KeyKind.V2X -> {
                activeKeyPrefix.value = "psv2x"
                isActivated.value = true
                isV2xUnlocked.value = true
                isTrialActive.value = false
                isDevKeyUnlocked.value = false
                isPlusEEUnlocked.value = true
                isPlusMSUnlocked.value = true
                unlockAllNonChineseEasterEggs()
                setDebugUnlocked(false)
            }
            SecureKeyValidator.KeyKind.TRIAL_1HR -> {
                activeKeyPrefix.value = "ps2t_"
                if (elapsed < trialDuration) {
                    isActivated.value = true
                    isV2xUnlocked.value = true
                    isTrialActive.value = true
                    isDevKeyUnlocked.value = false
                    isPlusEEUnlocked.value = true
                    isPlusMSUnlocked.value = true
                    trialRemainingMs.value = trialDuration - elapsed
                    unlockAllNonChineseEasterEggs()
                } else {
                    trialRemainingMs.value = 0L
                    isActivated.value = false
                    isV2xUnlocked.value = false
                    isTrialActive.value = false
                    isDevKeyUnlocked.value = false
                    isPlusEEUnlocked.value = false
                    isPlusMSUnlocked.value = false
                }
                setDebugUnlocked(false)
            }
            SecureKeyValidator.KeyKind.DEVKEY -> {
                activeKeyPrefix.value = "psdev"
                isActivated.value = true
                isV2xUnlocked.value = false
                isTrialActive.value = false
                isDevKeyUnlocked.value = true
                isPlusEEUnlocked.value = false
                isPlusMSUnlocked.value = false
                setDebugUnlocked(true)
            }
        }

        prefs.edit()
            .putString("activation_key", trimmed)
            .putLong("activation_timestamp", timestamp)
            .putString("active_key_prefix", activeKeyPrefix.value)
            .putBoolean("is_activated", isActivated.value)
            .apply()
    }

    fun checkActivationFile() {
        val fileNames = listOf("PSV2Xunl.txt", "PSV2Tunl.txt", "PSDEVunl.txt")
        val searchDirs = listOf(
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
            java.io.File("/sdcard/Download"),
            java.io.File("/sdcard/downloads")
        )

        var latestFile: java.io.File? = null
        var latestTime = -1L

        for (dir in searchDirs) {
            try {
                if (dir.exists() && dir.isDirectory) {
                    for (fName in fileNames) {
                        val f = java.io.File(dir, fName)
                        if (f.exists() && f.readText().trim().isNotEmpty()) {
                            val modTime = f.lastModified()
                            if (modTime > latestTime) {
                                latestTime = modTime
                                latestFile = f
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (latestFile != null) {
            val keyText = latestFile.readText().trim()
            val modTime = if (latestFile.lastModified() > 0) latestFile.lastModified() else System.currentTimeMillis()
            applyKeyPrefix(keyText, modTime)
        } else {
            val savedKey = prefs.getString("activation_key", null)
            val savedTime = prefs.getLong("activation_timestamp", 0L)
            if (savedKey != null && savedKey.isNotEmpty()) {
                applyKeyPrefix(savedKey, savedTime)
            }
        }
    }

    fun activateWithKey(context: Context, keyInput: String): Boolean {
        val trimmed = keyInput.trim()
        val kind = SecureKeyValidator.validateKey(trimmed) ?: return false

        val now = System.currentTimeMillis()
        applyKeyPrefix(trimmed, now)

        val prefix = activeKeyPrefix.value ?: "psv2x"
        val fileName = getFileNameForKeyPrefix(prefix)
        try {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val keyFile = java.io.File(downloadsDir, fileName)
            keyFile.writeText(trimmed)
        } catch (e: Exception) {
            try {
                val altFile = java.io.File("/sdcard/Download/$fileName")
                altFile.parentFile?.mkdirs()
                altFile.writeText(trimmed)
            } catch (ex: Exception) {}
        }
        return true
    }

    fun getMaskedKey(): String {
        val key = activationKey.value ?: ""
        if (!isActivated.value || key.isEmpty()) return "NOT ACTIVATED"
        return if (key.length >= 5) {
            key.take(5) + "X".repeat(maxOf(0, key.length - 5))
        } else {
            key
        }
    }

    fun getFormattedActivationDate(): String {
        val ts = activationTimestamp.value
        if (ts <= 0L) return "N/A"
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(ts))
    }

    fun getKeyTypeDescription(prefix: String?): String {
        val p = prefix?.lowercase() ?: (activeKeyPrefix.value?.lowercase() ?: "")
        return when (p) {
            "psv2x" -> "V2x (Full game with all features)"
            "ps2t_" -> "Trial (1 Hour Full Access)"
            "psdev" -> "DevKey (Debug Menu)"
            else -> if (isActivated.value) "Activated" else "None"
        }
    }

    fun setDebugUnlocked(unlocked: Boolean) {
        isDebugUnlocked.value = unlocked
        prefs.edit().putBoolean("debug_unlocked", unlocked).apply()
    }

    fun setCustomImageUri(uriString: String?) {
        pingleCustomImageUri.value = uriString
        prefs.edit().putString("pingle_custom_image_uri", uriString).apply()
    }

    fun setCustomMediaType(type: String) {
        customMediaType.value = type
        prefs.edit().putString("custom_media_type", type).apply()
    }

    fun setUseCustomImage(use: Boolean) {
        useCustomImage.value = use
        prefs.edit().putBoolean("use_custom_image", use).apply()
    }

    fun setScreen(screen: Screen) {
        currentScreen.value = screen
    }

    fun setPingleFoldAngleThreshold(angle: Float) {
        val rounded = Math.round(angle).toFloat().coerceIn(0f, 180f)
        pingleFoldAngleThreshold.value = rounded
        prefs.edit().putFloat("pingle_fold_angle_threshold", rounded).apply()
    }

    fun setPingleSpeed(speed: Float) {
        val rounded = Math.round(speed * 10f) / 10f
        pingleSpeed.value = rounded.coerceIn(0.5f, 10.0f)
        prefs.edit().putFloat("pingle_speed", rounded).apply()
    }

    fun setPingleFriction(friction: Float) {
        val rounded = Math.round(friction).toFloat().coerceIn(0f, 100f)
        pingleFriction.value = rounded
        prefs.edit().putFloat("pingle_friction", rounded).apply()
    }

    fun unlockManual() {
        isManualUnlocked.value = true
        prefs.edit().putBoolean("manual_unlocked", true).apply()
    }

    fun unlockEverything() {
        isManualUnlocked.value = true
        prefs.edit().putBoolean("manual_unlocked", true).apply()
        isDebugUnlocked.value = true
        prefs.edit().putBoolean("debug_unlocked", true).apply()

        val targetDuration = 18000000L // 5 hours in ms
        if (totalSpinDuration.value < targetDuration) {
            totalSpinDuration.value = targetDuration
            prefs.edit().putLong("total_spin_duration", targetDuration).apply()
        }
    }

    fun checkEasterEggUnlocks(durationMs: Long, totalDuration: Long) {
        // disabled
    }

    fun adjustSpeed(increase: Boolean) {
        val current = pingleSpeed.value
        val step = 0.5f
        val newVal = if (increase) {
            (current + step).coerceAtMost(10.0f)
        } else {
            (current - step).coerceAtLeast(0.5f)
        }
        val rounded = Math.round(newVal * 10f) / 10f
        pingleSpeed.value = rounded
        prefs.edit().putFloat("pingle_speed", rounded).apply()
    }

    fun saveScore(durationMs: Long, isManual: Boolean = false) {
        if (durationMs < 100L && !invisiblePingleEnabled.value) return // don't save ultra-tiny accidental spinnigs
        viewModelScope.launch {
            repository.insertScore(ScoreEntity(durationMs = durationMs, isManual = isManual))
            val currentTotal = prefs.getLong("total_spin_duration", 0L)
            val newTotal = currentTotal + durationMs
            prefs.edit().putLong("total_spin_duration", newTotal).apply()
            totalSpinDuration.value = newTotal
            checkEasterEggUnlocks(durationMs, newTotal)
        }
    }

    fun saveDebugScore(durationMs: Long) {
        viewModelScope.launch {
            repository.insertScore(ScoreEntity(durationMs = durationMs, isManual = false, isDebug = true))
        }
    }

    fun setPingleTint(tintId: String) {
        pingleTint.value = tintId
        prefs.edit().putString("pingle_tint", tintId).apply()
    }

    fun setPingleCustomColor(colorInt: Int) {
        pingleCustomColor.value = colorInt
        prefs.edit().putInt("pingle_custom_color", colorInt).apply()
    }

    fun addDebugSpinDuration(ms: Long) {
        val currentTotal = prefs.getLong("total_spin_duration", 0L)
        val newTotal = currentTotal + ms
        prefs.edit().putLong("total_spin_duration", newTotal).apply()
        totalSpinDuration.value = newTotal
    }

    fun clearAllScores() {
        viewModelScope.launch {
            repository.clearScores()
            prefs.edit().putLong("total_spin_duration", 0L).apply()
            totalSpinDuration.value = 0L
        }
    }

    val buttonVibrationMode = MutableStateFlow(prefs.getString("button_vibration_mode", "medium") ?: "medium")
    val spinVibrationMode = MutableStateFlow(prefs.getString("spin_vibration_mode", "light") ?: "light")
    val spinVibrationInterval = MutableStateFlow(prefs.getFloat("spin_vibration_interval", 15.0f))

    fun setButtonVibrationMode(mode: String) {
        buttonVibrationMode.value = mode
        prefs.edit().putString("button_vibration_mode", mode).apply()
    }

    fun setSpinVibrationMode(mode: String) {
        spinVibrationMode.value = mode
        prefs.edit().putString("spin_vibration_mode", mode).apply()
    }

    fun setSpinVibrationInterval(interval: Float) {
        val rounded = Math.round(interval * 10f) / 10f
        val clamped = rounded.coerceIn(5.0f, 90.0f)
        spinVibrationInterval.value = clamped
        prefs.edit().putFloat("spin_vibration_interval", clamped).apply()
    }

    val isChineseCrashed = MutableStateFlow(prefs.getBoolean("is_chinese_crashed", false))

    fun triggerChineseCrash() {
        isChineseCrashed.value = true
        prefs.edit().putBoolean("is_chinese_crashed", true).apply()
    }

    init {
        checkKeyFile(application)
        checkActivationFile()
        if (loadEverything.value && !isDeviceKilled.value) {
            preloadAllAssets(application)
        }

        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                if (isTrialActive.value) {
                    val ts = activationTimestamp.value
                    val now = System.currentTimeMillis()
                    val elapsed = maxOf(0L, now - ts)
                    val remaining = maxOf(0L, 3600_000L - elapsed)
                    trialRemainingMs.value = remaining
                    if (remaining <= 0L) {
                        isTrialActive.value = false
                        isActivated.value = false
                        isV2xUnlocked.value = false
                        isPlusEEUnlocked.value = false
                        isPlusMSUnlocked.value = false
                        prefs.edit().putBoolean("is_activated", false).apply()
                    }
                }
            }
        }
    }
}

data class LayoutConfig(
    val pingleScale: Float = 1.0f,
    val pingleOffsetX: Float = 0f,
    val pingleOffsetY: Float = 0f,
    val pingleTilt: Float = 0f,
    val timerScale: Float = 1.0f,
    val timerOffsetX: Float = 0f,
    val timerOffsetY: Float = 0f,
    val timerTilt: Float = 0f,
    val spotifyScale: Float = 1.0f,
    val spotifyOffsetX: Float = 0f,
    val spotifyOffsetY: Float = 0f,
    val spotifyTilt: Float = 0f,
    val discScale: Float = 1.0f,
    val discOffsetX: Float = 0f,
    val discOffsetY: Float = 0f,
    val discTilt: Float = 0f
)

