/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.xiaomiparts.utils

import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.util.Log
import kotlin.jvm.JvmStatic
import java.io.File
import org.lineageos.xiaomiparts.hbm.AutoHBMService
import org.lineageos.xiaomiparts.hbm.HBMFragment

private const val TAG = "FileUtils"

/*
 * Reads the first line of text from the given file.
 *
 * @return the read line contents, or null on failure
 */
fun readOneLine(fileName: String): String? =
    runCatching { File(fileName).useLines { it.firstOrNull() } }
        .onFailure { e -> Log.e(TAG, "Could not read from file $fileName", e) }
        .getOrNull()

/*
 * Writes the given value into the given file
 *
 * @return true on success, false on failure
 */
fun writeLine(fileName: String, value: String): Boolean =
    runCatching { File(fileName).writeText(value) }
        .onFailure { e -> Log.e(TAG, "Could not write to file $fileName", e) }
        .isSuccess

/*
 * Checks whether the given file exists
 *
 * @return true if exists, false if not
 */
fun fileExists(fileName: String): Boolean = File(fileName).exists()

/*
 * Checks whether the given file is readable
 *
 * @return true if readable, false if not
 */
fun isFileReadable(fileName: String): Boolean {
    val file = File(fileName)
    return file.exists() && file.canRead()
}

/*
 * Checks whether the given file is writable
 *
 * @return true if writable, false if not
 */
fun isFileWritable(fileName: String): Boolean {
    val file = File(fileName)
    return file.exists() && file.canWrite()
}

/*
 * Deletes an existing file
 *
 * @return true if the delete was successful, false if not
 */
fun delete(fileName: String): Boolean =
    runCatching { File(fileName).delete() }
        .onFailure { e -> Log.w(TAG, "Failed to delete $fileName", e) }
        .getOrDefault(false)

/*
 * Renames an existing file
 *
 * @return true if the rename was successful, false if not
 */
fun rename(srcPath: String, dstPath: String): Boolean =
    runCatching { File(srcPath).renameTo(File(dstPath)) }
        .onFailure { e -> Log.w(TAG, "Failed to rename $srcPath to $dstPath", e) }
        .getOrDefault(false)

fun getFileValueAsBoolean(filename: String, defValue: Boolean): Boolean {
    val fileValue = readOneLine(filename)
    return if (fileValue != null) {
        fileValue != "0"
    } else {
        defValue
    }
}

fun getFileValue(filename: String, defValue: String): String {
    val fileValue = readOneLine(filename)
    return fileValue ?: defValue
}

private var mServiceEnabled = false

private fun startService(context: Context) {
    context.startServiceAsUser(Intent(context, AutoHBMService::class.java), UserHandle.CURRENT)
    mServiceEnabled = true
}

private fun stopService(context: Context) {
    mServiceEnabled = false
    context.stopServiceAsUser(Intent(context, AutoHBMService::class.java), UserHandle.CURRENT)
}

fun enableService(context: Context) {
    if (HBMFragment.isAUTOHBMEnabled(context) && !mServiceEnabled) {
        startService(context)
    } else if (!HBMFragment.isAUTOHBMEnabled(context) && mServiceEnabled) {
        stopService(context)
    }
}

/**
 * Backwards-compatible facade for Java/Kotlin call sites that expect a `FileUtils` class/object.
 * Some code imports `org.lineageos.xiaomiparts.utils.FileUtils` and calls methods on it. To avoid
 * changing all call sites, expose an object with the small set of functions used elsewhere.
 */
object FileUtils {
    @JvmStatic
    fun enableService(context: Context) = org.lineageos.xiaomiparts.utils.enableService(context)

    @JvmStatic
    fun readOneLine(fileName: String): String? = org.lineageos.xiaomiparts.utils.readOneLine(fileName)

    @JvmStatic
    fun writeLine(fileName: String, value: String): Boolean = org.lineageos.xiaomiparts.utils.writeLine(fileName, value)

    @JvmStatic
    fun fileExists(fileName: String): Boolean = org.lineageos.xiaomiparts.utils.fileExists(fileName)

    @JvmStatic
    fun isFileReadable(fileName: String): Boolean = org.lineageos.xiaomiparts.utils.isFileReadable(fileName)

    @JvmStatic
    fun isFileWritable(fileName: String): Boolean = org.lineageos.xiaomiparts.utils.isFileWritable(fileName)

    @JvmStatic
    fun delete(fileName: String): Boolean = org.lineageos.xiaomiparts.utils.delete(fileName)

    @JvmStatic
    fun rename(srcPath: String, dstPath: String): Boolean = org.lineageos.xiaomiparts.utils.rename(srcPath, dstPath)

    @JvmStatic
    fun getFileValueAsBoolean(filename: String, defValue: Boolean): Boolean =
        org.lineageos.xiaomiparts.utils.getFileValueAsBoolean(filename, defValue)

    @JvmStatic
    fun getFileValue(filename: String, defValue: String): String =
        org.lineageos.xiaomiparts.utils.getFileValue(filename, defValue)
}
