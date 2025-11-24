package com.coffeeinjection.presentation.blank

import android.os.Bundle
import androidx.fragment.app.viewModels
//import androidx.fragment.app.viewModels
import com.coffeeinjection.message.databinding.FragmentBlankBinding
import com.coffeeinjection.message.presentation.BaseFragment

class BlankFragment : BaseFragment<FragmentBlankBinding>(FragmentBlankBinding::inflate) {

    private val viewModel: BlankViewModel by viewModels()

    override fun setupViews(savedInstanceState: Bundle?) {
        TODO("Not yet implemented")
    }
}