package org.lineageos.xiaomiparts.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lineageos.xiaomiparts.data.PREF_DC_DIMMING_KEY
import org.lineageos.xiaomiparts.utils.getFileValueAsBoolean
import org.lineageos.xiaomiparts.utils.writeLine
import org.lineageos.xiaomiparts.utils.Logging
import org.lineageos.xiaomiparts.services.AutoHBMService
import java.util.concurrent.CopyOnWriteArraySet


class HBMManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners = CopyOnWriteArraySet<HBMStateListener>()

    @Volatile
    private var currentOwner = HBMOwner.NONE
    @Volatile
    private var savedBrightnessMode: Int? = null
    @Volatile
    private var savedBrightnessValue: Int? = null

    
    fun isHBMEnabled(): Boolean {
        return currentOwner != HBMOwner.NONE
    }

    
    suspend fun isHBMEnabledInHardware(): Boolean = withContext(Dispatchers.IO) {
        getFileValueAsBoolean(HBM_SYSFS_PATH, false)
    }

    
    private suspend fun enableHBMInternal(owner: HBMOwner): Boolean = withContext(Dispatchers.IO) {
        Logging.i(TAG, "enableHBM: owner=$owner, currentOwner=$currentOwner")
        
        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        if (prefs.getBoolean(PREF_DC_DIMMING_KEY, false)) {
            Logging.w(TAG, "Cannot enable HBM: DC Dimming is active")
            return@withContext false
        }
        
        if (currentOwner == HBMOwner.MANUAL && owner == HBMOwner.AUTO_SERVICE) {
            Logging.i(TAG, "HBM already enabled manually, ignoring auto-enable request")
            return@withContext false
        }
        
        val hardwareEnabled = getFileValueAsBoolean(HBM_SYSFS_PATH, false)
        if (hardwareEnabled && currentOwner == owner) {
            Logging.i(TAG, "HBM already enabled by same owner, skipping")
            return@withContext true
        }
        
        if (currentOwner == HBMOwner.NONE) {
            val currentMode = Settings.System.getInt(
                appContext.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            savedBrightnessMode = currentMode
            Logging.i(TAG, "Saved brightness mode: $currentMode")
            
            val currentBrightness = Settings.System.getInt(
                appContext.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                128
            )
            savedBrightnessValue = currentBrightness
            Logging.i(TAG, "Saved brightness value: $currentBrightness")
        }
        
        
        if (!writeLine(HBM_SYSFS_PATH, "0x10000")) {
            Logging.e(TAG, "Failed to write to HBM sysfs")
            return@withContext false
        }
        
        Settings.System.putInt(
            appContext.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
        writeLine(BACKLIGHT_SYSFS_PATH, "2047")
        Settings.System.putInt(
            appContext.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            255
        )
        

        currentOwner = owner
        prefs.edit().putBoolean(PREF_HBM_KEY, true).apply()
        
        Logging.i(TAG, "HBM enabled successfully by $owner")
        notifyListeners(true, owner)
        return@withContext true
    }

    private suspend fun disableHBMInternal(owner: HBMOwner): Boolean = withContext(Dispatchers.IO) {
        Logging.i(TAG, "disableHBM: owner=$owner, currentOwner=$currentOwner")
        
        if (currentOwner == HBMOwner.NONE) {
            Logging.i(TAG, "HBM already disabled, skipping")
            return@withContext true
        }
        
        if (owner == HBMOwner.AUTO_SERVICE && currentOwner == HBMOwner.MANUAL) {
            Logging.i(TAG, "Cannot disable manually-enabled HBM from auto service")
            return@withContext false
        }
        
        
        if (!writeLine(HBM_SYSFS_PATH, "0xF0000")) {
            Logging.e(TAG, "Failed to write to HBM sysfs")
            return@withContext false
        }
        
        val resolver = appContext.contentResolver
        val currentSystemMode = Settings.System.getInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        )
        
        if (currentSystemMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
            val modeToRestore = savedBrightnessMode ?: Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            Logging.i(TAG, "Restoring brightness mode: $modeToRestore")
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                modeToRestore
            )
        }
        
        val restoredMode = Settings.System.getInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        )
        
        if (restoredMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL && savedBrightnessValue != null) {
            Logging.i(TAG, "Restoring brightness value: $savedBrightnessValue")
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS,
                savedBrightnessValue!!
            )
        }
        
        
        currentOwner = HBMOwner.NONE
        savedBrightnessMode = null
        savedBrightnessValue = null
        
        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        prefs.edit().putBoolean(PREF_HBM_KEY, false).apply()
        
        Logging.i(TAG, "HBM disabled successfully by $owner")
        notifyListeners(false, HBMOwner.NONE)
        return@withContext true
    }

    
    suspend fun enableHBM(owner: HBMOwner): Boolean {
        require(owner != HBMOwner.NONE) { "Cannot enable HBM with NONE owner" }
        return enableHBMInternal(owner)
    }

    suspend fun disableHBM(owner: HBMOwner): Boolean {
        return disableHBMInternal(owner)
    }

    suspend fun initializeOnBoot() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        val shouldBeEnabled = prefs.getBoolean(PREF_HBM_KEY, false)
        val hardwareEnabled = withContext(Dispatchers.IO) { 
            getFileValueAsBoolean(HBM_SYSFS_PATH, false) 
        }
        
        if (shouldBeEnabled && !hardwareEnabled) {
            if (!prefs.getBoolean(PREF_DC_DIMMING_KEY, false)) {
                Logging.i(TAG, "Restoring manually-enabled HBM on boot")
                enableHBMInternal(HBMOwner.MANUAL)
            }
        } else if (!shouldBeEnabled && hardwareEnabled) {
            Logging.i(TAG, "Disabling HBM on boot (preference is false)")
            disableHBMInternal(HBMOwner.MANUAL)
        }
    }

    

    fun addListener(listener: HBMStateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: HBMStateListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(enabled: Boolean, owner: HBMOwner) {
        mainHandler.post {
            listeners.forEach { listener ->
                try {
                    listener.onHBMStateChanged(enabled, owner)
                } catch (e: Exception) {
                    Logging.e(TAG, "Error notifying listener", e)
                }
            }
        }
    }

    fun isAutoHbmEnabled(): Boolean =
        PreferenceManager.getDefaultSharedPreferences(appContext)
            .getBoolean(PREF_AUTO_HBM_KEY, false)

    fun setAutoHbmEnabled(enabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
            .putBoolean(PREF_AUTO_HBM_KEY, enabled).apply()
        AutoHBMService.enableService(appContext, enabled)
    }
    
    fun getThresholdValue(): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        return try {
            prefs.getInt(PREF_AUTO_HBM_THRESHOLD_KEY, 20000)
        } catch (e: ClassCastException) {
            Logging.w(TAG, "Old threshold value is a String, parsing manually.")
            try {
                prefs.getString(PREF_AUTO_HBM_THRESHOLD_KEY, "20000")?.toInt() ?: 20000
            } catch (ne: NumberFormatException) {
                Logging.e(TAG, "Failed to parse old threshold value, resetting.", ne)
                20000
            }
        }
    }
    
    fun getDisableTime(): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        return try {
            prefs.getInt(PREF_HBM_DISABLE_TIME_KEY, 1)
        } catch (e: ClassCastException) {
            Logging.w(TAG, "Old disable time value is a String, parsing manually.")
            try {
                val stringValue = prefs.getString(PREF_HBM_DISABLE_TIME_KEY, "1")
                val intValue = stringValue?.toIntOrNull() ?: 1
                prefs.edit()
                    .putInt(PREF_HBM_DISABLE_TIME_KEY, intValue)
                    .apply()
                intValue
            } catch (ne: NumberFormatException) {
                Logging.e(TAG, "Failed to parse old disable time value, resetting.", ne)
                prefs.edit()
                    .putInt(PREF_HBM_DISABLE_TIME_KEY, 1)
                    .apply()
                1
            }
        }
    }
    
    fun setDisableTime(value: Int) {
        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
            .putInt(PREF_HBM_DISABLE_TIME_KEY, value).apply()
    }
    
    fun setThresholdValue(value: Int) {
        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
            .putInt(PREF_AUTO_HBM_THRESHOLD_KEY, value).apply()
    }

    

    interface HBMStateListener {
        fun onHBMStateChanged(enabled: Boolean, owner: HBMOwner)
    }

    enum class HBMOwner {
        NONE,
        MANUAL,
        AUTO_SERVICE
    }

    companion object {
        private const val TAG = "HBMMgr"
        private const val HBM_SYSFS_PATH = "/sys/class/drm/card0/card0-DSI-1/disp_param"
        private const val BACKLIGHT_SYSFS_PATH = "/sys/class/backlight/panel0-backlight/brightness"
        
        const val PREF_HBM_KEY = "hbm"
        const val PREF_AUTO_HBM_KEY = "auto_hbm"
        const val PREF_AUTO_HBM_THRESHOLD_KEY = "auto_hbm_threshold"
        const val PREF_HBM_DISABLE_TIME_KEY = "hbm_disable_time"

        const val MIN_THRESHOLD = 2000
        const val MAX_THRESHOLD = 20000
        const val STEP_THRESHOLD = 2000

        const val MIN_TIME_DELAY = 1
        const val MAX_TIME_DELAY = 10
        const val STEP_TIME_DELAY = 1

        @Volatile private var instance: HBMManager? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: HBMManager(context.applicationContext).also { instance = it }
            }
    }
}