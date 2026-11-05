package com.example.gpsmapcamera.adapter
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.databinding.ItemLanguageBinding
import androidx.core.graphics.toColorInt

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
                binding.tvLanguageName.setTextColor("#6366F1".toColorInt())
                binding.rbLanguage.buttonTintList = ColorStateList.valueOf("#6366F1".toColorInt())
            } else {
                // Normal state
                binding.tvLanguageName.setTextColor("#1e293b".toColorInt())
                binding.rbLanguage.buttonTintList = ColorStateList.valueOf("#6C7A89".toColorInt())
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