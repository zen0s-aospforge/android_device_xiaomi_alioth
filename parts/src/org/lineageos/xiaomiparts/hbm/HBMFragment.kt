package org.lineageos.xiaomiparts.hbm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.UserHandle
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import org.lineageos.xiaomiparts.R
import org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.Companion.DC_DIMMING_ENABLE_KEY
import org.lineageos.xiaomiparts.display.DcDimmingTileService
import org.lineageos.xiaomiparts.utils.writeLine
import org.lineageos.xiaomiparts.utils.Logging

class HBMFragment : PreferenceFragment() {

    private val TAG = "HBMFrag"

    private var hbmPreference: SwitchPreferenceCompat? = null
    private var autoHBMPreference: SwitchPreferenceCompat? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        Logging.i(TAG, "Creating HBM preferences")
        addPreferencesFromResource(R.xml.hbm_settings)

        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        hbmPreference = findPreference<SwitchPreferenceCompat>(HBMManager.PREF_HBM_KEY)?.apply {
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as? Boolean ?: false
                Logging.i(TAG, "Manual HBM preference changed: $enabled")
                
                handleHBMToggle(enabled)
            }
        }

        autoHBMPreference = findPreference<SwitchPreferenceCompat>(HBMManager.PREF_AUTO_HBM_KEY)?.apply {
            isChecked = prefs.getBoolean(HBMManager.PREF_AUTO_HBM_KEY, false)
            
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as? Boolean ?: false
                Logging.i(TAG, "Auto HBM preference changed: $enabled")
                
                prefs.edit().putBoolean(HBMManager.PREF_AUTO_HBM_KEY, enabled).apply()
                
                controlAutoHBMService(enabled)
                
                true
            }
        }
    }

    private fun handleHBMToggle(enable: Boolean): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        
        if (enable) {
            val dcDimmingEnabled = prefs.getBoolean(DC_DIMMING_ENABLE_KEY, false)
            
            if (dcDimmingEnabled) {
                Logging.i(TAG, "Disabling DC Dimming to enable HBM")
                
                prefs.edit().putBoolean(DC_DIMMING_ENABLE_KEY, false).apply()
                writeLine(
                    org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.DC_DIMMING_NODE,
                    "0"
                )
                
                DcDimmingTileService.updateTile(activity)
            }
        }
        
        var result = false
        if (enable) {
            HBMManager.enableHBM(activity, HBMManager.HBMOwner.MANUAL) { success ->
                result = success
                if (!success) {
                    Logging.w(TAG, "Failed to enable HBM, reverting preference")
                    activity?.runOnUiThread {
                        hbmPreference?.isChecked = false
                    }
                }
            }
        } else {
            HBMManager.disableHBM(activity, HBMManager.HBMOwner.MANUAL) { success ->
                result = success
                if (!success) {
                    Logging.w(TAG, "Failed to disable HBM, reverting preference")
                    activity?.runOnUiThread {
                        hbmPreference?.isChecked = true
                    }
                }
            }
        }
        
        return true
    }

    private fun controlAutoHBMService(enable: Boolean) {
        val intent = Intent(activity, AutoHBMService::class.java)
        
        if (enable) {
            Logging.i(TAG, "Starting AutoHBMService")
            activity.startServiceAsUser(intent, UserHandle.CURRENT)
        } else {
            Logging.i(TAG, "Stopping AutoHBMService")
            activity.stopServiceAsUser(intent, UserHandle.CURRENT)
        }
    }

    companion object {
        @JvmStatic
        fun isAutoHBMEnabled(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(HBMManager.PREF_AUTO_HBM_KEY, false)
        }
    }
}
