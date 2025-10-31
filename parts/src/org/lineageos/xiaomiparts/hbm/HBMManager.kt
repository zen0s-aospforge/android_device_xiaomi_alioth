package org.lineageos.xiaomiparts.hbm

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.Companion.DC_DIMMING_ENABLE_KEY
import org.lineageos.xiaomiparts.utils.getFileValueAsBoolean
import org.lineageos.xiaomiparts.utils.writeLine
import java.util.concurrent.CopyOnWriteArraySet

object HBMManager {

    private const val TAG = "HBMManager"
    
    private const val HBM_SYSFS_PATH = "/sys/class/drm/card0/card0-DSI-1/disp_param"
    private const val BACKLIGHT_SYSFS_PATH = "/sys/class/backlight/panel0-backlight/brightness"
    
    const val PREF_HBM_KEY = "hbm"
    const val PREF_AUTO_HBM_KEY = "auto_hbm"
    const val PREF_AUTO_HBM_THRESHOLD_KEY = "auto_hbm_threshold"
    const val PREF_HBM_DISABLE_TIME_KEY = "hbm_disable_time"
    
    enum class HBMOwner {
        NONE,
        MANUAL,
        AUTO_SERVICE
    }
    
    interface HBMStateListener {
        fun onHBMStateChanged(enabled: Boolean, owner: HBMOwner)
    }
    
    private val workerThread = HandlerThread("HBMWorker").apply { start() }
    private val workerHandler = Handler(workerThread.looper)
    private val mainHandler = Handler(Looper.getMainLooper())
    
    @Volatile
    private var currentOwner = HBMOwner.NONE
    
    @Volatile
    private var savedBrightnessMode: Int? = null
    
    @Volatile
    private var savedBrightnessValue: Int? = null
    
    private val listeners = CopyOnWriteArraySet<HBMStateListener>()
    
    fun enableHBM(context: Context, owner: HBMOwner, callback: ((Boolean) -> Unit)? = null) {
        require(owner != HBMOwner.NONE) { "Cannot enable HBM with NONE owner" }
        
        workerHandler.post {
            val result = enableHBMInternal(context, owner)
            callback?.let { mainHandler.post { it(result) } }
        }
    }
    
    private fun enableHBMInternal(context: Context, owner: HBMOwner): Boolean {
            Log.i(TAG, "enableHBM: owner=$owner, currentOwner=$currentOwner")
            
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (prefs.getBoolean(DC_DIMMING_ENABLE_KEY, false)) {
                Log.w(TAG, "Cannot enable HBM: DC Dimming is active")
                return false
            }
            
            if (currentOwner == HBMOwner.MANUAL && owner == HBMOwner.AUTO_SERVICE) {
                Log.i(TAG, "HBM already enabled manually, ignoring auto-enable request")
                return false
            }
            
            val hardwareEnabled = getFileValueAsBoolean(HBM_SYSFS_PATH, false)
            if (hardwareEnabled && currentOwner == owner) {
                Log.i(TAG, "HBM already enabled by same owner, skipping")
                return true
            }
            
            if (currentOwner == HBMOwner.NONE) {
                val currentMode = Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                savedBrightnessMode = currentMode
                Log.i(TAG, "Saved brightness mode: $currentMode")
                
                // Save current brightness value
                val currentBrightness = Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    128
                )
                savedBrightnessValue = currentBrightness
                Log.i(TAG, "Saved brightness value: $currentBrightness")
            }
            
            if (!writeLine(HBM_SYSFS_PATH, "0x10000")) {
                Log.e(TAG, "Failed to write to HBM sysfs")
                return false
            }
            
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            writeLine(BACKLIGHT_SYSFS_PATH, "2047")
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                255
            )
            
            currentOwner = owner
            prefs.edit().putBoolean(PREF_HBM_KEY, true).apply()
            
            Log.i(TAG, "HBM enabled successfully by $owner")
            notifyListeners(true, owner)
            return true
    }
    
    fun disableHBM(context: Context, owner: HBMOwner, callback: ((Boolean) -> Unit)? = null) {
        workerHandler.post {
            val result = disableHBMInternal(context, owner)
            callback?.let { mainHandler.post { it(result) } }
        }
    }
    
    private fun disableHBMInternal(context: Context, owner: HBMOwner): Boolean {
            Log.i(TAG, "disableHBM: owner=$owner, currentOwner=$currentOwner")
            
            if (currentOwner == HBMOwner.NONE) {
                Log.i(TAG, "HBM already disabled, skipping")
                return true
            }
            
            if (owner == HBMOwner.AUTO_SERVICE && currentOwner == HBMOwner.MANUAL) {
                Log.i(TAG, "Cannot disable manually-enabled HBM from auto service")
                return false
            }
            
            if (!writeLine(HBM_SYSFS_PATH, "0xF0000")) {
                Log.e(TAG, "Failed to write to HBM sysfs")
                return false
            }
            
            val resolver = context.contentResolver
            val currentSystemMode = Settings.System.getInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
            
            // Restore brightness mode
            if (currentSystemMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                val modeToRestore = savedBrightnessMode ?: Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                Log.i(TAG, "Restoring brightness mode: $modeToRestore")
                Settings.System.putInt(
                    resolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    modeToRestore
                )
            } else {
                Log.i(TAG, "User changed brightness mode to $currentSystemMode, keeping it")
            }
            
            // Restore brightness value if in MANUAL mode
            val restoredMode = Settings.System.getInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
            
            if (restoredMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL && savedBrightnessValue != null) {
                Log.i(TAG, "Restoring brightness value: $savedBrightnessValue")
                Settings.System.putInt(
                    resolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    savedBrightnessValue!!
                )
            }
            
            // Clear saved state
            currentOwner = HBMOwner.NONE
            savedBrightnessMode = null
            savedBrightnessValue = null
            
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putBoolean(PREF_HBM_KEY, false).apply()
            
            Log.i(TAG, "HBM disabled successfully by $owner")
            notifyListeners(false, HBMOwner.NONE)
            return true
    }
    
    fun isHBMEnabledInHardware(): Boolean {
        return getFileValueAsBoolean(HBM_SYSFS_PATH, false)
    }
    
    fun getCurrentOwner(): HBMOwner {
        return currentOwner
    }
    
    fun isHBMEnabled(): Boolean {
        return currentOwner != HBMOwner.NONE
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
                    Log.e(TAG, "Error notifying listener", e)
                }
            }
        }
    }
    
    fun initializeOnBoot(context: Context) {
        workerHandler.post {
            Log.i(TAG, "Initializing HBM state on boot")
            
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val shouldBeEnabled = prefs.getBoolean(PREF_HBM_KEY, false)
            val hardwareEnabled = getFileValueAsBoolean(HBM_SYSFS_PATH, false)
            
            if (shouldBeEnabled && !hardwareEnabled) {
                if (!prefs.getBoolean(DC_DIMMING_ENABLE_KEY, false)) {
                    Log.i(TAG, "Restoring manually-enabled HBM on boot")
                    enableHBMInternal(context, HBMOwner.MANUAL)
                }
            } else if (!shouldBeEnabled && hardwareEnabled) {
                Log.i(TAG, "Disabling HBM on boot (preference is false)")
                disableHBMInternal(context, HBMOwner.MANUAL)
            }
        }
    }
}
