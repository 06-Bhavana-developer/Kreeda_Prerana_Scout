package com.example.kreedapreranascout.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.repository.UserRepository
import com.example.kreedapreranascout.databinding.FragmentLoginBinding
import com.example.kreedapreranascout.util.SessionManager
import com.example.kreedapreranascout.util.ViewModelFactory

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: AuthViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).teacherDao()
        ViewModelFactory(UserRepository(dao))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        val sessionManager = SessionManager(requireContext())

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEdit.text.toString()
            val password = binding.passwordEdit.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerTv.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        viewModel.loginStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess { teacherId ->
                sessionManager.saveSession(teacherId)
                findNavController().navigate(R.id.action_login_to_dashboard)
            }.onFailure {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
