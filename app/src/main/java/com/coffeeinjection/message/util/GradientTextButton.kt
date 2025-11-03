package com.coffeeinjection.message.util

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import com.coffeeinjection.message.R
import android.graphics.Color
import androidx.appcompat.widget.AppCompatButton
import kotlin.math.max
import android.content.res.TypedArray

class GradientTextButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    private val defaultWidthPx = dp(320f)
    private val defaultHeightPx = dp(40f)

    init {
        isAllCaps = false
        gravity = Gravity.CENTER

        // XML에 android:textColor가 없으면 기본 흰색
        if (!hasAndroidAttribute(attrs, android.R.attr.textColor)) {
            setTextColor(Color.WHITE)
        }

        // XML에 android:background가 없으면 기본 배경 적용
        if (!hasAndroidAttribute(attrs, android.R.attr.background)) {
            setBackgroundResource(R.drawable.btn_gradient_navy)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)

        var w = measuredWidth
        var h = measuredHeight

        // EXTACTLY(정확히)면 XML 지정값을 존중, 그 외(WRAP_CONTENT/UNSPECIFIED)면 기본 크기 보장
        if (wMode != MeasureSpec.EXACTLY) w = max(w, defaultWidthPx)
        if (hMode != MeasureSpec.EXACTLY) h = max(h, defaultHeightPx)

        setMeasuredDimension(w, h)
    }

    private fun dp(value: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
            .toInt()

    private fun hasAndroidAttribute(attrs: AttributeSet?, attrId: Int): Boolean {
        if (attrs == null) return false
        val a: TypedArray = context.obtainStyledAttributes(attrs, intArrayOf(attrId))
        return try {
            a.hasValue(0)
        } finally {
            a.recycle()
        }
    }
}
