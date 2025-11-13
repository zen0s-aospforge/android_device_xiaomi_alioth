package org.lineageos.xiaomiparts.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lineageos.xiaomiparts.data.ThermalUtils
import org.lineageos.xiaomiparts.utils.Logging
import org.lineageos.xiaomiparts.data.DcDimmingUtils
import org.lineageos.xiaomiparts.data.ChargeUtils
import org.lineageos.xiaomiparts.data.HBMManager

class BootCompletedReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Logging.d(TAG, "Received intent: ${intent.action}")
        if (intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return

        Logging.i(TAG, "Boot completed, restoring settings...")
        
        val pendingResult = goAsync() 
        
        scope.launch {
            try {

                val dcDimmingUtils = DcDimmingUtils.getInstance(context)
                val dcDimmingEnabled = dcDimmingUtils.isDcDimmingEnabled()
                dcDimmingUtils.setDcDimmingEnabled(dcDimmingEnabled)
                
                val chargeUtils = ChargeUtils.getInstance(context)
                val bypassEnabled = chargeUtils.isBypassChargeEnabled()
                if (bypassEnabled) {
                    chargeUtils.enableBypassCharge(true)
                }

                Logging.i(TAG, "Initializing HBM")
                val hbmManager = HBMManager.getInstance(context)
                hbmManager.initializeOnBoot()

                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val autoHBMEnabled = prefs.getBoolean(HBMManager.PREF_AUTO_HBM_KEY, false)

                if (autoHBMEnabled) {
                    Logging.i(TAG, "Auto HBM is enabled, starting service")
                    AutoHBMService.enableService(context, true)
                }

            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "Boot"
    }
}