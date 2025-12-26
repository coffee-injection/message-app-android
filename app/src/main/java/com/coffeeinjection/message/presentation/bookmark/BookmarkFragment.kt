package com.coffeeinjection.message.presentation.bookmark

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentBookmarkBinding
import com.coffeeinjection.message.presentation.BaseFragment

class BookmarkFragment : BaseFragment<FragmentBookmarkBinding>(FragmentBookmarkBinding::inflate) {

    private val viewModel: BookmarkViewModel by viewModels()

    override fun setupViews(savedInstanceState: Bundle?) {

            binding.apply {
            }
    }

    override fun setupListeners()= with(binding) {
        super.setupListeners()

    }
}