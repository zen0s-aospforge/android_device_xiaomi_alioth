/*
* Copyright (C) 2018 The OmniROM Project
* ... (and other headers)
*/
package org.lineageos.xiaomiparts.hbm

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.display.DcDimmingTileService
import org.lineageos.xiaomiparts.utils.dlog

import org.lineageos.xiaomiparts.hbm.HBMConstants.PREF_HBM_KEY

class HBMModeTileService : TileService() {

    private val screenStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                // First check if HBM is actually enabled in hardware
                if (!HBMManager.isHBMEnabled()) {
                    dlog(TAG, "Screen off: HBM already disabled, skipping")
                    return
                }
                
                // Only disable HBM if it was NOT manually enabled by user
                // Check the HBM preference to determine if user manually enabled it
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val manuallyEnabled = prefs.getBoolean(PREF_HBM_KEY, false)
                
                if (!manuallyEnabled) {
                    // HBM was auto-enabled (by AutoHBMService or temporary), safe to disable
                    dlog(TAG, "Screen off: disabling auto-enabled HBM")
                    HBMManager.setHBMEnabled(context, false)
                    updateUI(false)
                } else {
                    // User manually enabled HBM, keep it on
                    dlog(TAG, "Screen off: keeping manually-enabled HBM active")
                }
            }
        }
    }

    private fun updateUI(enabled: Boolean) {
        dlog(TAG, "updateUI: enabled=$enabled")
        val tile = qsTile
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onCreate() {
        dlog(TAG, "onCreate")
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    override fun onDestroy() {
        dlog(TAG, "onDestroy")
        super.onDestroy()
        unregisterReceiver(screenStateReceiver)
    }

    override fun onStartListening() {
        dlog(TAG, "onStartListening")
        super.onStartListening()
        // Check preference instead of sysfs for instant UI update
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val enabled = prefs.getBoolean(PREF_HBM_KEY, false)
        updateUI(enabled)
    }

    override fun onClick() {
        dlog(TAG, "onClick")
        super.onClick()

        val currentState = qsTile.state
        val newState = if (currentState == Tile.STATE_ACTIVE) {
            Tile.STATE_INACTIVE
        } else {
            Tile.STATE_ACTIVE
        }
        val newEnabledState = (newState == Tile.STATE_ACTIVE)

        // Check if DC Dimming is enabled, and disable it if enabling HBM
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val dcDimmingEnabled = prefs.getBoolean(
            org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.DC_DIMMING_ENABLE_KEY, 
            false
        )
        
        if (newEnabledState && dcDimmingEnabled) {
            dlog(TAG, "Disabling DC Dimming to enable HBM")
            // Disable DC Dimming
            prefs.edit().putBoolean(
                org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.DC_DIMMING_ENABLE_KEY,
                false
            ).apply()
            org.lineageos.xiaomiparts.utils.writeLine(
                org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.DC_DIMMING_NODE,
                "0"
            )
            // Update DC Dimming tile
            org.lineageos.xiaomiparts.display.DcDimmingTileService.updateTile(this)
        }

        // Update UI instantly for responsive feel
        updateUI(newEnabledState)

        // Process actual system changes in background with delay
        Thread {
            try {
                Thread.sleep(1000) // 1 second delay before writing
                val success = HBMManager.setHBMEnabled(this, newEnabledState)
                dlog(TAG, "HBM toggle result: success=$success")

                if (!success) {
                    // Revert UI if operation failed
                    updateUI(currentState == Tile.STATE_ACTIVE)
                }
            } catch (e: Exception) {
                dlog(TAG, "Error toggling HBM: ${e.message}")
                updateUI(currentState == Tile.STATE_ACTIVE)
            }
        }.start()
    }

    companion object {
        private const val TAG = "HBMModeTileService"

        @JvmStatic
        fun updateTile(context: Context, enabled: Boolean) {
            dlog(TAG, "updateTile: enabled=$enabled")
            requestListeningState(
                context,
                ComponentName(context, HBMModeTileService::class.java)
            )
        }
    }
}