package com.example.kreedapreranascout.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.databinding.FragmentSettingsBinding
import com.example.kreedapreranascout.util.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        val sessionManager = SessionManager(requireContext())
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        // Notification Switch Logic
        val isEnabled = prefs.getBoolean("notifications_enabled", true)
        binding.notificationSwitch.isChecked = isEnabled
        updateNotificationStatus(isEnabled)

        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d("Settings", "Notification toggle changed: $isChecked")
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            updateNotificationStatus(isChecked)
            
            val message = if (isChecked) "Notifications Enabled" else "Notifications Disabled"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        binding.logoutBtn.setOnClickListener {
            showLogoutConfirmation(sessionManager)
        }

        binding.editProfileBtn.setOnClickListener {
            Log.d("Settings", "Edit Profile clicked, launching EditProfileActivity")
            try {
                val intent = Intent(requireContext(), EditProfileActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("Settings", "Failed to launch EditProfileActivity", e)
                Toast.makeText(context, "Unable to open profile editor", Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchAccountBtn.setOnClickListener {
            showLogoutConfirmation(sessionManager)
        }

        binding.aboutBtn.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun updateNotificationStatus(isEnabled: Boolean) {
        binding.notificationStatusTv.text = if (isEnabled) "Notifications Enabled" else "Notifications Disabled"
    }

    private fun showLogoutConfirmation(sessionManager: SessionManager) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout from the Scout application?")
            .setPositiveButton("Logout") { _, _ ->
                Log.d("Settings", "User logging out")
                sessionManager.logout()
                findNavController().navigate(R.id.loginFragment)
            }
            .setNegativeButton("Stay", null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About Kreeda Prerana")
            .setMessage("Kreeda Prerana Scout v1.0.0\n\nDeveloped for academic excellence in sports talent identification and progress tracking.\n\nDeveloper: Kreeda Team")
            .setPositiveButton("Close", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
