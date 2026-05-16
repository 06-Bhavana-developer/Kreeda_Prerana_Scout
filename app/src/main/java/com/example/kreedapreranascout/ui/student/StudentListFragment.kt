package com.example.kreedapreranascout.ui.student

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.model.Student
import com.example.kreedapreranascout.data.repository.StudentRepository
import com.example.kreedapreranascout.databinding.FragmentStudentListBinding
import com.example.kreedapreranascout.util.SessionManager
import com.example.kreedapreranascout.util.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class StudentListFragment : Fragment(R.layout.fragment_student_list) {
    private var _binding: FragmentStudentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(StudentRepository(db.studentDao(), db.performanceDao(), db.attendanceDao(), db.achievementDao()))
    }

    private var allStudents = listOf<Student>()
    private lateinit var adapter: StudentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudentListBinding.bind(view)

        Log.d("StudentList", "onViewCreated: Initializing Student Directory")
        
        applyWindowInsets()
        
        val sessionManager = SessionManager(requireContext())
        val teacherId = sessionManager.getTeacherId()

        setupRecyclerView()
        observeStudents(teacherId)
        setupSearch()

        binding.addStudentFab.setOnClickListener {
            Log.d("StudentList", "FAB clicked: Navigating to Add Student")
            findNavController().navigate(R.id.action_studentList_to_addStudent)
        }
    }

    private fun applyWindowInsets() {
        // Fix FAB positioning for all navigation modes (3-button, gesture, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(binding.addStudentFab) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            Log.d("StudentList", "Applying bottom inset: ${insets.bottom}")
            
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                // Add system navigation bar height to the base 32dp margin
                bottomMargin = insets.bottom + resources.getDimensionPixelSize(R.dimen.fab_base_margin_bottom)
                rightMargin = insets.right + resources.getDimensionPixelSize(R.dimen.fab_base_margin_end)
            }
            windowInsets
        }

        // Adjust RecyclerView padding so the last item isn't hidden by the FAB or nav bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.studentRv) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                insets.bottom + resources.getDimensionPixelSize(R.dimen.rv_bottom_padding_extra)
            )
            windowInsets
        }
    }

    private fun setupRecyclerView() {
        adapter = StudentAdapter(
            onItemClick = { student ->
                Log.d("StudentList", "Student clicked: ${student.fullName}")
                val action = StudentListFragmentDirections.actionStudentListToStudentProfile(student.id)
                findNavController().navigate(action)
            },
            onEditClick = { student ->
                Log.d("StudentList", "Edit clicked for: ${student.fullName}")
                val action = StudentListFragmentDirections.actionStudentListToAddStudent(student.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { student ->
                Log.d("StudentList", "Delete requested for: ${student.fullName}")
                showDeleteConfirmation(student)
            }
        )

        binding.studentRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@StudentListFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeStudents(teacherId: Long) {
        viewModel.getAllStudents(teacherId).observe(viewLifecycleOwner) { students ->
            Log.d("StudentList", "Observed ${students.size} students")
            allStudents = students
            updateUI(students)
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterStudents(newText)
                return true
            }
        })
    }

    private fun filterStudents(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            allStudents
        } else {
            allStudents.filter {
                it.fullName.contains(query, ignoreCase = true) || 
                it.rollNumber.contains(query, ignoreCase = true)
            }
        }
        Log.d("StudentList", "Filtering: query='$query', results=${filteredList.size}")
        updateUI(filteredList)
    }

    private fun updateUI(students: List<Student>) {
        if (students.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.studentRv.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.studentRv.visibility = View.VISIBLE
            adapter.submitList(students)
        }
    }

    private fun showDeleteConfirmation(student: Student) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Athlete Profile")
            .setMessage("Are you sure you want to delete ${student.fullName}? All performance data for this athlete will be permanently lost.")
            .setPositiveButton("Delete") { _, _ ->
                Log.d("StudentList", "Confirming delete for student ID: ${student.id}")
                viewModel.deleteStudent(student)
                Snackbar.make(binding.root, "${student.fullName} deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        viewModel.addStudent(student)
                    }.show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
