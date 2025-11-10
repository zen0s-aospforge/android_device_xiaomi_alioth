package org.lineageos.xiaomiparts.hbm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.utils.Logging

class HBMBootReceiver : BroadcastReceiver() {

    private val TAG = "HBMBoot"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            return
        }

        Logging.i(TAG, "Boot completed - initializing HBM")

        HBMManager.initializeOnBoot(context)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val autoHBMEnabled = prefs.getBoolean(HBMManager.PREF_AUTO_HBM_KEY, false)

        if (autoHBMEnabled) {
            Logging.i(TAG, "Auto HBM is enabled, starting service")
            val serviceIntent = Intent(context, AutoHBMService::class.java)
            context.startServiceAsUser(serviceIntent, UserHandle.CURRENT)
        } else {
            Logging.i(TAG, "Auto HBM is disabled, not starting service")
        }

        Logging.i(TAG, "HBM initialization complete")
    }
}
