package com.example.kreedapreranascout.ui.student

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kreedapreranascout.databinding.ItemBatchStudentBinding

data class BatchStudentRow(
    var name: String = "",
    var rollNumber: String = "",
    var age: String = "",
    var gender: String = "",
    var sport: String = ""
)

class BatchStudentAdapter(
    private val students: MutableList<BatchStudentRow>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<BatchStudentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBatchStudentBinding) : RecyclerView.ViewHolder(binding.root) {
        var currentWatchers = mutableListOf<TextWatcher>()

        fun removeListeners() {
            currentWatchers.forEach { 
                binding.nameEdit.removeTextChangedListener(it)
                binding.rollNoEdit.removeTextChangedListener(it)
                binding.ageEdit.removeTextChangedListener(it)
                binding.genderAutoComplete.removeTextChangedListener(it)
                binding.sportAutoComplete.removeTextChangedListener(it)
            }
            currentWatchers.clear()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBatchStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        val binding = holder.binding

        // 1. Setup UI with current model data
        holder.removeListeners()
        binding.rowNumberTv.text = "${position + 1}."
        binding.nameEdit.setText(student.name)
        binding.rollNoEdit.setText(student.rollNumber)
        binding.ageEdit.setText(student.age)
        binding.genderAutoComplete.setText(student.gender, false)
        binding.sportAutoComplete.setText(student.sport, false)

        setupDropdowns(holder)

        // 2. Attach new listeners to update model
        val nameWatcher = createSimpleWatcher { student.name = it }
        val rollWatcher = createSimpleWatcher { student.rollNumber = it }
        val ageWatcher = createSimpleWatcher { student.age = it }
        val genderWatcher = createSimpleWatcher { student.gender = it }
        val sportWatcher = createSimpleWatcher { student.sport = it }

        binding.nameEdit.addTextChangedListener(nameWatcher)
        binding.rollNoEdit.addTextChangedListener(rollWatcher)
        binding.ageEdit.addTextChangedListener(ageWatcher)
        binding.genderAutoComplete.addTextChangedListener(genderWatcher)
        binding.sportAutoComplete.addTextChangedListener(sportWatcher)

        holder.currentWatchers.addAll(listOf(nameWatcher, rollWatcher, ageWatcher, genderWatcher, sportWatcher))

        binding.removeBtn.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onRemoveClick(currentPos)
            }
        }
    }

    private fun setupDropdowns(holder: ViewHolder) {
        val genders = arrayOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_dropdown_item_1line, genders)
        holder.binding.genderAutoComplete.setAdapter(genderAdapter)

        val sports = arrayOf("Athletics", "Football", "Cricket", "Basketball", "Volleyball", "Kabaddi", "Kho-Kho")
        val sportAdapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_dropdown_item_1line, sports)
        holder.binding.sportAutoComplete.setAdapter(sportAdapter)
    }

    private fun createSimpleWatcher(onChanged: (String) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { onChanged(s?.toString() ?: "") }
        }
    }

    override fun getItemCount(): Int = students.size
}
