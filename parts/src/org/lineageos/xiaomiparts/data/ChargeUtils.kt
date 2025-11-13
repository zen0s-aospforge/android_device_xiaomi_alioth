package org.lineageos.xiaomiparts.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lineageos.xiaomiparts.utils.readOneLine
import org.lineageos.xiaomiparts.utils.writeLine
import org.lineageos.xiaomiparts.utils.Logging

class ChargeUtils private constructor(context: Context) {

    private val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    suspend fun isBypassChargeEnabled(): Boolean = withContext(Dispatchers.IO) {
        sharedPrefs.getBoolean(PREF_BYPASS_CHARGE, false)
    }

    suspend fun enableBypassCharge(enable: Boolean) = withContext(Dispatchers.IO) {
        try {
            writeLine(BYPASS_CHARGE_NODE, if (enable) "1" else "0")
            sharedPrefs.edit().putBoolean(PREF_BYPASS_CHARGE, enable).apply()
        } catch (e: Exception) {
            Logging.e(TAG, "Failed to write bypass charge status", e)
        }
    }

    private suspend fun isNodeAccessible(node: String): Boolean = withContext(Dispatchers.IO) {
        try {
            readOneLine(node)
            true
        } catch (e: Exception) {
            Logging.e(TAG, "Node $node not accessible", e)
            false
        }
    }

    suspend fun isBypassChargeSupported(): Boolean {
        return isNodeAccessible(BYPASS_CHARGE_NODE)
    }

    companion object {
        private const val TAG = "Charge"
        const val BYPASS_CHARGE_NODE = "/sys/class/power_supply/battery/input_suspend"
        const val PREF_BYPASS_CHARGE = "bypass_charge"

        const val BYPASS_DISABLED = 0
        const val BYPASS_ENABLED = 1

        @Volatile private var instance: ChargeUtils? = null

        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance ?: ChargeUtils(context.applicationContext).also { instance = it }
                }
    }
}