package com.example.gpsmapcamera.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gpsmapcamera.ui.onboarding.Onboarding1Fragment
import com.example.gpsmapcamera.ui.onboarding.Onboarding2Fragment
import com.example.gpsmapcamera.ui.onboarding.Onboarding3Fragment

class OnboardingAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Onboarding1Fragment()
            1 -> Onboarding2Fragment()
            2 -> Onboarding3Fragment()
            else -> Onboarding1Fragment()
        }
    }
}