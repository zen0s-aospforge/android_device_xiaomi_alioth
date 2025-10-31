package org.lineageos.xiaomiparts.hbm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.util.Log
import androidx.preference.PreferenceManager

class HBMBootReceiver : BroadcastReceiver() {

    private val TAG = "HBMBootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            return
        }

        Log.i(TAG, "Boot completed - initializing HBM")

        HBMManager.initializeOnBoot(context)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val autoHBMEnabled = prefs.getBoolean(HBMManager.PREF_AUTO_HBM_KEY, false)

        if (autoHBMEnabled) {
            Log.i(TAG, "Auto HBM is enabled, starting service")
            val serviceIntent = Intent(context, AutoHBMService::class.java)
            context.startServiceAsUser(serviceIntent, UserHandle.CURRENT)
        } else {
            Log.i(TAG, "Auto HBM is disabled, not starting service")
        }

        Log.i(TAG, "HBM initialization complete")
    }
}
