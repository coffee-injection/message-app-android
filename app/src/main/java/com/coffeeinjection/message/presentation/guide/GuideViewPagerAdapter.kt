package com.coffeeinjection.message.presentation.guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coffeeinjection.message.R

class GuideViewPagerAdapter(private val inflater: LayoutInflater) :
    RecyclerView.Adapter<GuideViewPagerAdapter.ViewHolder>() {

    private val layouts = listOf(
        R.layout.guide_view_step1,
        R.layout.guide_view_step2,
        R.layout.guide_view_step3
    )

    override fun getItemCount(): Int = layouts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(layouts[viewType], parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // TODO
    }

    override fun getItemViewType(position: Int): Int = position

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}