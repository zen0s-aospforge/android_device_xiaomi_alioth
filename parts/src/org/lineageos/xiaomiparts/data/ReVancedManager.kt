package org.lineageos.xiaomiparts.data

import android.content.Context
import android.os.SystemProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lineageos.xiaomiparts.utils.Logging

class ReVancedManager private constructor(context: Context) {

    private val appContext = context.applicationContext

    suspend fun isEnabled(): Boolean = withContext(Dispatchers.IO) {
        SystemProperties.getBoolean(PROPERTY_REVANCED_ENABLED, DEFAULT_ENABLED)
    }

    suspend fun setEnabled(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            SystemProperties.set(PROPERTY_REVANCED_ENABLED, if (enabled) "true" else "false")
            Logging.log(TAG, "ReVanced ${if (enabled) "enabled" else "disabled"}")
            true
        } catch (e: Exception) {
            Logging.log(TAG, "Failed to set ReVanced state: ${e.message}")
            false
        }
    }

    companion object {
        private const val TAG = "ReVancedManager"
        private const val PROPERTY_REVANCED_ENABLED = "persist.sys.revan.mod"
        private const val DEFAULT_ENABLED = true

        @Volatile private var instance: ReVancedManager? = null

        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance ?: ReVancedManager(context.applicationContext).also { instance = it }
                }
    }
}