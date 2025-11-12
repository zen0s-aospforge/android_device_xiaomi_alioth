/*
 * Copyright (C) 2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.xiaomiparts.display

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.SwitchPreferenceCompat
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.R
import org.lineageos.xiaomiparts.hbm.HBMManager
import org.lineageos.xiaomiparts.hbm.HBMModeTileService
import org.lineageos.xiaomiparts.utils.dlog
import org.lineageos.xiaomiparts.utils.fileExists
import org.lineageos.xiaomiparts.utils.writeLine
import java.io.File

class DcDimmingSettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

    private var mDcDimmingPreference: SwitchPreferenceCompat? = null
    private var hbmFile: File? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        dlog(TAG, "onCreatePreferences")
        setPreferencesFromResource(R.xml.dcdimming_settings, rootKey)

        mDcDimmingPreference = findPreference(DC_DIMMING_ENABLE_KEY)

        if (fileExists(DC_DIMMING_NODE)) {
            dlog(TAG, "DC dimming supported")
            mDcDimmingPreference?.isEnabled = true
            mDcDimmingPreference?.onPreferenceChangeListener = this
        } else {
            dlog(TAG, "DC dimming not supported")
            mDcDimmingPreference?.setSummary(R.string.dc_dimming_enable_summary_not_supported)
            mDcDimmingPreference?.isEnabled = false
        }

        hbmFile = File("/sys/class/drm/card0/card0-DSI-1/disp_param")
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        when (preference.key) {
            DC_DIMMING_ENABLE_KEY -> {
                val enabled = newValue as? Boolean ?: return false
                dlog(TAG, "DC dimming preference changed: enabled=$enabled")

                writeLine(DC_DIMMING_NODE, if (enabled) "1" else "0")

                if (enabled) {
                    disableHBM()
                } else {
                    hbmFile?.setWritable(true)
                }
                return true
            }
        }
        return true
    }

    private fun disableHBM() {
        dlog(TAG, "Disabling HBM due to DC dimming enable")
        
        // Update HBM preference to false immediately for instant UI sync
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        prefs.edit().putBoolean(HBMManager.PREF_HBM_KEY, false).apply()
        
        HBMManager.disableHBM(activity, HBMManager.HBMOwner.MANUAL)
        hbmFile?.setReadOnly()
        HBMModeTileService.updateTile(activity, false)
    }

    companion object {
        private const val TAG = "DcDimmingSettingsFragment"
        
        const val DC_DIMMING_ENABLE_KEY = "dc_dimming_enable"

        const val DC_DIMMING_NODE = "/sys/devices/platform/soc/soc:qcom,dsi-display-primary/dimlayer_exposure"
    }
}