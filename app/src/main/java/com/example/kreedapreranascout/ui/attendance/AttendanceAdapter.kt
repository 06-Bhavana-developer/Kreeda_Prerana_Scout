package com.example.kreedapreranascout.ui.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedapreranascout.data.model.Student
import com.example.kreedapreranascout.databinding.ItemStudentAttendanceBinding

class AttendanceAdapter : ListAdapter<Student, AttendanceAdapter.ViewHolder>(DiffCallback) {

    private val attendanceMap = mutableMapOf<Long, String>()

    fun getAttendanceData(): Map<Long, String> = attendanceMap

    override fun submitList(list: List<Student>?) {
        list?.forEach { student ->
            if (!attendanceMap.containsKey(student.id)) {
                attendanceMap[student.id] = "Present"
            }
        }
        super.submitList(list)
    }

    inner class ViewHolder(private val binding: ItemStudentAttendanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: Student) {
            binding.nameTv.text = student.fullName
            binding.rollNoTv.text = "Roll No: ${student.rollNumber}"
            
            // Set initials for avatar
            val initials = student.fullName.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
            binding.initialsTv.text = if (initials.isNotEmpty()) initials else "?"

            // Initialize checkbox state
            binding.presentCb.setOnCheckedChangeListener(null)
            binding.presentCb.isChecked = attendanceMap[student.id] == "Present"
            
            binding.presentCb.setOnCheckedChangeListener { _, isChecked ->
                val status = if (isChecked) "Present" else "Absent"
                attendanceMap[student.id] = status
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemStudentAttendanceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean = oldItem == newItem
    }
}
