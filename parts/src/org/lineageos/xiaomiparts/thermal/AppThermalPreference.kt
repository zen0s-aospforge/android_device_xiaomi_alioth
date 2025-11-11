/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.xiaomiparts.thermal

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
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

    var icon: Drawable? = null
    var state: ThermalState = ThermalState.DEFAULT
    var onStateChanged: ((Int) -> Unit)? = null

    private val modeAdapter = ModeAdapter(context)

    init {
        layoutResource = R.layout.thermal_app_preference
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val appIcon = holder.findViewById(R.id.app_icon) as ImageView
        val appMode = holder.findViewById(R.id.app_mode) as Spinner

        appIcon.setImageDrawable(icon)

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

    private inner class ModeAdapter(context: Context) : BaseAdapter() {
        private val inflater = LayoutInflater.from(context)
        private val items = ThermalState.values().map { state ->
            context.getString(state.label)
        }

        override fun getCount() = items.size

        override fun getItem(position: Int) = items[position]

        override fun getItemId(position: Int) = 0L

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            (convertView as? TextView
                    ?: inflater.inflate(
                        android.R.layout.simple_spinner_dropdown_item,
                        parent,
                        false
                    ) as TextView)
                .apply {
                    setText(items[position])
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
    }
}
