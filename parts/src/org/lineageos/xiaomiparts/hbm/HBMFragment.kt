/*
 * Copyright (C) 2016 The OmniROM Project
 * ... (and other headers)
 */
package org.lineageos.xiaomiparts.hbm

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.PreferenceManager
import androidx.preference.TwoStatePreference
import org.lineageos.xiaomiparts.R
import org.lineageos.xiaomiparts.utils.dlog
import org.lineageos.xiaomiparts.utils.enableService
import org.lineageos.xiaomiparts.hbm.HBMConstants.PREF_AUTO_HBM_KEY
import org.lineageos.xiaomiparts.hbm.HBMConstants.PREF_HBM_KEY

class HBMFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

    private var mAutoHBMSwitch: TwoStatePreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        dlog(TAG, "onCreatePreferences")
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        addPreferencesFromResource(R.xml.hbm_settings)

        findPreference<TwoStatePreference>(PREF_HBM_KEY)?.apply {
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as? Boolean ?: false
                dlog(TAG, "HBM preference changed: enabled=$enabled")
                
                val success = HBMManager.setHBMEnabled(activity, enabled)

                if (success) {
                    HBMModeTileService.updateTile(activity, enabled)
                }
                
                success
            }
        }

        mAutoHBMSwitch = findPreference<TwoStatePreference>(PREF_AUTO_HBM_KEY)?.apply {
            onPreferenceChangeListener = this@HBMFragment
            isChecked = prefs.getBoolean(PREF_AUTO_HBM_KEY, false)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (preference === mAutoHBMSwitch) {
            val enabled = newValue as? Boolean ?: return false
            dlog(TAG, "Auto HBM preference changed: enabled=$enabled")
            
            PreferenceManager.getDefaultSharedPreferences(activity).edit()
                .putBoolean(PREF_AUTO_HBM_KEY, enabled)
                .apply()
            
            enableService(activity)
            
            return true
        }
        return false
    }

    companion object {
        private const val TAG = "HBMFragment"

        @JvmStatic
        fun isAUTOHBMEnabled(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_AUTO_HBM_KEY, false)
        }
    }
}