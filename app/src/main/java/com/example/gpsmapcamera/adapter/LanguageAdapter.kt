package com.example.gpsmapcamera.adapter.language

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val languageList: List<String>,
    private val onLanguageSelected: (String) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class LanguageViewHolder(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(language: String, position: Int) {
            binding.tvLanguageName.text = language
            val isSelected = position == selectedPosition
            binding.rbLanguage.isChecked = isSelected

            if (isSelected) {
                binding.tvLanguageName.setTextColor(Color.parseColor("#6366F1"))
                binding.rbLanguage.buttonTintList = ColorStateList.valueOf(Color.parseColor("#6366F1"))
            } else {
                // Normal state
                binding.tvLanguageName.setTextColor(Color.parseColor("#1e293b"))
                binding.rbLanguage.buttonTintList = ColorStateList.valueOf(Color.parseColor("#6C7A89"))
            }

            binding.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onLanguageSelected(language)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(languageList[position], position)
    }

    override fun getItemCount(): Int = languageList.size
}