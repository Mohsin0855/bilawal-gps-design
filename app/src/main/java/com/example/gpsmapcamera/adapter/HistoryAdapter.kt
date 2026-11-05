package com.example.gpsmapcamera.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.databinding.ItemHistoryBinding
import com.example.gpsmapcamera.ui.HistoryModel

class HistoryAdapter(
    private var historyList: List<HistoryModel>,
    private val onDeleteClick: (HistoryModel) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        holder.binding.dataText.text = item.phoneNumber
        holder.binding.dataDescription.text = item.message

        holder.binding.dataDots.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<HistoryModel>) {
        this.historyList = newList
        notifyDataSetChanged()
    }
}