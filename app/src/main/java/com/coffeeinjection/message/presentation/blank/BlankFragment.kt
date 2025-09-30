package com.coffeeinjection.message.presentation.blank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.coffeeinjection.message.util.Logger
import com.coffeeinjection.message.databinding.FragmentBlankBinding
import com.coffeeinjection.message.presentation.MainViewModel

class BlankFragment : Fragment() {
    private var _binding: FragmentBlankBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: MainViewModel by activityViewModels()
    private val viewModel : BlankViewModel by viewModels()
    companion object {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.d("onCreateView")
        _binding = FragmentBlankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d("onViewCreated")
        initViews()
    }

    private fun initViews() = with(binding) {

    }

    override fun onDestroyView() {
        Logger.d("onDestroyView")
        super.onDestroyView()
        _binding = null
    }
}