/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.xiaomiparts.thermal

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import org.lineageos.xiaomiparts.R
import org.lineageos.xiaomiparts.thermal.ThermalUtils.ThermalState

class AppThermalPreference(
    context: Context,
    attrs: AttributeSet? = null
) : Preference(context, attrs) {

    var appIcon: Drawable? = null
    var state: ThermalState = ThermalState.DEFAULT
    var onStateChanged: ((Int) -> Unit)? = null

    private val modeAdapter = ModeAdapter(context)

    init {
        layoutResource = R.layout.thermal_app_preference
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val icon = holder.findViewById(R.id.app_icon) as ImageView
        val appMode = holder.findViewById(R.id.app_mode) as Spinner

        icon.setImageDrawable(appIcon)

        appMode.apply {
            adapter = modeAdapter
            setSelection(state.id, false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    if (state.id != pos) {
                        onStateChanged?.invoke(pos)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        holder.itemView.setOnClickListener {
            appMode.performClick()
        }
    }

    private inner class ModeAdapter(context: Context) :
        ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_item,
            ThermalState.values().map { context.getString(it.label) }
        ) {

        init {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            (super.getView(position, convertView, parent) as TextView).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
            (super.getDropDownView(position, convertView, parent) as TextView).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }
    }
}
