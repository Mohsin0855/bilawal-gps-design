package com.example.gpsmapcamera.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.FragmentDirectChatBinding
import com.hbb20.CountryCodePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class Fragment_direct_chat : Fragment() {

    private var _binding: FragmentDirectChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var ccp: CountryCodePicker
    private var isWhatsAppBusiness = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ccp = CountryCodePicker(requireContext())
        ccp.setDefaultCountryUsingNameCode("PK")
        ccp.resetToDefaultCountry()

        updateCountryUI(ccp.selectedCountryFlagResourceId, ccp.selectedCountryNameCode, ccp.selectedCountryCodeWithPlus)

        binding.phoneNumberArea.setOnClickListener {
            openCountryPickerDialog()
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.clockIcon.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentDirectChat_to_fragmentHistory)
        }

        binding.radiobutton1.setImageResource(R.drawable.radiobutton_selected)
        binding.radiobutton2.setImageResource(R.drawable.chat_radio_button)

        binding.radiobutton1.setOnClickListener {
            isWhatsAppBusiness = false
            binding.radiobutton1.setImageResource(R.drawable.radiobutton_selected)
            binding.radiobutton2.setImageResource(R.drawable.chat_radio_button)
        }

        binding.radiobutton2.setOnClickListener {
            isWhatsAppBusiness = true
            binding.radiobutton2.setImageResource(R.drawable.radiobutton_selected)
            binding.radiobutton1.setImageResource(R.drawable.chat_radio_button)
        }

        binding.rectanglePortion.setOnClickListener {
            sendWhatsAppMessage()
        }
    }

    private fun openCountryPickerDialog() {
        ccp.setDialogTextColor(resources.getColor(R.color.black, requireContext().theme))
        ccp.setDialogBackgroundColor(resources.getColor(R.color.white, requireContext().theme))
        ccp.launchCountrySelectionDialog()
        ccp.setOnCountryChangeListener {
            updateCountryUI(ccp.selectedCountryFlagResourceId, ccp.selectedCountryNameCode, ccp.selectedCountryCodeWithPlus)
        }
    }
    private fun updateCountryUI(flagResId: Int, countryIso: String, phoneCode: String) {
        binding.flagIcon.setImageResource(flagResId)
        binding.countryNo.text = countryIso
        binding.codeCountry.text = phoneCode
    }

    private fun sendWhatsAppMessage() {
        val countryCode = binding.codeCountry.text.toString().trim()
        val rawNumber = binding.number.text.toString().trim()
        val message = binding.messageInput.text.toString().trim()

        if (rawNumber.isEmpty() || rawNumber == "xxx xx x") {
            Toast.makeText(requireContext(), "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            return
        }

        val cleanCode = if (countryCode.startsWith("+")) countryCode else "+$countryCode"
        val fullNumber = "$cleanCode$rawNumber".replace("+", "")

        val historyItem = HistoryModel(
            phoneNumber = fullNumber,
            message = message.ifEmpty { "No Message" }
        )

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getDatabase(requireContext()).historyDao().insertHistory(historyItem)
        }

        val url = "https://api.whatsapp.com/send?phone=$fullNumber&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = url.toUri()

            if (isWhatsAppBusiness) {
                setPackage("com.whatsapp.w4b")
            } else {
                setPackage("com.whatsapp")
            }
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            try {
                intent.setPackage(null)
                startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}