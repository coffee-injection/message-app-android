package com.coffeeinjection.message.util

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import com.coffeeinjection.message.R
import android.graphics.Color
import androidx.appcompat.widget.AppCompatButton
import kotlin.math.max
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat

class GradientTextButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    private val defaultWidthPx = dp(320f)
    private val defaultHeightPx = dp(55f)

    // 아이콘 관련
    private var iconStart: Drawable? = null
    private var iconTint: ColorStateList? = null
    private var iconSizePx: Int = 0
    private var iconPaddingPx: Int = dp(8f)

    private var basePaddingStartPx: Int = 0
    private var basePaddingEndPx: Int = 0
    private var centerIconWithText: Boolean = false

    init {
        isAllCaps = false
        gravity = Gravity.CENTER

        // XML에 android:textColor가 없으면 기본 흰색
        if (!hasAndroidAttribute(attrs, android.R.attr.textColor)) {
            setTextColor(Color.WHITE)
        }

        // XML에 android:background가 없으면 기본 배경 적용
        if (!hasAndroidAttribute(attrs, android.R.attr.background)) {
            setBackgroundResource(R.drawable.btn_gradient_blue)
        }

        // 텍스트 정렬
        if (!hasAndroidAttribute(attrs, android.R.attr.gravity)) {
            gravity = Gravity.CENTER
        }

        compoundDrawablePadding = iconPaddingPx
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

    override fun onFinishInflate() {
        super.onFinishInflate()
        basePaddingStartPx = paddingStart
        basePaddingEndPx = paddingEnd
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (centerIconWithText) updateCompoundCentering()
    }

    private fun updateCompoundCentering() {
        val d = compoundDrawablesRelative[0] ?: run {
            // 아이콘 없으면 원래 패딩 복구
            if (paddingStart != basePaddingStartPx || paddingEnd != basePaddingEndPx) {
                setPaddingRelative(basePaddingStartPx, paddingTop, basePaddingEndPx, paddingBottom)
            }
            return
        }

        val iconW = if (d.bounds.width() > 0) d.bounds.width() else d.intrinsicWidth
        val textW = paint.measureText(text?.toString().orEmpty()).toInt()
        val bodyW = iconW + compoundDrawablePadding + textW

        val available = width - basePaddingStartPx - basePaddingEndPx
        val extra = ((available - bodyW) / 2).coerceAtLeast(0)

        val newStart = basePaddingStartPx + extra
        val newEnd = basePaddingEndPx + extra

        if (paddingStart != newStart || paddingEnd != newEnd) {
            setPaddingRelative(newStart, paddingTop, newEnd, paddingBottom)
        }
    }

    fun setCenterIconWithText(enable: Boolean) {
        centerIconWithText = enable
        requestLayout()
    }
}
