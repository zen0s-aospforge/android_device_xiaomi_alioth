/*
 * SPDX-FileCopyrightText: 2018 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.xiaomiparts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.SystemProperties
import android.util.Log
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.thermal.ThermalUtils
import org.lineageos.xiaomiparts.utils.FileUtils

/** Everything begins at boot. */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        if (intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return

        Log.i(TAG, "Boot completed, starting services")
        ThermalUtils.getInstance(context).startService()
        FileUtils.enableService(context)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val dcDimmingEnabled = sharedPrefs.getBoolean(DC_DIMMING_ENABLE_KEY, false)
        FileUtils.writeLine(DC_DIMMING_NODE, if (dcDimmingEnabled) "1" else "0")
    }

    companion object {
        private const val TAG = "XiaomiParts-BCR"
        private const val DC_DIMMING_ENABLE_KEY = "dc_dimming_enable"
        private const val DC_DIMMING_NODE = "/sys/devices/platform/soc/soc:qcom,dsi-display-primary/dimlayer_exposure"
    }
}
