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

package org.lineageos.xiaomiparts.revanced

import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import org.lineageos.xiaomiparts.R

class ReVancedSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var revancedToggle: SwitchPreferenceCompat

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.revanced_settings, rootKey)

        revancedToggle = findPreference("revanced_enable")!!
        revancedToggle.isChecked = ReVancedManager.isEnabled()

        revancedToggle.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            val success = ReVancedManager.setEnabled(enabled)
            
            if (success) {
                Toast.makeText(
                    context,
                    R.string.revanced_restart_required,
                    Toast.LENGTH_LONG
                ).show()
                true
            } else {
                Toast.makeText(
                    context,
                    R.string.revanced_toggle_failed,
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        revancedToggle.isChecked = ReVancedManager.isEnabled()
    }
}
