package com.coffeeinjection.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * protected로 제한 : 상속받은 Fragment 내부에서만 호출/재정의할 수 있음
 * abstract 직접 생성 금지(템플릿 강제): BaseFragment 자체는 화면이 아니고 공통 규약/도우미를 제공함 --> 클래스로 만들어두면 실수로 인스턴스화하는 일을 막고, 반드시 하위 클래스에서만 쓰도록 강제함
 * - 인터페이스만으로는 상태 필드를 가질 수 없기 떄문에 구현+상태를 공유 가능한 추상 클래스 사용
 * open fun으로 선택적 확장 가능하도록 세팅
 */
abstract class BaseFragment<VB : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {
    // backing field로 관리 (onDestroyView에서 null)
    private var _binding: VB? = null
    protected val binding: VB get() = _binding ?: error("Binding is only valid between onCreateView and onDestroyView.")

    /** 로그 태그: 클래스명 + 해시 (인스턴스 구분 용이) */
    protected val TAG: String by lazy { "${javaClass.simpleName}" }

    // ---------- 생명주기 로그 ----------
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.tag(TAG).d("[$TAG] onCreateView() ================================================================================================================")
        _binding = inflate(inflater, container, false)
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag(TAG).d("[$TAG] onViewCreated() ================================================================================================================")

        setupViews()
        setupListeners()
        setupCollectors()
    }

    override fun onResume() {
        super.onResume()
        Timber.tag(TAG).d("[$TAG] onResume() ================================================================================================================")
    }

    override fun onDestroyView() {
        Timber.tag(TAG).d("[$TAG] onDestroyView() ================================================================================================================")
        _binding = null
        super.onDestroyView()
    }

    // ---------- 하위 클래스에서 주로 쓰는 훅 ----------
    /** UI 초기화 (findViewById 대체, RecyclerView/Adapter, Toolbar 등) */
    protected abstract fun setupViews()

    /** 클릭/제스처/어댑터 리스너 등 이벤트 바인딩 */
    protected open fun setupListeners() {}

    /** Flow/StateFlow 등 비동기 스트림 수집 위치 (repeatOnLifecycle로 안전 수집) */
    protected open fun setupCollectors() {}

    // ---------- Flow 수집 도우미 ----------
    /**
     * 화면이 STARTED일 때 collectLatest, STOPPED에서 자동 취소.
     * 화면 회전/뒤로가기 등 생명주기 변화에 자동 대응.
     */
    protected fun <T> collectLatestOnStart(flow: Flow<T>, block: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collectLatest(block)
            }
        }
    }

    /**
     * 화면이 STARTED일 때 collect(버퍼 없이), STOPPED에서 자동 취소.
     */
    protected fun <T> collectOnStart(flow: Flow<T>, block: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect(block)
            }
        }
    }
}