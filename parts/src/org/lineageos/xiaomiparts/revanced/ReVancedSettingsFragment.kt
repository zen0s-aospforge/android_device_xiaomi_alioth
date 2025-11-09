/*
 * Copyright (C) 2025 The LineageOS Project
 * Licensed under the Apache License, Version 2.0
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
                Toast.makeText(context, R.string.revanced_restart_required, Toast.LENGTH_LONG).show()
                true
            } else {
                Toast.makeText(context, R.string.revanced_toggle_failed, Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        revancedToggle.isChecked = ReVancedManager.isEnabled()
    }
}
