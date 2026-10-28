package com.example.gpsmapcamera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.camTranslatorText.isSelected = true
        setupClicks()
    }

    private fun setupClicks() = with(binding) {
        smallCrown.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_subscriptionFragment)
        }
        setting.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingFragment)
        }


        editBtn.setOnClickListener { showMessage("Edit Photo") }

        camera.setOnClickListener { showMessage("Camera") }
        video.setOnClickListener { showMessage("Video") }

        qrCode.setOnClickListener { showMessage("QR Code") }
        directChat.setOnClickListener { showMessage("Direct Chat") }

        attendence.setOnClickListener { showMessage("Attendence") }
        camTranslator.setOnClickListener { showMessage("CamTranslator") }

        savedGirl1.setOnClickListener { showMessage("Saved Images") }
        savedGirl2.setOnClickListener { showMessage("Saved Videos") }
    }

    private fun showMessage(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}