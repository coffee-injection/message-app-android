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
    private val defaultHeightPx = dp(40f)

    // 아이콘 관련
    private var iconStart: Drawable? = null
    private var iconTint: ColorStateList? = null
    private var iconSizePx: Int = 0
    private var iconPaddingPx: Int = dp(8f)

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

        // 텍스트 정렬
        if (!hasAndroidAttribute(attrs, android.R.attr.gravity)) {
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }

        // 1) 아이콘 커스텀 속성 우선
        context.obtainStyledAttributes(attrs, R.styleable.GradientTextButton, defStyleAttr, 0).apply {
            iconStart = getDrawable(R.styleable.GradientTextButton_ciIcon) //
            iconTint = getColorStateList(R.styleable.GradientTextButton_ciIconTint) // 아이콘 색
            iconSizePx = getDimensionPixelSize(R.styleable.GradientTextButton_ciIconSize, 0) // 아이콘 크기
            iconPaddingPx = getDimensionPixelSize( // 텍스트와 아이콘 간격
                R.styleable.GradientTextButton_ciIconPadding, iconPaddingPx
            )
            recycle()
        }

        // 2) 커스텀 아이콘이 없으면 프레임워크 속성으로 폴백
        if (iconStart == null && attrs != null) {
            // drawableStart(0) -> drawableLeft(1) -> src(2) 순으로 시도
            val a = context.obtainStyledAttributes(
                attrs, intArrayOf(
                    android.R.attr.drawableStart,
                    android.R.attr.drawableLeft,
                    android.R.attr.src // Button에선 보통 값이 없음
                )
            )
            val d = try {
                a.getDrawable(0) ?: a.getDrawable(1) ?: a.getDrawable(2)
            } finally {
                a.recycle()
            }
            if (d != null) iconStart = d
        }

        compoundDrawablePadding = iconPaddingPx
        applyIcon()
    }

    // 아이콘 추가
    private fun applyIcon() {
        val isIcon = iconStart?.mutate()
        if (isIcon != null) {
            // 틴트 적용
            iconTint?.let { DrawableCompat.setTintList(isIcon, it) }

            // 크기 지정(있으면 bounds로, 없으면 intrinsic 사용)
            if (iconSizePx > 0) {
                isIcon.setBounds(0, 0, iconSizePx, iconSizePx)
                // bounds를 수동 지정한 경우 setCompoundDrawables(Relative) 사용
                setCompoundDrawablesRelative(isIcon, null, null, null)
            } else {
                // intrinsic 크기 사용
                setCompoundDrawablesRelativeWithIntrinsicBounds(isIcon, null, null, null)
            }
        } else {
            // 아이콘 제거
            setCompoundDrawablesRelative(null, null, null, null)
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
