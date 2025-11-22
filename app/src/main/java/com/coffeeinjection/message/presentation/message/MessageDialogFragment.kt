package com.coffeeinjection.message.presentation.message

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.DialogFragmentMessageBinding

class MessageDialogFragment : DialogFragment() {

    companion object {
        private const val MAX_LENGTH = 1000

        fun newInstance(): MessageDialogFragment = MessageDialogFragment()
    }

    private var _binding: DialogFragmentMessageBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 다이얼로그 스타일 설정 (배경 투명 + 딤)
        setStyle(STYLE_NORMAL, R.style.MessageDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() = with(binding) {
        // 최대 1000자 제한
        etMessage.filters = arrayOf(InputFilter.LengthFilter(MAX_LENGTH))
        tvCharCount.text = "0 / $MAX_LENGTH"

        // 글자 수 카운트
        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val length = s?.length ?: 0
                binding.tvCharCount.text = "$length / $MAX_LENGTH"
            }
        })

        // 닫기 버튼
        ivClose.setOnClickListener {
            dismiss()
        }

        // 전송 버튼 (TODO: 콜백 연결해서 Activity/Fragment에 전달)
        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            // TODO: message 검증 및 콜백 처리
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.let { window ->
            val metrics = resources.displayMetrics
            val width = (metrics.widthPixels * 0.9f).toInt()   // 화면의 90% 폭만 사용

            window.setLayout(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            window.attributes = window.attributes.apply {
                gravity = Gravity.CENTER
                dimAmount = 0.6f
            }

            // 키보드 올라올 때 다이얼로그가 위로 밀리도록 (잘려 보이는 것 방지)
            window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}
