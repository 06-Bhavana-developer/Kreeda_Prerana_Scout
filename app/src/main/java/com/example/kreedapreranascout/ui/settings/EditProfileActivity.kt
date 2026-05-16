package com.example.kreedapreranascout.ui.settings

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.model.Teacher
import com.example.kreedapreranascout.data.repository.UserRepository
import com.example.kreedapreranascout.databinding.ActivityEditProfileBinding
import com.example.kreedapreranascout.ui.auth.AuthViewModel
import com.example.kreedapreranascout.util.SessionManager
import com.example.kreedapreranascout.util.ViewModelFactory

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: AuthViewModel by viewModels {
        val dao = AppDatabase.getDatabase(this).teacherDao()
        ViewModelFactory(UserRepository(dao))
    }
    
    private var currentTeacher: Teacher? = null
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("EditProfile", "Image selected: $uri")
            selectedImageUri = uri
            binding.profileImg.setImageURI(uri)
            
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e("EditProfile", "Failed to take persistable permission", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        Log.d("EditProfile", "Activity created")
        
        setupToolbar()
        
        val sessionManager = SessionManager(this)
        val teacherId = sessionManager.getTeacherId()
        
        viewModel.getTeacherById(teacherId).observe(this) { teacher ->
            if (teacher != null) {
                currentTeacher = teacher
                if (selectedImageUri == null) {
                    populateData(teacher)
                }
            }
        }
        
        binding.changeImgFab.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        
        binding.removeImgBtn.setOnClickListener {
            selectedImageUri = null
            binding.profileImg.setImageResource(android.R.drawable.ic_menu_camera)
        }
        
        binding.saveBtn.setOnClickListener {
            saveProfile()
        }
        
        viewModel.updateStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun populateData(teacher: Teacher) {
        binding.nameEdit.setText(teacher.fullName)
        binding.schoolEdit.setText(teacher.schoolName)
        binding.emailEdit.setText(teacher.email)
        binding.phoneEdit.setText(teacher.phone)
        
        if (!teacher.profileImageUri.isNullOrEmpty()) {
            try {
                selectedImageUri = Uri.parse(teacher.profileImageUri)
                binding.profileImg.setImageURI(selectedImageUri)
            } catch (e: Exception) {
                Log.e("EditProfile", "Error parsing profile URI", e)
            }
        }
    }

    private fun saveProfile() {
        val teacher = currentTeacher ?: return
        
        val newName = binding.nameEdit.text.toString().trim()
        val newSchool = binding.schoolEdit.text.toString().trim()
        val newEmail = binding.emailEdit.text.toString().trim()
        val newPhone = binding.phoneEdit.text.toString().trim()
        
        if (newName.isEmpty()) {
            binding.nameLayout.error = "Name cannot be empty"
            return
        }
        
        val updatedTeacher = teacher.copy(
            fullName = newName,
            schoolName = newSchool,
            email = newEmail,
            phone = newPhone,
            profileImageUri = selectedImageUri?.toString()
        )
        
        Log.d("EditProfile", "Updating teacher profile")
        viewModel.updateProfile(updatedTeacher)
    }
}
