package com.coffeeinjection.message.presentation.bookmark

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentBookmarkBinding
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.message.presentation.activity.SharedViewModel
import com.coffeeinjection.message.presentation.bookmark.adapter.BookmarkAdapter
import kotlinx.coroutines.launch
import com.coffeeinjection.message.presentation.message.MessageDialogFragmentArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment : BaseFragment<FragmentBookmarkBinding>(FragmentBookmarkBinding::inflate) {

    private val viewModel: BookmarkViewModel by viewModels()
    private var lastRefreshToken: Long = 0L


    /**
     * 북마크 해제/ 신고시 화면 갱신을 위한 프로퍼티
     */
    private val bookmarkBackStackEntry by lazy {
        findNavController().getBackStackEntry(R.id.bookmarkFragment)
    }


    // 어댑터 초기화
    private val adapter by lazy {
        BookmarkAdapter(
            onClickItem = { item ->
                val bundle = MessageDialogFragmentArgs(
                    letterId = item.letterId,
                    readOnly = true,
                    entry = "bookmark"
                ).toBundle()
                findNavController().navigate(R.id.messageDialogFragment, bundle)
            },
            onClickBookmark = { item ->
                // 토글/해제 처리
            }
        )
    }


    override fun setupViews(savedInstanceState: Bundle?) {

        binding.apply {
            // TitleBar
            titleBar.setupDefault(getString(R.string.title_bookmarks))

            // RecyclerView
            rvBookmarks.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = this@BookmarkFragment.adapter
                setHasFixedSize(true)
            }
        }
        viewModel.loadBookmarks() // 북마크 리스트 로드

    }

    override fun setupCollectors(): Unit = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.bookmarks.collect { list ->
                        adapter.submitList(list)

                        // 북마크가 비었을 때 처리
                        layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.INVISIBLE
                        rvBookmarks.visibility = if (list.isEmpty()) View.INVISIBLE else View.VISIBLE
                    }
                }

                launch {
                    viewModel.loading.collect { loading ->
                        // TODO: 로딩 UI
                        // binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.error.collect { msg ->
                        if (!msg.isNullOrBlank()) {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // 북마크 해제/ 신고시 화면 갱신
                launch {
                    findNavController().currentBackStackEntryFlow.collect { entry ->
                        // 현재 top destination이 BookmarkFragment가 된 순간(= 다이얼로그 닫힘 포함)
                        if (entry.destination.id != R.id.bookmarkFragment) return@collect

                        val token = entry.savedStateHandle.get<Long>("refresh_bookmarks") ?: 0L
                        if (token != 0L && token != lastRefreshToken) {
                            lastRefreshToken = token
                            viewModel.loadBookmarks()
                        }
                    }
                }


            }
        }
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()
    }


}