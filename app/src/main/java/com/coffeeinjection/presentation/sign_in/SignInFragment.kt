package com.coffeeinjection.presentation.sign_in

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.coffeeinjection.message.databinding.FragmentSignInBinding
import com.coffeeinjection.message.util.Logger
import kotlin.getValue

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val viewModel : SignInViewModel by viewModels()
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
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
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