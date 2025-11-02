package org.lineageos.xiaomiparts.hbm

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.Companion.DC_DIMMING_ENABLE_KEY
import org.lineageos.xiaomiparts.utils.dlog
import org.lineageos.xiaomiparts.utils.getFileValueAsBoolean
import org.lineageos.xiaomiparts.utils.writeLine

object HBMConstants {
    const val HBM_SYSFS_PATH = "/sys/class/drm/card0/card0-DSI-1/disp_param"
    const val BACKLIGHT_SYSFS_PATH = "/sys/class/backlight/panel0-backlight/brightness"

    const val PREF_HBM_KEY = "hbm"
    const val PREF_AUTO_HBM_KEY = "auto_hbm"
    const val PREF_AUTO_HBM_THRESHOLD_KEY = "auto_hbm_threshold"
    const val PREF_HBM_DISABLE_TIME_KEY = "hbm_disable_time"
    const val PREF_HBM_SAVED_BRIGHTNESS_MODE = "hbm_saved_brightness_mode"
}

object HBMManager {

    private const val TAG = "HBMManager"

    fun setHBMEnabled(context: Context, enable: Boolean): Boolean {
        // Always visible error log to track caller
        Log.e(TAG, "========== setHBMEnabled called: enable=$enable ==========")
        Log.e(TAG, "CALLER STACK TRACE:")
        Exception().stackTrace.take(10).forEach { 
            Log.e(TAG, "  at $it")
        }
        
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val resolver = context.contentResolver

        val dcDimmingEnabled = prefs.getBoolean(DC_DIMMING_ENABLE_KEY, false)

        if (enable && dcDimmingEnabled) {
            dlog(TAG, "Cannot enable HBM: DC dimming is enabled")
            return false
        }

        // When disabling, check if HBM is already off to avoid unnecessary brightness mode changes
        if (!enable) {
            val currentlyEnabled = getFileValueAsBoolean(HBMConstants.HBM_SYSFS_PATH, false)
            if (!currentlyEnabled) {
                dlog(TAG, "HBM already disabled, skipping brightness mode manipulation")
                prefs.edit().putBoolean(HBMConstants.PREF_HBM_KEY, false).apply()
                return true
            }
        }

        val success = writeLine(HBMConstants.HBM_SYSFS_PATH, if (enable) "0x10000" else "0xF0000")

        if (!success) {
            dlog(TAG, "Failed to write to HBM sysfs")
            return false
        }

        if (enable) {
            // Save current brightness mode ONLY if not already saved (first time enabling)
            if (!prefs.contains(HBMConstants.PREF_HBM_SAVED_BRIGHTNESS_MODE)) {
                val currentMode = Settings.System.getInt(
                    resolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                )
                prefs.edit().putInt(HBMConstants.PREF_HBM_SAVED_BRIGHTNESS_MODE, currentMode).apply()
                dlog(TAG, "Enabling HBM: saved mode=$currentMode (first time)")
            } else {
                dlog(TAG, "Enabling HBM: mode already saved, not overwriting")
            }
            
            dlog(TAG, "Setting to manual and max brightness")
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            writeLine(HBMConstants.BACKLIGHT_SYSFS_PATH, "2047")
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 255)
        } else {
            // First set to AUTO to trigger brightness refresh based on ambient light
            dlog(TAG, "Disabling HBM: triggering brightness refresh via AUTO mode")
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )

            // Wait for system to read ambient light and calculate brightness
            Thread.sleep(1000)

            // Then restore the original brightness mode (AUTO or MANUAL)
            val savedMode = prefs.getInt(
                HBMConstants.PREF_HBM_SAVED_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
            dlog(TAG, "Restoring saved brightness mode: $savedMode")
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                savedMode
            )
            
            // Clear saved mode after restoring
            prefs.edit().remove(HBMConstants.PREF_HBM_SAVED_BRIGHTNESS_MODE).apply()
            dlog(TAG, "Cleared saved brightness mode")
        }

        prefs.edit().putBoolean(HBMConstants.PREF_HBM_KEY, enable).apply()
        dlog(TAG, "HBM ${if (enable) "enabled" else "disabled"} successfully")
        return true
    }

    fun isHBMEnabled(): Boolean {
        val enabled = getFileValueAsBoolean(HBMConstants.HBM_SYSFS_PATH, false)
        dlog(TAG, "isHBMEnabled: $enabled")
        return enabled
    }
}