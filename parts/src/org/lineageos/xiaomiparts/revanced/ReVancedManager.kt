/*
 * Copyright (C) 2025 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.xiaomiparts.revanced

import android.os.SystemProperties
import org.lineageos.xiaomiparts.utils.Logging

object ReVancedManager {
    private const val TAG = "ReVancedManager"
    private const val PROPERTY_REVANCED_ENABLED = "persist.sys.revan.mod"
    private const val DEFAULT_ENABLED = true

    fun isEnabled(): Boolean {
        return SystemProperties.getBoolean(PROPERTY_REVANCED_ENABLED, DEFAULT_ENABLED)
    }

    fun setEnabled(enabled: Boolean): Boolean {
        return try {
            SystemProperties.set(PROPERTY_REVANCED_ENABLED, if (enabled) "true" else "false")
            Logging.log(TAG, "ReVanced integration ${if (enabled) "enabled" else "disabled"}")
            true
        } catch (e: Exception) {
            Logging.log(TAG, "Failed to set ReVanced state: ${e.message}")
            false
        }
    }

    fun getPropertyValue(): String {
        return SystemProperties.get(PROPERTY_REVANCED_ENABLED, DEFAULT_ENABLED.toString())
    }
}
