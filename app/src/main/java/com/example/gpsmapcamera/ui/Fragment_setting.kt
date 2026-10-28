package com.example.gpsmapcamera.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.FragmentSettingBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupClicks()
    }

    private fun setupUi() {
        binding.touchLastDescription.text = "Photo Capture"
        binding.galleryDescription2.text = "High"
    }

    private fun setupClicks() = with(binding) {

        backarrow.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        rectangleBox.setOnClickListener { openPremium() }
        trial.setOnClickListener { openPremium() }

        mapType.setOnClickListener {
            // TODO: open Map type screen / dialog
        }
        calender.setOnClickListener {
            // TODO: open Date & time type screen / dialog
        }
        locationFormat.setOnClickListener {
            // TODO: open Location format screen / dialog
        }
        temperatureUnit.setOnClickListener {
            // TODO: open Temperature unit screen / dialog
        }
        volume.setOnClickListener {
            // TODO: open Volume button settings
        }
        touch.setOnClickListener {
            // TODO: open Touch settings, then update touchLastDescription
        }
        image.setOnClickListener {
            // TODO: open Image quality dialog, then update galleryDescription2
        }

        appLanguages.setOnClickListener {
            // TODO: e.g. findNavController().navigate(R.id.action_settingFragment_to_languagesFragment)
        }
        share.setOnClickListener { shareApp() }
        rate.setOnClickListener { rateApp() }
        privacy.setOnClickListener { openPrivacyPolicy() }
    }

    private fun openPremium() {
        // TODO: e.g. findNavController().navigate(R.id.action_settingFragment_to_subscriptionFragment)
    }

    private fun shareApp() {
        val pkg = requireContext().packageName
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this app: https://play.google.com/store/apps/details?id=$pkg"
            )
        }
        startActivity(Intent.createChooser(intent, "Share app via"))
    }

    private fun rateApp() {
        val pkg = requireContext().packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                )
            )
        }
    }

    private fun openPrivacyPolicy() {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://yourwebsite.com/privacy-policy"))
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No browser found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}