/*
* Copyright (C) 2018 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it is useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.lineageos.xiaomiparts.display

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.utils.writeLine
import java.io.File

import org.lineageos.xiaomiparts.hbm.HBMManager
import org.lineageos.xiaomiparts.hbm.HBMModeTileService
import org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.Companion.DC_DIMMING_ENABLE_KEY
import org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.Companion.DC_DIMMING_NODE

class DcDimmingTileService : TileService() {

    private var hbmFile: File? = null

    private val screenStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPrefs.edit().putBoolean(DC_DIMMING_ENABLE_KEY, false).apply()
                updateUI(false)

                disableHBM()
            }
        }
    }

    private fun updateUI(enabled: Boolean) {
        qsTile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    private fun disableHBM() {
        // Update HBM preference to false immediately for instant UI sync
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putBoolean(HBMManager.PREF_HBM_KEY, false).apply()
        
        HBMManager.disableHBM(this, HBMManager.HBMOwner.MANUAL)

        hbmFile?.setReadOnly()

        HBMModeTileService.updateTile(this, false)
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
        hbmFile = File("/sys/class/drm/card0/card0-DSI-1/disp_param")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenStateReceiver)
    }

    override fun onStartListening() {
        super.onStartListening()
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        updateUI(sharedPrefs.getBoolean(DC_DIMMING_ENABLE_KEY, false))
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()

        val currentState = qsTile.state
        val newState = if (currentState == Tile.STATE_ACTIVE) {
            Tile.STATE_INACTIVE
        } else {
            Tile.STATE_ACTIVE
        }
        val newEnabledState = (newState == Tile.STATE_ACTIVE)

        // Update UI instantly for responsive feel
        updateUI(newEnabledState)
        
        // Update preference immediately so other components see the new state
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPrefs.edit().putBoolean(DC_DIMMING_ENABLE_KEY, newEnabledState).apply()

        // Process actual system changes in background (no delay needed for DC Dimming)
        Thread {
            try {
                writeLine(DC_DIMMING_NODE, if (newEnabledState) "1" else "0")

                if (newEnabledState) {
                    disableHBM()
                } else {
                    hbmFile?.setWritable(true)
                }

            } catch (e: Exception) {
                // Revert UI and preference if operation failed
                updateUI(currentState == Tile.STATE_ACTIVE)
                sharedPrefs.edit().putBoolean(DC_DIMMING_ENABLE_KEY, currentState == Tile.STATE_ACTIVE).apply()
            }
        }.start()
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