package org.lineageos.xiaomiparts.hbm

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.preference.PreferenceManager

class HBMModeTileService : TileService(), HBMManager.HBMStateListener {

    private val TAG = "HBMTileService"
    
    @Volatile
    private var isOperationInProgress = false

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Tile created")
        HBMManager.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Tile destroyed")
        HBMManager.removeListener(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.i(TAG, "Tile listening")
        
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val enabled = prefs.getBoolean(HBMManager.PREF_HBM_KEY, false)
        updateTileUI(enabled)
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.i(TAG, "Tile stopped listening")
    }

    override fun onClick() {
        super.onClick()
        
        // Prevent multiple simultaneous operations
        if (isOperationInProgress) {
            Log.w(TAG, "Operation already in progress, ignoring rapid click")
            return
        }
        
        val currentState = qsTile.state
        val newEnabled = currentState != Tile.STATE_ACTIVE
        
        Log.i(TAG, "Tile clicked: toggling HBM to $newEnabled")
        
        isOperationInProgress = true
        
        // Update preference immediately for instant sync
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putBoolean(HBMManager.PREF_HBM_KEY, newEnabled).apply()
        
        // Update UI instantly based on preference
        updateTileUI(newEnabled)
        
        // Execute operation in background (UI already updated)
        if (newEnabled) {
            HBMManager.enableHBM(applicationContext, HBMManager.HBMOwner.MANUAL) { success ->
                isOperationInProgress = false
                if (!success) {
                    Log.w(TAG, "Failed to enable HBM, reverting")
                    prefs.edit().putBoolean(HBMManager.PREF_HBM_KEY, false).apply()
                    updateTileUI(false)
                }
            }
        } else {
            HBMManager.disableHBM(applicationContext, HBMManager.HBMOwner.MANUAL) { success ->
                isOperationInProgress = false
                if (!success) {
                    Log.w(TAG, "Failed to disable HBM, reverting")
                    prefs.edit().putBoolean(HBMManager.PREF_HBM_KEY, true).apply()
                    updateTileUI(true)
                }
            }
        }
    }

    override fun onHBMStateChanged(enabled: Boolean, owner: HBMManager.HBMOwner) {
        Log.i(TAG, "HBM state changed: enabled=$enabled, owner=$owner")
        updateTileUI(enabled)
    }

    private fun updateTileUI(enabled: Boolean) {
        qsTile?.let { tile ->
            tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.updateTile()
        }
    }

    companion object {
        @JvmStatic
        fun updateTile(context: Context, enabled: Boolean) {
            try {
                requestListeningState(
                    context,
                    ComponentName(context, HBMModeTileService::class.java)
                )
            } catch (e: Exception) {
                Log.e("HBMTileService", "Failed to request tile update", e)
            }
        }
    }
}
