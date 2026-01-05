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
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.DialogFragmentMessageReadBinding
import com.coffeeinjection.message.databinding.DialogFragmentMessageWriteBinding
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import com.coffeeinjection.message.util.toKoreanDateHourFast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 다이얼로그 메세지 프레그먼트
 * 메세지 읽기모드 / 쓰기모드 둘다 지원
 */
@AndroidEntryPoint
class MessageDialogFragment : DialogFragment() {

    companion object {
        private const val MAX_LENGTH = 500
    }

    // 읽기/쓰기 모드를 한 Fragment에서 관리하되, layout(binding)은 분리해서 들고 갑니다.
    // (한 번에 하나만 inflate 되므로 메모리 낭비는 거의 없습니다.)
    private var _readBinding: DialogFragmentMessageReadBinding? = null
    private val readBinding get() = _readBinding!!

    private var _writeBinding: DialogFragmentMessageWriteBinding? = null
    private val writeBinding get() = _writeBinding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val args: MessageDialogFragmentArgs by navArgs()

    /** 현재 모드 */
    private val mode: Mode
        get() = if (args.readOnly) Mode.READ else Mode.WRITE

    private enum class Mode { READ, WRITE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dialog 스타일 적용
        setStyle(STYLE_NORMAL, R.style.MessageDialogTheme)
    }

    /**
     * 모드에 따라 다른 레이아웃을 inflate 합니다.
     * - READ  : dialog_fragment_message_read.xml
     * - WRITE : dialog_fragment_message_write.xml
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return when (mode) {
            Mode.READ -> {
                _readBinding = DialogFragmentMessageReadBinding.inflate(inflater, container, false)
                readBinding.root
            }

            Mode.WRITE -> {
                _writeBinding =
                    DialogFragmentMessageWriteBinding.inflate(inflater, container, false)
                writeBinding.root
            }
        }
    }

    /**
     * inflate 된 레이아웃에 맞춰 동작(클릭/바인딩)을 설정합니다.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (mode) {
            Mode.READ -> setupReadMode()
            Mode.WRITE -> setupWriteMode()
        }
    }

    // ---------------------------------------------------------------------------------------------
    // READ MODE (읽기 레이아웃: dialog_fragment_message_read.xml)
    // ---------------------------------------------------------------------------------------------
    private fun setupReadMode() = with(readBinding) {

        ivClose.setOnClickListener {
            showWarningDialog(
                title = getString(R.string.dialog_fragment_message_title2),
                subTitle = getString(R.string.dialog_fragment_message_sub2)
            ) {
                dismiss()
            }
        }

        // 1) 상세 조회 호출
        sharedViewModel.readLetter(args.letterId)

        // 2) 상세 데이터 구독 → UI 바인딩
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.letterDetail.collect { letter ->
                        if (letter == null) return@collect
                        tvNickname.text = letter.senderName
                        tvReceivedMsg.text = letter.content
                        // todo -> 섬 이름 추가되어야함
//                        tvIslandName.text =letter.senderName
                        tvReceivedDate.text =letter.matchedAt.toKoreanDateHourFast()
                    }
                }

                // (선택) 에러 토스트
                launch {
                    sharedViewModel.letterDetailError.collect { msg ->
                        if (!msg.isNullOrBlank()) {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // 3) 저장(북마크)
        btnSave.setCenterIconWithText(true)
        btnSave.setOnClickListener {
            sharedViewModel.bookmarkLetter(args.letterId)
            Toast.makeText(requireContext(), "저장했습니다.", Toast.LENGTH_SHORT).show()
        }

        // 4) 답장하기 → 쓰기 모드로 다시 열기
        btnReply.setCenterIconWithText(true)
        btnReply.setOnClickListener {
            val bundle = MessageDialogFragmentArgs(
                letterId = 0L,
                readOnly = false
            ).toBundle()

            findNavController().navigate(R.id.messageDialogFragment, bundle)
            dismiss()
        }

        // 5) 신고하기
        btnReport.setCenterIconWithText(true)
        btnReport.setOnClickListener {
            showWarningDialog(
                title = getString(R.string.dialog_fragment_message_title3),
                subTitle = getString(R.string.dialog_fragment_message_sub3)
            ) {
                sharedViewModel.reportLetter(args.letterId, reason = "Bad Request")
                Toast.makeText(requireContext(), "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

    }

    // ---------------------------------------------------------------------------------------------
    // WRITE MODE (쓰기 레이아웃: dialog_fragment_message_write.xml)
    // ---------------------------------------------------------------------------------------------
    private fun setupWriteMode() = with(writeBinding) {
        // 닫기
        ivClose.setOnClickListener { dismiss() }

        // 최대 글자수 제한 + 카운트 초기화
        etMessage.filters = arrayOf(InputFilter.LengthFilter(MAX_LENGTH))
        tvCharCount.text = "0/$MAX_LENGTH"

        // 글자수 카운트 업데이트
        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val length = s?.length ?: 0
                tvCharCount.text = "$length/$MAX_LENGTH"
            }
        })

        // 전송 버튼
        btnSend.setCenterIconWithText(true)
        btnSend.setOnClickListener {
            val message = etMessage.text?.toString()?.trim().orEmpty()
            if (message.isBlank()) {
                Toast.makeText(requireContext(), "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 전송 확인
            AlertDialog.Builder(requireContext())
                .setTitle("메세지 보틀 띄우기")
                .setMessage("메세지를 바다에 띄우시겠습니까?")
                .setPositiveButton("전송") { _, _ ->
                    // receiverNickname 은 nav args로 전달받음
                    sharedViewModel.sendLetter(
                        content = message
                    )
                    dismiss()
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Dialog Window 공통 설정 (크기/배경/중앙정렬/딤/키보드)
    // ---------------------------------------------------------------------------------------------
    override fun onStart() {
        super.onStart()

        dialog?.window?.let { window ->
            val metrics = resources.displayMetrics
            val width = (metrics.widthPixels * 0.9f).toInt()

            // 다이얼로그 크기
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

            // 투명 배경 + 딤
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.attributes = window.attributes.apply {
                gravity = Gravity.CENTER
                dimAmount = 0.6f
            }

            // 쓰기모드에서 키보드 올라올 때 레이아웃이 잘 보이도록
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    /**
     * 경고 다이얼로그 공통함수
     */
    private fun showWarningDialog(
        title: String,
        subTitle: String,
        onConfirm: () -> Unit
    ) {
        val warning = WarningDialogFragment.newInstance(title, subTitle).apply {
            onConfirmClose = { onConfirm() } // btn_close 눌렀을 때만 실행
        }
        warning.show(childFragmentManager, "warning_dialog")
    }

    /**
     * 모드에 따라 inflate 된 binding만 정리합니다.
     * (메모리 릭 방지)
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _readBinding = null
        _writeBinding = null
    }
}
