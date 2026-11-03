package com.example.gpsmapcamera.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapter.OnboardingAdapter
import com.example.gpsmapcamera.databinding.FragmentOnboardingContainerBinding

class OnboardingContainerFragment : Fragment() {

    private var _binding: FragmentOnboardingContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = OnboardingAdapter(this)
        binding.viewPagerOnboarding.adapter = adapter
        binding.viewPagerOnboarding.isUserInputEnabled = true
    }

    fun navigateNext() {
        val currentItem = binding.viewPagerOnboarding.currentItem
        if (currentItem < 2) {
            binding.viewPagerOnboarding.currentItem = currentItem + 1
        } else {
            findNavController().navigate(R.id.action_onboardingContainer_to_subscriptionFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}