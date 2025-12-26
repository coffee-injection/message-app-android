package com.coffeeinjection.message.presentation.message

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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.DialogFragmentMessageBinding
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessageDialogFragment : DialogFragment() {

    companion object {
        private const val MAX_LENGTH = 500
    }

    private var _binding: DialogFragmentMessageBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val args: MessageDialogFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        // 이 프래그먼트는 "쓰기 전용" (읽기모드는 별도 프래그먼트에서 처리)
        if (args.readOnly) {
            dismiss()
            return
        }

        setupWriteMode()

        binding.ivClose.setOnClickListener { dismiss() }
    }

    private fun setupWriteMode() = with(binding) {
        // 입력 가능
        etMessage.visibility = View.VISIBLE
        tvCharCount.visibility = View.VISIBLE
        btnSend.visibility = View.VISIBLE

        // 최대 글자수 제한
        etMessage.filters = arrayOf(InputFilter.LengthFilter(MAX_LENGTH))
        tvCharCount.text = "0/$MAX_LENGTH"

        // 글자수 카운트
        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val length = s?.length ?: 0
                tvCharCount.text = "$length/$MAX_LENGTH"
            }
        })

        btnSend.setOnClickListener {
            val message = etMessage.text?.toString()?.trim().orEmpty()
            if (message.isBlank()) {
                Toast.makeText(requireContext(), "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val receiverNickname = args.receiverNickname
            val vm = sharedViewModel

            AlertDialog.Builder(requireContext())
                .setTitle("메세지 보틀 띄우기")
                .setMessage("메세지를 바다에 띄우시겠습니까?")
                .setPositiveButton("전송") { _, _ ->
                    vm.sendLetter(
                        receiverNickname = receiverNickname,
                        content = message
                    )
                    dismiss()
                }
                .setNegativeButton("취소", null)
                .show()
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
