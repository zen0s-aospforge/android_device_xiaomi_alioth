/*
 * SPDX-FileCopyrightText: 2020 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.xiaomiparts.thermal

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.os.UserHandle
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragment
import org.lineageos.xiaomiparts.R
import org.lineageos.xiaomiparts.thermal.ThermalUtils.ThermalState
import org.lineageos.xiaomiparts.utils.dlog
import com.android.settingslib.widget.MainSwitchPreference

class ThermalSettingsFragment : PreferenceFragment() {

    private lateinit var launcherApps: LauncherApps
    private lateinit var thermalUtils: ThermalUtils
    private lateinit var mainSwitch: MainSwitchPreference
    private lateinit var appsCategory: PreferenceCategory
    private val handlerThread = HandlerThread(TAG).apply { start() }
    private val bgHandler = Handler(handlerThread.looper)

    private val launcherAppsCallback =
        object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                if (user != Process.myUserHandle()) return
                val prefToRemove = appsCategory.findPreference<AppThermalPreference>(packageName)
                if (prefToRemove != null) {
                    activity?.runOnUiThread {
                        dlog(TAG, "onPackageRemoved: $packageName")
                        appsCategory.removePreference(prefToRemove)
                    }
                }
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                if (user != Process.myUserHandle()) return
                val info = launcherApps.getActivityList(packageName, user).firstOrNull() ?: return
                val entry = info.toAppEntry()
                if (appsCategory.findPreference<AppThermalPreference>(packageName) != null) return
                
                activity?.runOnUiThread {
                    var insertIndex = 0
                    for (i in 0 until appsCategory.preferenceCount) {
                        val pref = appsCategory.getPreference(i)
                        if (pref is AppThermalPreference) {
                            if ((pref.title?.toString() ?: "").compareTo(entry.label, ignoreCase = true) > 0) {
                                break
                            }
                        }
                        insertIndex = i + 1
                    }
                    
                    val pref = AppThermalPreference(preferenceScreen.context).apply {
                        key = entry.packageName
                        title = entry.label
                        appIcon = entry.icon
                        state = entry.state
                        isPersistent = false
                        onStateChanged = { newState ->
                            thermalUtils.writePackage(entry.packageName, newState)
                        }
                    }
                    dlog(TAG, "onPackageAdded: $packageName")
                    appsCategory.addPreference(pref)
                    appsCategory.setOrder(insertIndex)
                }
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {}

            override fun onPackagesAvailable(
                packageNames: Array<String>,
                user: UserHandle,
                replacing: Boolean
            ) {}

            override fun onPackagesUnavailable(
                packageNames: Array<String>,
                user: UserHandle,
                replacing: Boolean
            ) {}
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.thermal_settings)

        thermalUtils = ThermalUtils.getInstance(activity)
        appsCategory = findPreference<PreferenceCategory>("thermal_apps_category")!!
        mainSwitch =
            findPreference<MainSwitchPreference>(THERMAL_ENABLE_KEY)!!.apply {
                isChecked = thermalUtils.enabled
                addOnSwitchChangeListener { _, isChecked ->
                    thermalUtils.enabled = isChecked
                    updateAppListVisibility(isChecked)
                    if (isChecked && appsCategory.preferenceCount == 0) {
                        loadApps()
                    }
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launcherApps = activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    }

    override fun onStart() {
        super.onStart()
        updateAppListVisibility(thermalUtils.enabled)
        if (thermalUtils.enabled && appsCategory.preferenceCount == 0) {
            loadApps()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dlog(TAG, "onDestroy")
        handlerThread.quitSafely()
        launcherApps.unregisterCallback(launcherAppsCallback)
    }

    private fun updateAppListVisibility(visible: Boolean) {
        activity?.runOnUiThread {
            appsCategory.isVisible = visible
        }
    }

    private fun loadApps() {
        bgHandler.post {
            val appEntries =
                launcherApps
                    .getActivityList(null, Process.myUserHandle())
                    .distinctBy { it.componentName.packageName }
                    .map { it.toAppEntry() }
                    .sortedBy { it.label.toString().lowercase() }
            dlog(TAG, "loaded ${appEntries.size} apps")
            
            activity?.runOnUiThread {
                appsCategory.removeAll()
                appEntries.forEach { entry ->
                    val pref = AppThermalPreference(preferenceScreen.context).apply {
                        key = entry.packageName
                        title = entry.label
                        appIcon = entry.icon
                        state = entry.state
                        isPersistent = false
                        onStateChanged = { newState ->
                            thermalUtils.writePackage(entry.packageName, newState)
                        }
                    }
                    appsCategory.addPreference(pref)
                }
            }
            
            launcherApps.registerCallback(launcherAppsCallback, bgHandler)
        }
    }

    private fun LauncherActivityInfo.toAppEntry() =
        AppEntry(
            packageName = componentName.packageName,
            label = label.toString(),
            icon = getIcon(0),
            state = thermalUtils.getStateForPackage(componentName.packageName)
        )

    private data class AppEntry(
        val packageName: String,
        val label: String,
        val icon: Drawable,
        val state: ThermalState,
    )

    companion object {
        private const val TAG = "ThermalSettingsFragment"
        private const val THERMAL_ENABLE_KEY = "thermal_enable"
    }
}
