package com.coffeeinjection.presentation.blank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
//import androidx.fragment.app.viewModels
import com.coffeeinjection.message.util.Logger
import com.coffeeinjection.message.databinding.FragmentBlankBinding
import com.coffeeinjection.presentation.BaseFragment

class BlankFragment : BaseFragment<FragmentBlankBinding>(FragmentBlankBinding::inflate) {

    private val viewModel: BlankViewModel by viewModels()
    override fun setupViews() = with(binding) {

    }
}