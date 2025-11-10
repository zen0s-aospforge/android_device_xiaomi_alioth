/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.xiaomiparts.utils

import android.content.Context
import android.content.Intent
import android.os.UserHandle
import java.io.File
import org.lineageos.xiaomiparts.hbm.AutoHBMService
import org.lineageos.xiaomiparts.hbm.HBMFragment

private const val TAG = "FUtils"

fun readOneLine(fileName: String): String? =
    runCatching { File(fileName).useLines { it.firstOrNull() } }
        .onFailure { e -> Logging.e(TAG, "Could not read from file $fileName", e) }
        .getOrNull()

fun writeLine(fileName: String, value: String): Boolean =
    runCatching { File(fileName).writeText(value) }
        .onFailure { e -> Logging.e(TAG, "Could not write to file $fileName", e) }
        .isSuccess

fun fileExists(fileName: String): Boolean = File(fileName).exists()

fun isFileReadable(fileName:String): Boolean {
    val file = File(fileName)
    return file.exists() && file.canRead()
}

fun isFileWritable(fileName: String): Boolean {
    val file = File(fileName)
    return file.exists() && file.canWrite()
}

fun delete(fileName: String): Boolean =
    runCatching { File(fileName).delete() }
        .onFailure { e -> Logging.w(TAG, "Failed to delete $fileName", e) }
        .getOrDefault(false)

fun rename(srcPath: String, dstPath: String): Boolean =
    runCatching { File(srcPath).renameTo(File(dstPath)) }
        .onFailure { e -> Logging.w(TAG, "Failed to rename $srcPath to $dstPath", e) }
        .getOrDefault(false)

fun getFileValueAsBoolean(filename: String, defValue: Boolean): Boolean {
    return when (readOneLine(filename)) {
        "0" -> false
        null -> defValue
        else -> true
    }
}

fun getFileValue(filename: String, defValue: String): String {
    return readOneLine(filename) ?: defValue
}

fun enableService(context: Context) {
    val autoHBMEnabled = HBMFragment.isAutoHBMEnabled(context)
    if (autoHBMEnabled) {
        context.startServiceAsUser(
            Intent(context, AutoHBMService::class.java),
            UserHandle.CURRENT
        )
    } else {
        context.stopServiceAsUser(
            Intent(context, AutoHBMService::class.java),
            UserHandle.CURRENT
        )
    }
}