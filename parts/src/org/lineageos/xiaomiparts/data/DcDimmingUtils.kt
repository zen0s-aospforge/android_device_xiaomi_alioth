package org.lineageos.xiaomiparts.data

import android.content.Context
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.data.PREF_HBM_KEY
import org.lineageos.xiaomiparts.services.HBMModeTileService
import org.lineageos.xiaomiparts.utils.Logging
import org.lineageos.xiaomiparts.utils.fileExists
import org.lineageos.xiaomiparts.utils.writeLine
import java.io.File
import org.lineageos.xiaomiparts.data.DC_DIMMING_NODE

class DcDimmingUtils private constructor(context: Context) {

    private val appContext = context.applicationContext
    private var hbmFile: File? = File(HBM_FILE)

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext)

    suspend fun isDcDimmingSupported(): Boolean {
        return fileExists(DC_DIMMING_NODE)
    }

    fun isDcDimmingEnabled(): Boolean {
        return sharedPrefs.getBoolean(PREF_DC_DIMMING_KEY, false)
    }

    suspend fun setDcDimmingEnabled(enabled: Boolean) {
        Logging.d(TAG, "Setting DC Dimming: $enabled")
        
        sharedPrefs.edit().putBoolean(PREF_DC_DIMMING_KEY, enabled).apply()

        try {
            writeLine(DC_DIMMING_NODE, if (enabled) "1" else "0")
            
            if (enabled) {
                disableHBM()
            } else {
                hbmFile?.setWritable(true)
            }
        } catch (e: Exception) {
            Logging.e(TAG, "Failed to set DC Dimming", e)
            
            sharedPrefs.edit().putBoolean(PREF_DC_DIMMING_KEY, !enabled).apply()
        }
    }

    private suspend fun disableHBM() {
        Logging.d(TAG, "Disabling HBM due to DC dimming enable")

        val hbmManager = HBMManager.getInstance(appContext)

        sharedPrefs.edit().putBoolean(PREF_HBM_KEY, false).apply()

        hbmManager.disableHBM(HBMManager.HBMOwner.MANUAL)
        hbmFile?.setReadOnly()
        HBMModeTileService.updateTile(appContext, false)
    }

    companion object {
        private const val TAG = "DcDimmingUtils"

        private const val HBM_FILE = "/sys/class/drm/card0/card0-DSI-1/disp_param"

        @Volatile private var instance: DcDimmingUtils? = null

        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance ?: DcDimmingUtils(context.applicationContext).also { instance = it }
                }
    }
}