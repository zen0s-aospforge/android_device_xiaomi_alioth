/*
 * Copyright (C) 2025 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.xiaomiparts.charge

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.SwitchPreferenceCompat
import org.lineageos.xiaomiparts.R

class ChargeSettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

    private lateinit var chargeUtils: ChargeUtils
    private var bypassChargePreference: SwitchPreferenceCompat? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.charge_settings, rootKey)

        chargeUtils = ChargeUtils(activity!!)
        bypassChargePreference = findPreference<SwitchPreferenceCompat>(KEY_BYPASS_CHARGE)

        val bypassChargeSupported = chargeUtils.isBypassChargeSupported()

        bypassChargePreference?.apply {
            isEnabled = bypassChargeSupported
            if (bypassChargeSupported) {
                isChecked = chargeUtils.isBypassChargeEnabled()
                onPreferenceChangeListener = this@ChargeSettingsFragment
            } else {
                setSummary(R.string.charge_bypass_unavailable)
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            KEY_BYPASS_CHARGE -> {
                val bypassValue = newValue as Boolean
                if (bypassValue) {
                    AlertDialog.Builder(activity!!)
                        .setTitle(R.string.charge_bypass_title)
                        .setMessage(R.string.charge_bypass_warning)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            chargeUtils.enableBypassCharge(true)
                            bypassChargePreference?.isChecked = true
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ ->
                            bypassChargePreference?.isChecked = false
                        }
                        .show()
                    return false
                } else {
                    chargeUtils.enableBypassCharge(false)
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private const val KEY_BYPASS_CHARGE = "bypass_charge"
    }
}
