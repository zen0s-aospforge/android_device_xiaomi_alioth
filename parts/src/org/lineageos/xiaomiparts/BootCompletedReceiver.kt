/*
 * SPDX-FileCopyrightText: 2018 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.xiaomiparts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.thermal.ThermalUtils
import org.lineageos.xiaomiparts.utils.enableService
import org.lineageos.xiaomiparts.utils.writeLine
import org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.Companion.DC_DIMMING_ENABLE_KEY
import org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.Companion.DC_DIMMING_NODE
import org.lineageos.xiaomiparts.hbm.HBMConstants
import org.lineageos.xiaomiparts.hbm.HBMManager

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        if (intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return

        Log.i(TAG, "Boot completed, restoring HBM, DC, and Thermal...")

        val thermalUtils = ThermalUtils.getInstance(context)
        if (thermalUtils.enabled) {
            thermalUtils.startService()
        } else {
            // Apply default thermal profile (value 20) when thermal profiles are disabled
            thermalUtils.setDefaultThermalProfile()
        }

        enableService(context)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val dcDimmingEnabled = sharedPrefs.getBoolean(DC_DIMMING_ENABLE_KEY, false)
        writeLine(DC_DIMMING_NODE, if (dcDimmingEnabled) "1" else "0")

        // Restore HBM state (only if DC Dimming is off, due to mutual exclusion)
        if (!dcDimmingEnabled) {
            val hbmEnabled = sharedPrefs.getBoolean(HBMConstants.PREF_HBM_KEY, false)
            if (hbmEnabled) {
                Log.i(TAG, "Restoring HBM enabled state")
                HBMManager.setHBMEnabled(context, true)
            }
        }
    }

    companion object {
        private const val TAG = "XiaomiParts-BCR"
    }
}