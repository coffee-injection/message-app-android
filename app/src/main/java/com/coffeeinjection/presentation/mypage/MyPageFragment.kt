package com.coffeeinjection.presentation.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coffeeinjection.message.util.Logger
import com.coffeeinjection.message.databinding.FragmentMypageBinding
import com.coffeeinjection.presentation.home.HomeFragmentDirections

class MyPageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private val viewModel : BlankViewModel by viewModels()

    companion object {
        private const val TAG = "MyPageFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.d(TAG,"onCreateView")
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG,"onViewCreated")
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