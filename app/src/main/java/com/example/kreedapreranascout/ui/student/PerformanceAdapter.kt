package com.example.kreedapreranascout.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedapreranascout.data.model.Performance
import com.example.kreedapreranascout.databinding.ItemPerformanceBinding
import com.example.kreedapreranascout.util.SportsEventUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PerformanceAdapter : ListAdapter<Performance, PerformanceAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemPerformanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(performance: Performance) {
            binding.testTypeTv.text = performance.testType
            binding.valueTv.text = SportsEventUtils.formatValue(performance.value, performance.testType)
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.dateTv.text = sdf.format(Date(performance.date))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPerformanceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Performance>() {
        override fun areItemsTheSame(oldItem: Performance, newItem: Performance): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Performance, newItem: Performance): Boolean = oldItem == newItem
    }
}
