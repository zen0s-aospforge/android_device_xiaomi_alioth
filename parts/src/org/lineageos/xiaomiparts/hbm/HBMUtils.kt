package org.lineageos.xiaomiparts.hbm

import android.content.Context
import android.provider.Settings
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
}

object HBMManager {

    private const val TAG = "HBMManager"

    fun setHBMEnabled(context: Context, enable: Boolean): Boolean {
        dlog(TAG, "setHBMEnabled: enable=$enable")
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val resolver = context.contentResolver

        val dcDimmingEnabled = prefs.getBoolean(DC_DIMMING_ENABLE_KEY, false)

        if (enable && dcDimmingEnabled) {
            dlog(TAG, "Cannot enable HBM: DC dimming is enabled")
            return false
        }

        val success = writeLine(HBMConstants.HBM_SYSFS_PATH, if (enable) "0x10000" else "0xF0000")

        if (!success) {
            dlog(TAG, "Failed to write to HBM sysfs")
            return false
        }

        if (enable) {
            dlog(TAG, "Enabling HBM: setting brightness mode to manual and max brightness")
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            writeLine(HBMConstants.BACKLIGHT_SYSFS_PATH, "2047")
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 255)
        } else {
            dlog(TAG, "Disabling HBM: resetting brightness mode")
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )

            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
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