package com.example.kreedapreranascout.ui.student

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.model.Student
import com.example.kreedapreranascout.data.repository.StudentRepository
import com.example.kreedapreranascout.databinding.FragmentBatchStudentBinding
import com.example.kreedapreranascout.util.SessionManager
import com.example.kreedapreranascout.util.ViewModelFactory
import kotlinx.coroutines.launch

class BatchStudentFragment : Fragment(R.layout.fragment_batch_student) {
    private var _binding: FragmentBatchStudentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(StudentRepository(db.studentDao(), db.performanceDao(), db.attendanceDao(), db.achievementDao()))
    }

    private lateinit var adapter: BatchStudentAdapter
    private val studentRows = mutableListOf<BatchStudentRow>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBatchStudentBinding.bind(view)
        Log.d("AthleteEntry", "onViewCreated: Initializing Athlete Batch Entry Screen")

        setupRecyclerView()
        applyWindowInsets()

        binding.addRowBtn.setOnClickListener {
            Log.d("AthleteEntry", "Add Row Button Clicked. Current rows: ${studentRows.size}")
            addRow()
        }

        binding.saveBatchBtn.setOnClickListener {
            Log.d("AthleteEntry", "Save All Button Clicked. Current rows: ${studentRows.size}")
            saveBatch()
        }

        viewModel.batchAddStatus.observe(viewLifecycleOwner) { result ->
            result?.let {
                binding.loadingOverlay.visibility = View.GONE
                it.onSuccess { ids ->
                    Log.d("AthleteEntry", "Batch save successful. IDs: ${ids.size}")
                    Toast.makeText(context, getString(R.string.athletes_saved_success), Toast.LENGTH_SHORT).show()
                    viewModel.clearBatchAddStatus()
                    findNavController().popBackStack()
                }.onFailure { error ->
                    Log.e("AthleteEntry", "Batch save failed", error)
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    binding.saveBatchBtn.isEnabled = true
                }
            }
        }

        // Add an initial row if the list is empty
        if (studentRows.isEmpty()) {
            addRow()
        }
        updateEmptyState()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomActionLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            Log.d("AthleteEntry", "Applying bottom navigation inset: ${insets.bottom}")
            
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
            }
            windowInsets
        }
    }

    private fun setupRecyclerView() {
        adapter = BatchStudentAdapter(studentRows) { position ->
            Log.d("AthleteEntry", "Removing row at position: $position")
            if (position in studentRows.indices) {
                studentRows.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, studentRows.size)
                updateEmptyState()
            }
        }
        binding.batchRecyclerView.adapter = adapter
    }

    private fun addRow() {
        studentRows.add(BatchStudentRow())
        val newPos = studentRows.size - 1
        adapter.notifyItemInserted(newPos)
        binding.batchRecyclerView.post {
            binding.batchRecyclerView.smoothScrollToPosition(newPos)
        }
        updateEmptyState()
        Log.d("AthleteEntry", "Row added at index $newPos. Total rows: ${studentRows.size}")
    }

    private fun updateEmptyState() {
        if (studentRows.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.batchRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.batchRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun saveBatch() {
        val sessionManager = SessionManager(requireContext())
        val teacherId = sessionManager.getTeacherId()

        if (teacherId == -1L) {
            Toast.makeText(context, "Session error. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        val filledRows = studentRows.filter { 
            it.name.isNotBlank() || it.rollNumber.isNotBlank() || it.age.isNotBlank() || it.sport.isNotBlank()
        }

        if (filledRows.isEmpty()) {
            Toast.makeText(context, "Please add and fill at least one athlete", Toast.LENGTH_SHORT).show()
            return
        }

        for ((index, row) in filledRows.withIndex()) {
            if (row.name.isBlank()) {
                showValidationError("Name required at row ${index + 1}")
                return
            }
            if (row.rollNumber.isBlank()) {
                showValidationError("Roll Number required at row ${index + 1}")
                return
            }
            val age = row.age.toIntOrNull()
            if (age == null || age <= 0 || age > 100) {
                showValidationError("Valid numeric Age required at row ${index + 1}")
                return
            }
            if (row.sport.isBlank()) {
                showValidationError("Sport must be selected for row ${index + 1}")
                return
            }
        }

        val rollNumbers = filledRows.map { it.rollNumber.trim() }
        if (rollNumbers.size != rollNumbers.distinct().size) {
            showValidationError("Duplicate Roll Numbers found in batch")
            return
        }

        binding.loadingOverlay.visibility = View.VISIBLE
        binding.saveBatchBtn.isEnabled = false

        lifecycleScope.launch {
            try {
                val existingRollNumbers = viewModel.getAllRollNumbers(teacherId)
                val databaseDuplicates = rollNumbers.filter { it in existingRollNumbers }
                
                if (databaseDuplicates.isNotEmpty()) {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.saveBatchBtn.isEnabled = true
                    showValidationError("Roll Numbers already exist: ${databaseDuplicates.joinToString()}")
                    return@launch
                }

                val studentsToSave = filledRows.map { row ->
                    Student(
                        fullName = row.name.trim(),
                        age = row.age.trim().toInt(),
                        gender = row.gender.ifBlank { "Male" },
                        classGrade = "N/A",
                        section = "N/A",
                        rollNumber = row.rollNumber.trim(),
                        primarySport = row.sport,
                        secondarySport = null,
                        height = 0.0,
                        weight = 0.0,
                        bmi = 0.0,
                        guardianName = "Not Specified",
                        guardianContact = "N/A",
                        address = "Not Specified",
                        medicalNotes = null,
                        createdByTeacherId = teacherId
                    )
                }

                Log.d("AthleteEntry", "Saving ${studentsToSave.size} athletes to Room")
                viewModel.addBatchStudents(studentsToSave)
            } catch (e: Exception) {
                Log.e("AthleteEntry", "Error during save process", e)
                binding.loadingOverlay.visibility = View.GONE
                binding.saveBatchBtn.isEnabled = true
                Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showValidationError(message: String) {
        Log.w("AthleteEntry", "Validation error: $message")
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
