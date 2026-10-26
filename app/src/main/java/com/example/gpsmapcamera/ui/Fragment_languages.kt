package com.example.gpsmapcamera.ui.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapter.language.LanguageAdapter
import com.example.gpsmapcamera.databinding.FragmentLanguagesBinding

class LanguagesFragment : Fragment() {

    private var _binding: FragmentLanguagesBinding? = null
    private val binding get() = _binding!!

    private var selectedLanguage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val languageList = listOf(
            "English", "Spanish", "Portuguese", "Italian",
            "Hindi", "Chinese", "German", "French",
            "Turkish", "Arabic", "Indonesian", "Afrikaans",
            "Dutch", "Czech"
        )

        val adapter = LanguageAdapter(languageList) { chosenLang ->
            selectedLanguage = chosenLang
            binding.btnDone.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#6366F1")
            )
        }

        binding.rvLanguages.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvLanguages.adapter = adapter

        binding.btnDone.setOnClickListener {
            if (selectedLanguage.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please select a language", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Save language preference (SharedPreferences) if needed
                findNavController().navigate(R.id.action_languagesFragment_to_onboarding1Fragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}