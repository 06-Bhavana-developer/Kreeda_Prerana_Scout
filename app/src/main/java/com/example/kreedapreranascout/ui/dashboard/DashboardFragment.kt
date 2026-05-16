package com.example.kreedapreranascout.ui.dashboard

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.repository.StudentRepository
import com.example.kreedapreranascout.data.repository.UserRepository
import com.example.kreedapreranascout.databinding.FragmentDashboardBinding
import com.example.kreedapreranascout.ui.auth.AuthViewModel
import com.example.kreedapreranascout.util.SessionManager
import com.example.kreedapreranascout.util.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(StudentRepository(db.studentDao(), db.performanceDao(), db.attendanceDao(), db.achievementDao()))
    }
    private val authViewModel: AuthViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(UserRepository(db.teacherDao()))
    }

    private lateinit var sessionManager: SessionManager

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("Dashboard", "Image selected: $uri")
            saveProfileImage(uri)
        }
    }

    override fun onResume() {
        super.onResume()
        // Hide ActionBar title for a professional, clean look on the Dashboard
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
        // Show ActionBar again for other screens that need the back button/title
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        val teacherId = sessionManager.getTeacherId()
        Log.d("Dashboard", "Dashboard loaded for Teacher ID: $teacherId")

        // Load Persisted Image on startup
        loadPersistedProfileImage()

        // Observe Teacher Data
        authViewModel.getTeacherById(teacherId).observe(viewLifecycleOwner) { teacher ->
            if (teacher == null) {
                Log.e("Dashboard", "Session Invalid: Teacher not found. Redirecting.")
                sessionManager.logout()
                findNavController().navigate(R.id.loginFragment)
                return@observe
            }
            // Update subtitle with teacher info
            binding.subtitleTv.text = "Logged in as ${teacher.fullName} @ ${teacher.schoolName}"
        }

        // Observe Live Student Count
        viewModel.getStudentCount(teacherId).observe(viewLifecycleOwner) { count ->
            binding.studentCountTv.text = count.toString()
        }

        // Observe Top Performer for the spotlight card
        viewModel.getTopPerformer(teacherId).observe(viewLifecycleOwner) { result ->
            if (result != null) {
                binding.topPerformerCard.visibility = View.VISIBLE
                binding.topPerformerNameTv.text = result.studentName
                binding.topPerformerEventTv.text = result.testType
                binding.topPerformerScoreTv.text = "${result.value} ${result.unit}"
                binding.presentCountTv.text = "${result.value}"
            } else {
                binding.topPerformerCard.visibility = View.GONE
                binding.presentCountTv.text = "--"
            }
        }

        // Professional Image Handling
        binding.profileImg.setOnClickListener {
            showImageOptionsDialog()
        }

        // Dashboard Navigation Actions
        binding.addStudentCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addStudent)
        }
        
        binding.batchStudentCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_batchStudent)
        }

        binding.studentListCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_studentList)
        }
        binding.leaderboardCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_leaderboard)
        }
        binding.settingsCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_settings)
        }
    }

    private fun showImageOptionsDialog() {
        val options = arrayOf("Choose from Gallery", "Remove Photo", "Cancel")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Dashboard Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    1 -> removeProfileImage()
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun saveProfileImage(uri: Uri) {
        try {
            // Persist permission to access the URI across app restarts
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            
            val prefs = requireContext().getSharedPreferences("dashboard_settings", 0)
            prefs.edit().putString("profile_uri", uri.toString()).apply()
            
            binding.profileImg.setImageURI(uri)
            Log.d("Dashboard", "Profile image saved and persisted: $uri")
        } catch (e: Exception) {
            Log.e("Dashboard", "Error persisting image URI: ${e.message}")
            binding.profileImg.setImageURI(uri) // Still set it for current session
        }
    }

    private fun removeProfileImage() {
        val prefs = requireContext().getSharedPreferences("dashboard_settings", 0)
        prefs.edit().remove("profile_uri").apply()
        
        binding.profileImg.setImageResource(android.R.drawable.ic_menu_camera)
        Log.d("Dashboard", "Profile image removed")
    }

    private fun loadPersistedProfileImage() {
        val prefs = requireContext().getSharedPreferences("dashboard_settings", 0)
        val uriString = prefs.getString("profile_uri", null)
        
        if (uriString != null) {
            try {
                val uri = Uri.parse(uriString)
                binding.profileImg.setImageURI(uri)
                Log.d("Dashboard", "Loaded persisted image: $uri")
            } catch (e: Exception) {
                Log.e("Dashboard", "Failed to load persisted image URI", e)
                binding.profileImg.setImageResource(android.R.drawable.ic_menu_camera)
            }
        } else {
            binding.profileImg.setImageResource(android.R.drawable.ic_menu_camera)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
