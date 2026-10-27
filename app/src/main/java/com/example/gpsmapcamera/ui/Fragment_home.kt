package com.example.gpsmapcamera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowCompat.enableEdgeToEdge
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
        setupClicks()
        enableEdgeToEdge()
    }

    private fun setupClicks() = with(binding) {
        smallCrown.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_subscriptionFragment)
        }

        setting.setOnClickListener {
            // TODO: settings screen par navigate karo
            showMessage("Settings")
        }


        editBtn.setOnClickListener {
            // TODO: gallery picker / edit photo screen
            showMessage("Edit Photo")
        }

        camera.setOnClickListener {
            // TODO: camera screen par navigate karo
            showMessage("Camera")
        }
    }

    private fun showMessage(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}