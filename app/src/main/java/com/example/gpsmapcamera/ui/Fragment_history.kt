package com.example.gpsmapcamera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.adapter.HistoryAdapter
import com.example.gpsmapcamera.databinding.FragmentHistoryBinding
import com.example.gpsmapcamera.viewmodel.history_viewmodel
import kotlinx.coroutines.launch

class Fragment_history : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: history_viewmodel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.deleteBtn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete All")
                .setMessage("Are you sure you want to delete all history?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteAll()
                    Toast.makeText(requireContext(), "All history deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val adapter = HistoryAdapter(emptyList()) { historyItem ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this.")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.delete(historyItem)
                    Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allHistory.collect { historyList ->
                    adapter.updateData(historyList)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}