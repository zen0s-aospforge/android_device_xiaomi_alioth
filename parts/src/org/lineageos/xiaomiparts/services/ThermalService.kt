package org.lineageos.xiaomiparts.services

import android.app.ActivityTaskManager
import android.app.Service
import android.app.TaskStackListener
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.lineageos.xiaomiparts.utils.Logging
import org.lineageos.xiaomiparts.data.ThermalUtils

/** Service to monitor current top (foreground) app and set thermal profile accordingly. */
class ThermalService : Service() {

    private lateinit var thermalUtils: ThermalUtils

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentApp = ""
        set(value) {
            if (field == value) return
            field = value
            Logging.d(TAG, "Top app changed: $value")
            setThermalProfile()
        }

    private var screenOn = true
        set(value) {
            if (field == value) return
            field = value
            Logging.d(TAG, "Screen state changed: $value")
            setThermalProfile()
        }

    private val taskListener =
        object : TaskStackListener() {
            override fun onTaskStackChanged() {
                runCatching {
                    val focusedTask = ActivityTaskManager.getService().focusedRootTaskInfo
                    focusedTask?.topActivity?.let { currentApp = it.packageName }
                }
            }
        }

    private val intentReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_OFF -> screenOn = false
                    Intent.ACTION_SCREEN_ON -> screenOn = true
                }
            }
        }

    override fun onCreate() {
        Logging.d(TAG, "Creating service")
        thermalUtils = ThermalUtils.getInstance(this)
        super.onCreate()
    }

    override fun onDestroy() {
        Logging.d(TAG, "Destroying service")
        serviceScope.cancel()
        unregisterReceiver(intentReceiver)
        runCatching { ActivityTaskManager.getService().unregisterTaskStackListener(taskListener) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logging.d(TAG, "Starting service")
        runCatching { ActivityTaskManager.getService().registerTaskStackListener(taskListener) }
        registerReceiver(
            intentReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            },
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun setThermalProfile() {
        serviceScope.launch {
            if (screenOn) {
                thermalUtils.setThermalProfile(currentApp)
            } else {
                thermalUtils.setDefaultThermalProfile()
            }
        }
    }

    companion object {
        private const val TAG = "ThermalService"
    }
}
