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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.FragmentSubscriptionBinding
import com.example.gpsmapcamera.viewmodel.Plan
import com.example.gpsmapcamera.viewmodel.SubscriptionEvent
import com.example.gpsmapcamera.viewmodel.SubscriptionViewModel
import kotlinx.coroutines.launch

class SubscriptionFragment : Fragment() {

    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubscriptionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        observeViewModel()
    }

    private fun setupClicks() = with(binding) {
        closebtn.setOnClickListener { closeSubscription() }

        box1.setOnClickListener { viewModel.selectPlan(Plan.YEARLY) }
        box2.setOnClickListener { viewModel.selectPlan(Plan.WEEKLY) }

        btnfreetrial.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch { viewModel.onContinueClicked() }
        }

        termOfUse.setOnClickListener { openUrl("https://your-site.com/terms") }
        privacyPolicy.setOnClickListener { openUrl("https://your-site.com/privacy") }
        subscriptionsDetail.setOnClickListener { showSubscriptionDetails() }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedPlan.collect { plan -> renderSelectedPlan(plan) }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is SubscriptionEvent.StartPurchase -> startPurchase(event.plan)
                        }
                    }
                }
            }
        }
    }

    private fun renderSelectedPlan(plan: Plan) = with(binding) {
        val yearlySelected = plan == Plan.YEARLY

        radiobutton.setImageResource(
            if (yearlySelected) R.drawable.radiobutton_selected else R.drawable.radiobutton
        )
        radiobutton2.setImageResource(
            if (yearlySelected) R.drawable.radiobutton else R.drawable.radiobutton_selected
        )

        box1.setBackgroundResource(
            if (yearlySelected) R.drawable.rectangle_selected else R.drawable.rectangle
        )
        box2.setBackgroundResource(
            if (yearlySelected) R.drawable.rectangle else R.drawable.rectangle_selected
        )

        btnfreetrial.text = getString(
            if (yearlySelected) R.string.continue_free_trial else R.string.continue_text
        )
    }

    private fun closeSubscription() {
        findNavController().navigate(R.id.action_subscriptionFragment_to_homeFragment)
    }

    private fun startPurchase(plan: Plan) {
        // TODO: connect your Google Play Billing flow here
        Toast.makeText(requireContext(), "Selected: $plan", Toast.LENGTH_SHORT).show()
    }

    private fun showSubscriptionDetails() {
        // TODO: open your Subscription Details dialog or navigate to it
        // findNavController().navigate(R.id.action_subscription_to_details)
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No browser found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}