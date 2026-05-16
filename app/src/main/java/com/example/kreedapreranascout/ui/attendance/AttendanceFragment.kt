package com.example.kreedapreranascout.ui.attendance

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.repository.StudentRepository
import com.example.kreedapreranascout.databinding.FragmentAttendanceBinding
import com.example.kreedapreranascout.ui.student.StudentViewModel
import com.example.kreedapreranascout.util.SessionManager
import com.example.kreedapreranascout.util.ViewModelFactory

class AttendanceFragment : Fragment(R.layout.fragment_attendance) {
    private lateinit var binding: FragmentAttendanceBinding
    private val viewModel: StudentViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(StudentRepository(db.studentDao(), db.performanceDao(), db.attendanceDao(), db.achievementDao()))
    }
    private val adapter = AttendanceAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAttendanceBinding.bind(view)

        val sessionManager = SessionManager(requireContext())
        val teacherId = sessionManager.getTeacherId()

        binding.attendanceRv.adapter = adapter

        viewModel.getAllStudents(teacherId).observe(viewLifecycleOwner) { students ->
            if (students.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.attendanceRv.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.attendanceRv.visibility = View.VISIBLE
                adapter.submitList(students)
            }
        }
    }
}
