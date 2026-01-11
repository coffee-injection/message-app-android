package com.coffeeinjection.message.util
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.ViewTitleBarBinding

class TitleBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding =
        ViewTitleBarBinding.inflate(LayoutInflater.from(context), this, true)

    var title: CharSequence
        get() = binding.tvTitle.text
        set(value) {
            binding.tvTitle.text = value
        }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        applyAttributes(attrs, defStyleAttr)
    }

    private fun applyAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) return

        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.TitleBarView,
            defStyleAttr,
            0
        )

        // XML에서 지정한 타이틀
        val titleText = typedArray.getString(R.styleable.TitleBarView_titleText)
        if (!titleText.isNullOrEmpty()) {
            title = titleText
        }

        // 뒤로가기 버튼 표시 여부
        val showBack = typedArray.getBoolean(R.styleable.TitleBarView_showBack, true)
        binding.btnBack.visibility = if (showBack) View.VISIBLE else View.GONE

        typedArray.recycle()
    }

    fun setOnBackClickListener(listener: (View) -> Unit) {
        binding.btnBack.setOnClickListener(listener)
    }

    fun setOnCloseClickListener(listener: (View) -> Unit){
        binding.btnClose.setOnClickListener(listener)
    }

    fun showBack(show: Boolean) {
        binding.btnBack.visibility = if (show) VISIBLE else GONE
        binding.marginView.visibility = if (show) GONE else VISIBLE
    }

    fun showClose(show: Boolean) {
        binding.btnClose.visibility = if (show) VISIBLE else GONE
    }
}
