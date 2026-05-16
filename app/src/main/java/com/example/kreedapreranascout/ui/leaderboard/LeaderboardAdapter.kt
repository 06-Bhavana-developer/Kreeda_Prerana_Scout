package com.example.kreedapreranascout.ui.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.LeaderboardResult
import com.example.kreedapreranascout.databinding.ItemLeaderboardBinding
import com.example.kreedapreranascout.util.SportsEventUtils

class LeaderboardAdapter : ListAdapter<LeaderboardResult, LeaderboardAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(result: LeaderboardResult, position: Int) {
            binding.rankTv.text = (position + 1).toString()
            binding.nameTv.text = result.studentName
            binding.collegeTv.text = result.college ?: "No College"
            
            // Standardized formatting
            binding.scoreTv.text = SportsEventUtils.formatValue(result.value, "") // testType handled by unit in DB or passed empty if unit is in result
            // Actually, LeaderboardResult has unit. Let's use a helper that takes value and unit if testType is unknown, 
            // or just use formatValue if we know the testType.
            // Since LeaderboardResult has value and unit, and unit might be "seconds" or "meters" now.
            binding.scoreTv.text = "${result.value} ${result.unit}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLeaderboardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<LeaderboardResult>() {
        override fun areItemsTheSame(oldItem: LeaderboardResult, newItem: LeaderboardResult): Boolean = 
            oldItem.studentId == newItem.studentId
        override fun areContentsTheSame(oldItem: LeaderboardResult, newItem: LeaderboardResult): Boolean = 
            oldItem == newItem
    }
}
