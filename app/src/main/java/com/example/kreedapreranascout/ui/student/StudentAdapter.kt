package com.example.kreedapreranascout.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedapreranascout.data.model.Student
import com.example.kreedapreranascout.databinding.ItemStudentBinding

class StudentAdapter(
    private val onItemClick: (Student) -> Unit,
    private val onEditClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudentViewHolder(private val binding: ItemStudentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(student: Student) {
            binding.apply {
                nameTv.text = student.fullName
                sportTv.text = student.primarySport
                detailsTv.text = "${student.rollNumber} • ${student.college ?: "N/A"}"
                
                val initials = student.fullName.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                initialsTv.text = if (initials.isNotEmpty()) initials else "?"

                root.setOnClickListener { onItemClick(student) }
                editBtn.setOnClickListener { onEditClick(student) }
                deleteBtn.setOnClickListener { onDeleteClick(student) }
            }
        }
    }

    class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean = oldItem == newItem
    }
}
