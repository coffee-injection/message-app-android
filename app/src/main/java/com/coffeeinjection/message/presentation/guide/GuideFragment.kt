package com.coffeeinjection.message.presentation.guide

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.FragmentGuideBinding
import com.coffeeinjection.message.presentation.BaseFragment
import com.coffeeinjection.presentation.sign_in.SignInFragmentDirections

class GuideFragment : BaseFragment<FragmentGuideBinding>(FragmentGuideBinding::inflate) {

    val viewModel: GuideViewModel by viewModels()
    private var lastPage = 0
    val viewPagerAdapter by lazy { GuideViewPagerAdapter(this.layoutInflater) }

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
        }
    }

    private val viewPagerCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (position > lastPage) viewModel.nextGuide()
            else if (position < lastPage) viewModel.previousGuide()
            lastPage = position
        }
    }

    override fun setupViews(savedInstanceState: Bundle?) = with(binding) {
        viewPager.adapter = viewPagerAdapter
        viewPager.registerOnPageChangeCallback(viewPagerCallback)
        dotsIndicator.attachTo(viewPager)
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()
        with(viewModel) {
            btnNext.setOnClickListener {
                viewModel.nextGuide()
            }
            btnBack.setOnClickListener {
                viewModel.previousGuide()
            }
            btnSkip.setOnClickListener {
                viewModel.skipGuide()
            }

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {


            })
        }
    }

    override fun setupCollectors() = with(binding) {
        super.setupCollectors()
        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            lastPage = state.index
            when (state) {
                GuideViewState.GUIDE_STEP_1 -> {
                    viewPager.setCurrentItem(state.index, true)
                    btnBack.visibility = View.GONE
                    btnSkip.visibility = View.VISIBLE
                    btnNext.setText(R.string.guide_next)
                }
                GuideViewState.GUIDE_STEP_2 -> {
                    viewPager.setCurrentItem(state.index, true)
                    btnBack.visibility = View.VISIBLE
                    btnSkip.visibility = View.VISIBLE
                    btnNext.setText(R.string.guide_next)
                }
                GuideViewState.GUIDE_STEP_3 -> {
                    viewPager.setCurrentItem(state.index, true)
                    btnBack.visibility = View.VISIBLE
                    btnSkip.visibility = View.GONE
                    btnNext.setText(R.string.guide_start)
                }
                GuideViewState.GUIDE_SKIP -> this@GuideFragment.findNavController().navigate(
                    GuideFragmentDirections.actionGuideFragmentToSignInFragment()
                )
            }
        }
    }

    override fun onDestroyView() {
        binding.viewPager.unregisterOnPageChangeCallback(viewPagerCallback)
        super.onDestroyView()
    }
}