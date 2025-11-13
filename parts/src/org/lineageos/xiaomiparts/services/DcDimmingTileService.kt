package org.lineageos.xiaomiparts.services

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import org.lineageos.xiaomiparts.data.HBMManager
import org.lineageos.xiaomiparts.services.HBMModeTileService
import org.lineageos.xiaomiparts.data.PREF_DC_DIMMING_KEY
import org.lineageos.xiaomiparts.data.PREF_HBM_KEY
import org.lineageos.xiaomiparts.data.DcDimmingUtils

class DcDimmingTileService : TileService() {

    private lateinit var hbmManager: HBMManager
    private var hbmFile: File? = null
    
    private val dcDimmingUtils: DcDimmingUtils by lazy {
        DcDimmingUtils.getInstance(this)
    }

    private val tileScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val screenStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                tileScope.launch {
                    dcDimmingUtils.setDcDimmingEnabled(false)
                }
                updateUI(false)

            }
        }
    }

    private fun updateUI(enabled: Boolean) {
        qsTile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    override fun onCreate() {
        super.onCreate()
        hbmManager = HBMManager.getInstance(this)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
        hbmFile = File("/sys/class/drm/card0/card0-DSI-1/disp_param")
    }

    override fun onDestroy() {
        super.onDestroy()
        tileScope.cancel()
        unregisterReceiver(screenStateReceiver)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateUI(dcDimmingUtils.isDcDimmingEnabled())
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()

        val newState = qsTile.state == Tile.STATE_INACTIVE
        
        tileScope.launch {
            dcDimmingUtils.setDcDimmingEnabled(newState)
        }
        updateUI(newState)

    }

    companion object {
        @JvmStatic
        fun updateTile(context: Context) {
            requestListeningState(
                context,
                ComponentName(context, DcDimmingTileService::class.java)
            )
        }
    }
}