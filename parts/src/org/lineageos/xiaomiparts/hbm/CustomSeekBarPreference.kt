/*
 * Copyright (C) 2016-2021 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lineageos.xiaomiparts.hbm

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import org.lineageos.xiaomiparts.utils.Logging
import androidx.preference.PreferenceViewHolder
import org.lineageos.xiaomiparts.R
import kotlin.math.abs

open class CustomSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = TypedArrayUtils.getAttr(
        context,
        androidx.preference.R.attr.preferenceStyle,
        android.R.attr.preferenceStyle
    ),
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes), SeekBar.OnSeekBarChangeListener {

    protected open val TAG: String = javaClass.name

    protected var mInterval = 1
    protected var mShowSign = false
    protected var mUnits = ""
    protected var mContinuousUpdates = false

    protected var mMinValue = 0
    protected var mMaxValue = 100
    protected var mDefaultValueExists = false
    protected var mDefaultValue = 0
    protected var mDefaultValueTextExists = false
    protected var mDefaultValueText: String? = null

    protected var mValue: Int

    protected var mValueTextView: TextView? = null
    protected var mResetImageView: ImageView? = null
    protected var mMinusImageView: ImageView? = null
    protected var mPlusImageView: ImageView? = null
    protected var mSeekBar: SeekBar

    protected var mTrackingTouch = false
    protected var mTrackingValue = 0

    private var mTargetPrefKey: String? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBarPreference).use { a ->
            mShowSign = a.getBoolean(R.styleable.CustomSeekBarPreference_showSign, mShowSign)
            a.getString(R.styleable.CustomSeekBarPreference_units)?.let {
                mUnits = " $it"
            }
            mContinuousUpdates =
                a.getBoolean(R.styleable.CustomSeekBarPreference_continuousUpdates, mContinuousUpdates)

            val defaultValueText = a.getString(R.styleable.CustomSeekBarPreference_defaultValueText)
            mDefaultValueTextExists = !defaultValueText.isNullOrEmpty()
            if (mDefaultValueTextExists) {
                mDefaultValueText = defaultValueText
            }

            a.getString(R.styleable.CustomSeekBarPreference_targetPrefKey)?.let {
                mTargetPrefKey = it
                isPersistent = false
            }

            mInterval = a.getInt(R.styleable.CustomSeekBarPreference_interval, 1)
            mMinValue = a.getInt(R.styleable.CustomSeekBarPreference_min, 0)
        }

        mMaxValue = attrs?.getAttributeIntValue(ANDROIDNS, "max", mMaxValue) ?: mMaxValue
        if (mMaxValue < mMinValue) {
            mMaxValue = mMinValue
        }

        val defaultValue = attrs?.getAttributeValue(ANDROIDNS, "defaultValue")
        mDefaultValueExists = !defaultValue.isNullOrEmpty()
        mDefaultValue = if (mDefaultValueExists) {
             getLimitedValue(defaultValue!!.toInt())
        } else {
            mMinValue
        }

        mValue = if (mTargetPrefKey != null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.getString(mTargetPrefKey, mDefaultValue.toString())?.toIntOrNull() ?: mDefaultValue
        } else {
            mDefaultValue
        }

        mSeekBar = SeekBar(context, attrs)
        layoutResource = R.layout.preference_custom_seekbar
    }

    override fun onDependencyChanged(dependency: Preference, disableDependent: Boolean) {
        super.onDependencyChanged(dependency, disableDependent)
        this.shouldDisableView = true
        mSeekBar.isEnabled = !disableDependent
        mResetImageView?.isEnabled = !disableDependent
        mPlusImageView?.isEnabled = !disableDependent
        mMinusImageView?.isEnabled = !disableDependent
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        try {
            val oldContainer = mSeekBar.parent
            val newContainer = holder.findViewById(R.id.seekbar) as ViewGroup
            if (oldContainer !== newContainer) {
                if (oldContainer != null) {
                    (oldContainer as ViewGroup).removeView(mSeekBar)
                }
                newContainer.removeAllViews()
                newContainer.addView(
                    mSeekBar, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        } catch (ex: Exception) {
            Logging.e("Seek", "Error binding view: $ex")
        }

        mSeekBar.max = getSeekValue(mMaxValue)
        mSeekBar.progress = getSeekValue(mValue)
        mSeekBar.isEnabled = isEnabled

        mValueTextView = holder.findViewById(R.id.value) as TextView
        mResetImageView = holder.findViewById(R.id.reset) as ImageView
        mMinusImageView = holder.findViewById(R.id.minus) as ImageView
        mPlusImageView = holder.findViewById(R.id.plus) as ImageView

        updateValueViews()

        mSeekBar.setOnSeekBarChangeListener(this)

        mResetImageView?.setOnClickListener {
            Toast.makeText(
                context,
                context.getString(R.string.custom_seekbar_default_value_to_set, getTextValue(mDefaultValue)),
                Toast.LENGTH_LONG
            ).show()
        }
        mResetImageView?.setOnLongClickListener {
            setValue(mDefaultValue, true)
            true
        }

        mMinusImageView?.setOnClickListener {
            setValue(mValue - mInterval, true)
        }
        mMinusImageView?.setOnLongClickListener {
            val newValue = if (mMaxValue - mMinValue > mInterval * 2 && mMaxValue + mMinValue < mValue * 2) {
                (mMaxValue + mMinValue) / 2
            } else {
                mMinValue
            }
            setValue(newValue, true)
            true
        }

        mPlusImageView?.setOnClickListener {
            setValue(mValue + mInterval, true)
        }
        mPlusImageView?.setOnLongClickListener {
            val newValue = if (mMaxValue - mMinValue > mInterval * 2 && mMaxValue + mMinValue > mValue * 2) {
                (mMaxValue + mMinValue + 1) / 2
            } else {
                mMaxValue
            }
            setValue(newValue, true)
            true
        }
    }

    protected open fun getLimitedValue(v: Int): Int {
        return v.coerceIn(mMinValue, mMaxValue)
    }

    protected open fun getSeekValue(v: Int): Int {
        return 0 - ((mMinValue - v) / mInterval)
    }

    protected open fun getTextValue(v: Int): String {
        if (mDefaultValueTextExists && mDefaultValueExists && v == mDefaultValue) {
            return mDefaultValueText!!
        }
        return (if (mShowSign && v > 0) "+" else "") + v + mUnits
    }

    protected open fun updateValueViews() {
        mValueTextView?.let { textView ->
            if (!mTrackingTouch || mContinuousUpdates) {
                if (mDefaultValueTextExists && mDefaultValueExists && mValue == mDefaultValue) {
                    textView.text = "$mDefaultValueText (${context.getString(R.string.custom_seekbar_default_value)})"
                } else {
                    val defaultStr = if (mDefaultValueExists && mValue == mDefaultValue) {
                        " (${context.getString(R.string.custom_seekbar_default_value)})"
                    } else {
                        ""
                    }
                    textView.text = context.getString(R.string.custom_seekbar_value, getTextValue(mValue)) + defaultStr
                }
            } else {
                if (mDefaultValueTextExists && mDefaultValueExists && mTrackingValue == mDefaultValue) {
                    textView.text = "[$mDefaultValueText]"
                } else {
                    textView.text = context.getString(R.string.custom_seekbar_value, "[${getTextValue(mTrackingValue)}]")
                }
            }
        }

        mResetImageView?.let {
            it.visibility = if (!mDefaultValueExists || mValue == mDefaultValue || mTrackingTouch) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
        }

        mMinusImageView?.let {
            if (mValue == mMinValue || mTrackingTouch) {
                it.isClickable = false
                it.setColorFilter(context.getColor(R.color.disabled_text_color), PorterDuff.Mode.MULTIPLY)
            } else {
                it.isClickable = true
                it.clearColorFilter()
            }
        }

        mPlusImageView?.let {
            if (mValue == mMaxValue || mTrackingTouch) {
                it.isClickable = false
                it.setColorFilter(context.getColor(R.color.disabled_text_color), PorterDuff.Mode.MULTIPLY)
            } else {
                it.isClickable = true
                it.clearColorFilter()
            }
        }
    }

    protected open fun saveValue(newValue: Int) {
        if (mTargetPrefKey != null) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(mTargetPrefKey, newValue.toString())
                .apply()
        } else {
            changeValue(newValue)
            persistInt(newValue)
        }
    }

    protected open fun changeValue(newValue: Int) {
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val newValue = getLimitedValue(mMinValue + (progress * mInterval))
        if (mTrackingTouch && !mContinuousUpdates) {
            mTrackingValue = newValue
            updateValueViews()
        } else if (mValue != newValue) {
            if (!callChangeListener(newValue)) {
                mSeekBar.progress = getSeekValue(mValue)
                return
            }

            saveValue(newValue)

            mValue = newValue
            updateValueViews()
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        mTrackingValue = mValue
        mTrackingTouch = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        mTrackingTouch = false
        if (!mContinuousUpdates) {
            onProgressChanged(mSeekBar, getSeekValue(mTrackingValue), false)
        }
        notifyChanged()
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        if (mTargetPrefKey == null && restoreValue) {
             mValue = getPersistedInt(mValue)
        }
    }

    override fun setDefaultValue(defaultValue: Any?) {
        if (defaultValue is Int) {
            setDefaultValue(defaultValue, mSeekBar != null)
        } else {
            setDefaultValue(defaultValue?.toString(), mSeekBar != null)
        }
    }

    fun setDefaultValue(newValue: Int, update: Boolean) {
        val limitedValue = getLimitedValue(newValue)
        if (!mDefaultValueExists || mDefaultValue != limitedValue) {
            mDefaultValueExists = true
            mDefaultValue = limitedValue
            if (update) {
                updateValueViews()
            }
        }
    }

    fun setDefaultValue(newValue: String?, update: Boolean) {
        if (mDefaultValueExists && newValue.isNullOrEmpty()) {
            mDefaultValueExists = false
            if (update) {
                updateValueViews()
            }
        } else if (!newValue.isNullOrEmpty()) {
            setDefaultValue(newValue.toInt(), update)
        }
    }

    fun setValue(newValue: Int) {
        mValue = getLimitedValue(newValue)
        mSeekBar.progress = getSeekValue(mValue)
    }

    fun setValue(newValue: Int, update: Boolean) {
        val limitedValue = getLimitedValue(newValue)
        if (mValue != limitedValue) {
            if (update) {
                mSeekBar.progress = getSeekValue(limitedValue)
            } else {
                mValue = limitedValue
            }
        }
    }

    fun getValue(): Int {
        return mValue
    }

    fun refresh(newValue: Int) {
        setValue(newValue, true)
    }

    companion object {
        private const val SETTINGS_NS = "http://schemas.android.com/apk/res/com.android.settings"
        protected const val ANDROIDNS = "http://schemas.android.com/apk/res/android"
    }
}