/*
 * SPDX-FileCopyrightText: 2018 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.xiaomiparts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.thermal.ThermalUtils
import org.lineageos.xiaomiparts.utils.writeLine
import org.lineageos.xiaomiparts.utils.Logging
import org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.Companion.DC_DIMMING_ENABLE_KEY
import org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.Companion.DC_DIMMING_NODE

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Logging.d(TAG, "Received intent: ${intent.action}")
        if (intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return

        Logging.i(TAG, "Boot completed, restoring DC and Thermal...")

        val thermalUtils = ThermalUtils.getInstance(context)
        if (thermalUtils.enabled) {
            thermalUtils.startService()
        } else {
            // Apply default thermal profile (value 20) when thermal profiles are disabled
            thermalUtils.setDefaultThermalProfile()
        }

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val dcDimmingEnabled = sharedPrefs.getBoolean(DC_DIMMING_ENABLE_KEY, false)
        writeLine(DC_DIMMING_NODE, if (dcDimmingEnabled) "1" else "0")
    }

    companion object {
        private const val TAG = "Boot"
    }
}