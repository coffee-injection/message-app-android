package com.coffeeinjection.message.presentation.bookmark

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.databinding.FragmentBookmarkBinding
import com.coffeeinjection.message.presentation.BaseFragment

class BookmarkFragment : BaseFragment<FragmentBookmarkBinding>(FragmentBookmarkBinding::inflate) {

    private val viewModel: BookmarkViewModel by viewModels()

    override fun setupViews() = with(binding) {

    }

    override fun setupListeners()= with(binding) {
        super.setupListeners()

        // 뒤로가기
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}