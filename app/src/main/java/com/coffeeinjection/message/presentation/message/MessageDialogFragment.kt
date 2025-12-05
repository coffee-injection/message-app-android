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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.DialogFragmentMessageBinding
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessageDialogFragment : DialogFragment() {

    companion object {
        private const val MAX_LENGTH = 1000

        fun newInstance(): MessageDialogFragment = MessageDialogFragment()
    }

    private var _binding: DialogFragmentMessageBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()


    // HomeFragment 에서 넘긴 값 받기
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

        if (args.readOnly) {
            setupReadMode()
        } else {
            setupWriteMode()
        }

        // 닫기 버튼 공통
        binding.ivClose.setOnClickListener { dismiss() }
    }

    /**
     * ✉️ 읽기 모드 (홈에서 병 아이콘 눌렀을 때)
     * - tv_content : VISIBLE + 내용 표시
     * - etMessage, tvCharCount, btnSend : GONE
     * - tv_sender_name : senderName 표시
     */
    private fun setupReadMode() = with(binding) {
        // 내용 표시
        tvContent.visibility = View.VISIBLE
        tvContent.text = args.content

        // 발신자 이름 표시
        tvSenderName.text = args.senderName

        // 입력 관련 뷰 숨기기
        etMessage.visibility = View.GONE
        tvCharCount.visibility = View.GONE
        btnSend.visibility = View.GONE
    }

    /**
     * 📝 쓰기 모드 (새 메시지 작성할 때)
     * - 기존 로직 그대로 유지
     */
    private fun setupWriteMode() = with(binding) {
        // 내용 TextView 숨김
        tvContent.visibility = View.GONE

        // 입력 필드 보이기
        etMessage.visibility = View.VISIBLE
        tvCharCount.visibility = View.VISIBLE
        btnSend.visibility = View.VISIBLE

        // 최대 1000자 제한
        etMessage.filters = arrayOf(InputFilter.LengthFilter(MAX_LENGTH))
        tvCharCount.text = "0 / $MAX_LENGTH"

        // 글자 수 카운트
        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val length = s?.length ?: 0
                tvCharCount.text = "$length / $MAX_LENGTH"
            }
        })

        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isBlank()) {
                Toast.makeText(requireContext(), "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 여기서 ViewModel, args 를 먼저 꺼내서 캡처해 둠
            val vm = sharedViewModel
            val receiverNickname = args.receiverNickname

            AlertDialog.Builder(requireContext())
                .setTitle("편지 전송")
                .setMessage("정말 보내시겠습니까?")
                .setPositiveButton("전송") { _, _ ->
                    // 여기서는 이미 생성된 vm, receiverNickname, message 만 사용
                    vm.sendLetter(
                        receiverNickname = receiverNickname,
                        content = message
                    )
                    // 상태 저장 타이밍에 따라 안전하게 가려면 아래처럼 써도 됨
                    // dismissAllowingStateLoss()
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

            window.setLayout(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            window.attributes = window.attributes.apply {
                gravity = Gravity.CENTER
                dimAmount = 0.6f
            }

            window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
