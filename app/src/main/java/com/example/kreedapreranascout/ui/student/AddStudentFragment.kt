package com.example.kreedapreranascout.ui.student

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.model.Student
import com.example.kreedapreranascout.data.repository.StudentRepository
import com.example.kreedapreranascout.databinding.FragmentAddStudentBinding
import com.example.kreedapreranascout.util.SessionManager
import com.example.kreedapreranascout.util.ViewModelFactory

class AddStudentFragment : Fragment(R.layout.fragment_add_student) {
    private var _binding: FragmentAddStudentBinding? = null
    private val binding get() = _binding!!
    private val args: AddStudentFragmentArgs by navArgs()

    private val viewModel: StudentViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(StudentRepository(db.studentDao(), db.performanceDao(), db.attendanceDao(), db.achievementDao()))
    }

    private var isEditMode = false
    private var existingStudent: Student? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddStudentBinding.bind(view)

        Log.d("AddStudentFragment", "onViewCreated: Initializing UI")
        setupDropdowns()

        val sessionManager = SessionManager(requireContext())
        val teacherId = sessionManager.getTeacherId()
        
        if (teacherId == -1L) {
            Toast.makeText(context, "Session error. Please login again.", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.loginFragment)
            return
        }

        // Check for Edit Mode
        if (args.studentId != -1L) {
            isEditMode = true
            binding.saveBtn.text = "Update Athlete Profile"
            // Title is in AppBarLayout, but we can't easily change it if it's hardcoded in XML without an ID.
            // Let's assume we can find it if we add an ID or just change the save button for now.
            loadStudentData(args.studentId)
        }

        binding.saveBtn.setOnClickListener {
            Log.d("AddStudentFragment", "Save/Update button clicked. isEditMode: $isEditMode")
            saveStudent(teacherId)
        }

        viewModel.addStudentStatus.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess { id ->
                    val msg = if (isEditMode) "Athlete Profile Updated Successfully" else "Athlete Profile Created Successfully"
                    Log.d("AddStudentFragment", "Success: $msg with ID: $id")
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearAddStudentStatus()
                    findNavController().popBackStack()
                }.onFailure { error ->
                    Log.e("AddStudentFragment", "Failed to save/update student", error)
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    binding.saveBtn.isEnabled = true
                }
            }
        }
    }

    private fun loadStudentData(studentId: Long) {
        viewModel.getStudentById(studentId).observe(viewLifecycleOwner) { student ->
            if (student != null) {
                existingStudent = student
                binding.nameEdit.setText(student.fullName)
                binding.ageEdit.setText(student.age.toString())
                binding.genderAutoComplete.setText(student.gender, false)
                binding.rollNoEdit.setText(student.rollNumber)
                binding.sportEdit.setText(student.primarySport, false)
                binding.heightEdit.setText(student.height.toString())
                binding.weightEdit.setText(student.weight.toString())
                binding.collegeEdit.setText(student.college)
            }
        }
    }

    private fun setupDropdowns() {
        val genders = arrayOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.genderAutoComplete.setAdapter(genderAdapter)

        val sports = arrayOf("Athletics", "Football", "Cricket", "Basketball", "Volleyball", "Kabaddi", "Kho-Kho")
        val sportAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sports)
        binding.sportEdit.setAdapter(sportAdapter)
    }

    private fun saveStudent(teacherId: Long) {
        val name = binding.nameEdit.text.toString().trim()
        val ageStr = binding.ageEdit.text.toString().trim()
        val gender = binding.genderAutoComplete.text.toString().trim()
        val rollNo = binding.rollNoEdit.text.toString().trim()
        val sport = binding.sportEdit.text.toString().trim()
        val heightStr = binding.heightEdit.text.toString().trim()
        val weightStr = binding.weightEdit.text.toString().trim()
        val college = binding.collegeEdit.text.toString().trim()

        // Validation
        var isValid = true

        if (name.isEmpty()) {
            binding.nameLayout.error = "Name is required"
            isValid = false
        } else binding.nameLayout.error = null

        val age = ageStr.toIntOrNull() ?: 0
        if (age <= 0 || age > 100) {
            binding.ageLayout.error = "Invalid age"
            isValid = false
        } else binding.ageLayout.error = null

        if (gender.isEmpty()) {
            binding.genderLayout.error = "Select gender"
            isValid = false
        } else binding.genderLayout.error = null

        if (rollNo.isEmpty()) {
            binding.rollNoLayout.error = "Roll Number is mandatory"
            isValid = false
        } else binding.rollNoLayout.error = null

        if (sport.isEmpty()) {
            binding.sportLayout.error = "Select primary sport"
            isValid = false
        } else binding.sportLayout.error = null

        val height = heightStr.toDoubleOrNull() ?: 0.0
        if (height <= 0) {
            binding.heightLayout.error = "Enter valid height"
            isValid = false
        } else binding.heightLayout.error = null

        val weight = weightStr.toDoubleOrNull() ?: 0.0
        if (weight <= 0) {
            binding.weightLayout.error = "Enter valid weight"
            isValid = false
        } else binding.weightLayout.error = null

        if (!isValid) return

        binding.saveBtn.isEnabled = false

        val bmi = weight / ((height / 100) * (height / 100))

        val student = Student(
            id = if (isEditMode) args.studentId else 0,
            fullName = name,
            age = age,
            gender = gender,
            classGrade = existingStudent?.classGrade ?: "N/A",
            semester = existingStudent?.semester ?: "N/A",
            section = existingStudent?.section ?: "N/A",
            rollNumber = rollNo,
            usn = rollNo,
            college = if (college.isEmpty()) "Not Specified" else college,
            primarySport = sport,
            secondarySport = existingStudent?.secondarySport,
            height = height,
            weight = weight,
            bmi = bmi,
            guardianName = existingStudent?.guardianName ?: "Not Specified",
            guardianContact = existingStudent?.guardianContact ?: "N/A",
            address = existingStudent?.address ?: "Not Specified",
            medicalNotes = existingStudent?.medicalNotes,
            profileImageUri = existingStudent?.profileImageUri,
            createdByTeacherId = teacherId,
            createdAt = existingStudent?.createdAt ?: System.currentTimeMillis(),
            isMilestoneAchieved = existingStudent?.isMilestoneAchieved ?: false
        )
        
        Log.d("AddStudentFragment", "Saving student. isEditMode: $isEditMode, Name: ${student.fullName}")
        if (isEditMode) {
            viewModel.updateStudent(student)
        } else {
            viewModel.addStudent(student)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
