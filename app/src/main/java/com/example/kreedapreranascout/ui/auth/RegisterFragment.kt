package com.example.kreedapreranascout.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.model.Teacher
import com.example.kreedapreranascout.data.repository.UserRepository
import com.example.kreedapreranascout.databinding.FragmentRegisterBinding
import com.example.kreedapreranascout.util.SessionManager
import com.example.kreedapreranascout.util.ViewModelFactory

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: AuthViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).teacherDao()
        ViewModelFactory(UserRepository(dao))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)

        val sessionManager = SessionManager(requireContext())

        binding.registerBtn.setOnClickListener {
            val name = binding.nameEdit.text.toString()
            val email = binding.emailEdit.text.toString()
            val school = binding.schoolEdit.text.toString()
            val password = binding.passwordEdit.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && school.isNotEmpty() && password.isNotEmpty()) {
                val teacher = Teacher(
                    fullName = name,
                    email = email,
                    schoolName = school,
                    phone = "",
                    schoolAddress = "",
                    passwordHash = "" // Will be hashed in ViewModel
                )
                viewModel.register(teacher, password)
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginTv.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        viewModel.registrationStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess { teacherId ->
                sessionManager.saveSession(teacherId)
                findNavController().navigate(R.id.action_register_to_dashboard)
            }.onFailure {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
