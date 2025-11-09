/*
 * Copyright (C) 2025 The LineageOS Project
 * Licensed under the Apache License, Version 2.0
 */

package org.lineageos.xiaomiparts.revanced

import android.os.SystemProperties
import org.lineageos.xiaomiparts.utils.Logging

object ReVancedManager {
    private const val TAG = "ReVancedManager"
    private const val PROPERTY_REVANCED_ENABLED = "persist.sys.revan.mod"
    private const val DEFAULT_ENABLED = true

    fun isEnabled(): Boolean =
        SystemProperties.getBoolean(PROPERTY_REVANCED_ENABLED, DEFAULT_ENABLED)

    fun setEnabled(enabled: Boolean): Boolean = try {
        SystemProperties.set(PROPERTY_REVANCED_ENABLED, if (enabled) "true" else "false")
        Logging.log(TAG, "ReVanced ${if (enabled) "enabled" else "disabled"}")
        true
    } catch (e: Exception) {
        Logging.log(TAG, "Failed to set ReVanced state: ${e.message}")
        false
    }

    fun getPropertyValue(): String =
        SystemProperties.get(PROPERTY_REVANCED_ENABLED, DEFAULT_ENABLED.toString())
}
