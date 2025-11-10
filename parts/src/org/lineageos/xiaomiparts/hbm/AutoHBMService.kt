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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.display.DcDimmingSettingsFragment.Companion.DC_DIMMING_ENABLE_KEY
import org.lineageos.xiaomiparts.utils.Logging

class AutoHBMService : Service() {

    private val TAG = "AutoHBM"
    
    private lateinit var sensorManager: SensorManager
    private lateinit var powerManager: PowerManager
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var prefs: SharedPreferences
    
    private var lightSensor: Sensor? = null
    private var currentLux = 0f
    
    private val handler = Handler(Looper.getMainLooper())
    private var disableHBMRunnable: Runnable? = null
    
    private var autoHBMActive = false
    
    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            currentLux = event.values[0]
            
            val luxThreshold = prefs.getString(
                HBMManager.PREF_AUTO_HBM_THRESHOLD_KEY,
                "20000"
            )?.toFloatOrNull() ?: 20000f
            
            val disableDelaySeconds = prefs.getString(
                HBMManager.PREF_HBM_DISABLE_TIME_KEY,
                "1"
            )?.toLongOrNull() ?: 1L
            
            val isLocked = keyguardManager.isKeyguardLocked
            val dcDimmingEnabled = prefs.getBoolean(DC_DIMMING_ENABLE_KEY, false)
            
            if (currentLux > luxThreshold) {
                disableHBMRunnable?.let {
                    handler.removeCallbacks(it)
                    disableHBMRunnable = null
                }
                
                if (!isLocked && !dcDimmingEnabled && !autoHBMActive) {
                    Logging.i(TAG, "Lux $currentLux > $luxThreshold, enabling Auto HBM")
                    HBMManager.enableHBM(applicationContext, HBMManager.HBMOwner.AUTO_SERVICE) { success ->
                        if (success) {
                            autoHBMActive = true
                            Logging.i(TAG, "Auto HBM enabled successfully")
                        }
                    }
                }
            }
            else if (currentLux < luxThreshold && autoHBMActive) {
                disableHBMRunnable?.let {
                    handler.removeCallbacks(it)
                }
                
                disableHBMRunnable = Runnable {
                    if (currentLux < luxThreshold && autoHBMActive) {
                        Logging.i(TAG, "Lux $currentLux < $luxThreshold after delay, disabling Auto HBM")
                        HBMManager.disableHBM(applicationContext, HBMManager.HBMOwner.AUTO_SERVICE) { success ->
                            if (success) {
                                autoHBMActive = false
                                Logging.i(TAG, "Auto HBM disabled successfully")
                            }
                        }
                    } else {
                        Logging.i(TAG, "Lux increased during delay, keeping Auto HBM enabled")
                    }
                    disableHBMRunnable = null
                }
                
                handler.postDelayed(disableHBMRunnable!!, disableDelaySeconds * 1000)
                Logging.i(TAG, "Scheduled Auto HBM disable in ${disableDelaySeconds}s")
            }
        }
        
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        }
    }
    
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> {
                    Logging.i(TAG, "Screen ON - activating light sensor")
                    activateLightSensor()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    Logging.i(TAG, "Screen OFF - deactivating light sensor")
                    deactivateLightSensor()
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Logging.i(TAG, "AutoHBMService created")
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor == null) {
            Logging.e(TAG, "No light sensor found! Auto HBM will not work")
        }
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
        
        if (powerManager.isInteractive) {
            Logging.i(TAG, "Screen is on at service start, activating sensor")
            activateLightSensor()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logging.i(TAG, "AutoHBMService started")
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Logging.i(TAG, "AutoHBMService destroyed")
        
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            Logging.e(TAG, "Error unregistering receiver", e)
        }
        
        deactivateLightSensor()
        
        if (autoHBMActive) {
            Logging.i(TAG, "Service stopping - disabling Auto HBM")
            HBMManager.disableHBM(applicationContext, HBMManager.HBMOwner.AUTO_SERVICE)
            autoHBMActive = false
        }
        
        disableHBMRunnable?.let {
            handler.removeCallbacks(it)
            disableHBMRunnable = null
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun activateLightSensor() {
        lightSensor?.let { sensor ->
            val registered = sensorManager.registerListener(
                lightSensorListener,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            if (registered) {
                Logging.i(TAG, "Light sensor activated")
            } else {
                Logging.e(TAG, "Failed to register light sensor listener")
            }
        }
    }
    
    private fun deactivateLightSensor() {
        sensorManager.unregisterListener(lightSensorListener)
        Logging.i(TAG, "Light sensor deactivated")
        
        disableHBMRunnable?.let {
            handler.removeCallbacks(it)
            disableHBMRunnable = null
        }
        
        if (autoHBMActive) {
            Logging.i(TAG, "Screen off - disabling Auto HBM")
            HBMManager.disableHBM(applicationContext, HBMManager.HBMOwner.AUTO_SERVICE)
            autoHBMActive = false
        }
    }
}
