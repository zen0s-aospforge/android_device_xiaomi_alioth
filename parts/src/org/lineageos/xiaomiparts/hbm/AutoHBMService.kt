package org.lineageos.xiaomiparts.hbm

import android.app.KeyguardManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import androidx.preference.PreferenceManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import org.lineageos.xiaomiparts.hbm.HBMConstants.PREF_AUTO_HBM_THRESHOLD_KEY
import org.lineageos.xiaomiparts.hbm.HBMConstants.PREF_HBM_DISABLE_TIME_KEY


class AutoHBMService : Service() {

    private lateinit var mExecutorService: ExecutorService
    private lateinit var mSensorManager: SensorManager
    private var mLightSensor: Sensor? = null
    private lateinit var mSharedPrefs: SharedPreferences

    private var dcDimmingEnabled = false

    fun activateLightSensorRead() {
        submit {
            mSensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            mLightSensor?.let {
                mSensorManager.registerListener(
                    mSensorEventListener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }
    }

    fun deactivateLightSensorRead() {
        submit {
            if (::mSensorManager.isInitialized) {
                mSensorManager.unregisterListener(mSensorEventListener)
            }
            mAutoHBMActive = false
            HBMManager.setHBMEnabled(this, false)
        }
    }

    private val mSensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lux = event.values[0]
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val keyguardShowing = km.inKeyguardRestrictedInputMode()
            val luxThreshold = mSharedPrefs.getString(
                PREF_AUTO_HBM_THRESHOLD_KEY, "20000"
            )?.toFloatOrNull() ?: 20000f

            val timeToDisableHBM = mSharedPrefs.getString(
                PREF_HBM_DISABLE_TIME_KEY, "1"
            )?.toLongOrNull() ?: 1L

            if (lux > luxThreshold) {
                if ((!mAutoHBMActive || !HBMManager.isHBMEnabled()) && !keyguardShowing && !dcDimmingEnabled) {
                    mAutoHBMActive = true
                    HBMManager.setHBMEnabled(this@AutoHBMService, true)
                }
            } else if (lux < luxThreshold) {
                if (mAutoHBMActive) {
                    mExecutorService.submit {
                        try {
                            Thread.sleep(timeToDisableHBM * 1000)
                        } catch (e: InterruptedException) {
                        }
                    
                        if (lux < luxThreshold) {
                            mAutoHBMActive = false
                            HBMManager.setHBMEnabled(this@AutoHBMService, false)
                        }
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        }
    }

    private val mScreenStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> activateLightSensorRead()
                Intent.ACTION_SCREEN_OFF -> deactivateLightSensorRead()
            }
        }
    }

    override fun onCreate() {
        mExecutorService = Executors.newSingleThreadExecutor()

        val screenStateFilter = IntentFilter(Intent.ACTION_SCREEN_ON).apply {
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(mScreenStateReceiver, screenStateFilter)

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        if (pm.isInteractive) {
            activateLightSensorRead()
        }
    }

    private fun submit(runnable: Runnable): Future<*> {
        return mExecutorService.submit(runnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mScreenStateReceiver)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isInteractive) {
            deactivateLightSensorRead()
        }
        mExecutorService.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {

        @JvmStatic
        private var mAutoHBMActive = false
    }
}