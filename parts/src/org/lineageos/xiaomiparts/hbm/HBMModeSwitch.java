/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.lineageos.xiaomiparts.hbm;

import android.content.Context;
import android.provider.Settings;
import android.content.SharedPreferences;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceManager;
import android.util.Log;

import org.lineageos.xiaomiparts.utils.FileUtils;
import org.lineageos.xiaomiparts.display.*;

public class HBMModeSwitch implements OnPreferenceChangeListener {
    private static final String TAG = "HBMModeSwitch";
    private static final String HBM = "/sys/class/drm/card0/card0-DSI-1/disp_param";
    private static final String BACKLIGHT = "/sys/class/backlight/panel0-backlight/brightness";
    private Context mContext;

    public HBMModeSwitch(Context context) {
        mContext = context;
    }

    public static String getHBM() {
        if (FileUtils.isFileWritable(HBM)) {
            return HBM;
        }
        return null;
    }

    public static String getBACKLIGHT() {
        if (FileUtils.isFileWritable(BACKLIGHT)) {
            return BACKLIGHT;
        }
        return null;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Boolean enabled = (Boolean) newValue;
        boolean dcDimmingEnabled = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(DcDimmingTileService.DC_DIMMING_ENABLE_KEY, false);
        if (dcDimmingEnabled) {
            return false;
        }

        // Defensive: ensure HBM path is available before writing to avoid NPE/crash
        String hbmPath = getHBM();
        if (hbmPath == null) {
            Log.w(TAG, "HBM sysfs path not available/writable, rejecting change");
            return false;
        }

        try {
            FileUtils.writeLine(hbmPath, enabled ? "0x10000" : "0xF0000");
        } catch (Exception e) {
            Log.e(TAG, "Failed to write HBM value", e);
            return false;
        }

        if (enabled) {
            String backlightPath = getBACKLIGHT();
            if (backlightPath != null) {
                try {
                    FileUtils.writeLine(backlightPath, "2047");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to write BACKLIGHT value", e);
                    // continue: don't crash Settings; return false to keep toggle consistent
                    return false;
                }
            } else {
                Log.w(TAG, "BACKLIGHT sysfs path not available/writable");
                return false;
            }

            try {
                Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
            } catch (SecurityException se) {
                Log.e(TAG, "No permission to write system settings (SCREEN_BRIGHTNESS)", se);
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Failed to set SCREEN_BRIGHTNESS", e);
                return false;
            }
            String backlightPath = getBACKLIGHT();
            if (backlightPath != null) {
                FileUtils.writeLine(backlightPath, "2047");
            }
            Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
        }

        return true;
    }
}
