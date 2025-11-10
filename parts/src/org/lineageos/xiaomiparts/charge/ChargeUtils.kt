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

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.utils.readOneLine
import org.lineageos.xiaomiparts.utils.writeLine
import org.lineageos.xiaomiparts.utils.Logging

class ChargeUtils(context: Context) {

    private val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun isBypassChargeEnabled(): Boolean {
        return try {
            val value = readOneLine(BYPASS_CHARGE_NODE)
            value == "1"
        } catch (e: Exception) {
            Logging.e(TAG, "Failed to read bypass charge status", e)
            false
        }
    }

    fun enableBypassCharge(enable: Boolean) {
        try {
            writeLine(BYPASS_CHARGE_NODE, if (enable) "1" else "0")
            sharedPrefs.edit().putBoolean(PREF_BYPASS_CHARGE, enable).apply()
        } catch (e: Exception) {
            Logging.e(TAG, "Failed to write bypass charge status", e)
        }
    }

    private fun isNodeAccessible(node: String): Boolean {
        return try {
            readOneLine(node)
            true
        } catch (e: Exception) {
            Logging.e(TAG, "Node $node not accessible", e)
            false
        }
    }

    fun isBypassChargeSupported(): Boolean {
        return isNodeAccessible(BYPASS_CHARGE_NODE)
    }

    companion object {
        private const val TAG = "Charge"
        const val BYPASS_CHARGE_NODE = "/sys/class/power_supply/battery/input_suspend"
        private const val PREF_BYPASS_CHARGE = "bypass_charge"

        // Bypass modes
        const val BYPASS_DISABLED = 0
        const val BYPASS_ENABLED = 1
    }
}
