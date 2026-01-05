package com.coffeeinjection.message.presentation.message

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.DialogFragmentWarningBinding

class WarningDialogFragment : DialogFragment() {

    private var _binding: DialogFragmentWarningBinding? = null
    private val binding get() = _binding!!

    var onConfirmClose: (() -> Unit)? = null

    companion object {
        private const val KEY_TITLE = "key_title"
        private const val KEY_SUB_TITLE = "key_sub_title"
        private const val KEY_CANCEL_TEXT = "key_cancel_text"
        private const val KEY_CLOSE_TEXT = "key_close_text"

        fun newInstance(
            title: String,
            subTitle: String,
            cancelText: String = "취소",
            closeText: String = "닫기"
        ): WarningDialogFragment {
            return WarningDialogFragment().apply {
                arguments = bundleOf(
                    KEY_TITLE to title,
                    KEY_SUB_TITLE to subTitle,
                    KEY_CANCEL_TEXT to cancelText,
                    KEY_CLOSE_TEXT to closeText
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MessageDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentWarningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = requireArguments().getString(KEY_TITLE).orEmpty()
        val subTitle = requireArguments().getString(KEY_SUB_TITLE).orEmpty()
        val cancelText = requireArguments().getString(KEY_CANCEL_TEXT).orEmpty()
        val closeText = requireArguments().getString(KEY_CLOSE_TEXT).orEmpty()

        binding.tvTitle.text = title
        binding.tvSubTitle.text = subTitle
        binding.btnCancel.text = cancelText
        binding.btnClose.text = closeText

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnClose.setOnClickListener {
            dismiss()
            onConfirmClose?.invoke()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val metrics = resources.displayMetrics
            val width = (metrics.widthPixels * 0.9f).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.attributes = window.attributes.apply {
                gravity = Gravity.CENTER
                dimAmount = 0.6f
            }
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
