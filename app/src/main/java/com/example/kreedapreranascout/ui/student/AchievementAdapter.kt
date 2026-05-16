package com.example.kreedapreranascout.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.model.Achievement
import com.example.kreedapreranascout.databinding.ItemAchievementBinding

class AchievementAdapter : ListAdapter<Achievement, AchievementAdapter.AchievementViewHolder>(AchievementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AchievementViewHolder(private val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(achievement: Achievement) {
            binding.badgeTitle.text = achievement.title
            
            val context = itemView.context
            val color = when (achievement.level) {
                "District" -> context.getColor(R.color.accent_teal)
                "State" -> context.getColor(R.color.accent_orange)
                "National" -> context.getColor(R.color.primary_blue)
                else -> context.getColor(R.color.text_secondary)
            }
            
            binding.badgeIcon.imageTintList = android.content.res.ColorStateList.valueOf(color)
            binding.badgeTitle.setTextColor(color)
        }
    }

    class AchievementDiffCallback : DiffUtil.ItemCallback<Achievement>() {
        override fun areItemsTheSame(oldItem: Achievement, newItem: Achievement): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Achievement, newItem: Achievement): Boolean = oldItem == newItem
    }
}
