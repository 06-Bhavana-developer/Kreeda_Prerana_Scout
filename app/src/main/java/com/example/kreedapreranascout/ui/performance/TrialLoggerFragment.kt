package com.example.kreedapreranascout.ui.performance

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.model.Performance
import com.example.kreedapreranascout.data.repository.StudentRepository
import com.example.kreedapreranascout.databinding.FragmentTrialLoggerBinding
import com.example.kreedapreranascout.ui.student.StudentViewModel
import com.example.kreedapreranascout.util.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class TrialLoggerFragment : Fragment(R.layout.fragment_trial_logger) {
    private var _binding: FragmentTrialLoggerBinding? = null
    private val binding get() = _binding!!
    
    private val args: TrialLoggerFragmentArgs by navArgs()
    private val viewModel: StudentViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(StudentRepository(db.studentDao(), db.performanceDao(), db.attendanceDao(), db.achievementDao()))
    }

    private var startTime = 0L
    private var isRunning = false
    private var lastElapsed = 0L
    private val handler = Handler(Looper.getMainLooper())
    
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val elapsed = SystemClock.elapsedRealtime() - startTime
                binding.timerTv.text = formatTime(elapsed)
                handler.postDelayed(this, 10)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTrialLoggerBinding.bind(view)

        applyWindowInsets()
        setupSpinners()
        loadStudentInfo()
        observeViewModel()

        binding.startStopBtn.setOnClickListener {
            if (isRunning) stopTimer() else startTimer()
        }

        binding.resetBtn.setOnClickListener {
            resetTimer()
        }

        binding.saveTrialBtn.setOnClickListener {
            saveTrial()
        }
    }

    private fun observeViewModel() {
        viewModel.addPerformanceStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Log.d("TrialLogger", "Performance record saved")
                // Success handled, but don't pop back yet if we're waiting for badge notification
                handler.postDelayed({ 
                    if (isAdded) findNavController().popBackStack() 
                }, 1500)
            }.onFailure {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.newBadgeEarned.collectLatest { achievement ->
                Snackbar.make(binding.root, "New Badge Earned: ${achievement.title}", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(resources.getColor(R.color.primary_blue, null))
                    .setTextColor(resources.getColor(R.color.white, null))
                    .show()
            }
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.trialScrollView) { v, windowInsets ->
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

    private fun loadStudentInfo() {
        viewModel.getStudentById(args.studentId).observe(viewLifecycleOwner) { student ->
            binding.studentNameTv.text = student?.fullName ?: "Unknown Athlete"
        }
    }

    private fun setupSpinners() {
        val categories = arrayOf("Sprint", "Jump", "Throw")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.categorySpinner.setAdapter(categoryAdapter)

        val sprintEvents = arrayOf("100m Sprint", "200m Sprint", "400m Sprint")
        val jumpEvents = arrayOf("Long Jump", "High Jump", "Triple Jump")
        val throwEvents = arrayOf("Shot Put", "Discus Throw", "Javelin Throw")

        binding.categorySpinner.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = categories[position]
            val events = when (selectedCategory) {
                "Sprint" -> sprintEvents
                "Jump" -> jumpEvents
                "Throw" -> throwEvents
                else -> arrayOf()
            }
            val eventAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, events)
            binding.testTypeSpinner.setAdapter(eventAdapter)
            binding.testTypeSpinner.setText("", false)
            updateUnits(selectedCategory)
        }
    }

    private fun updateUnits(category: String) {
        val isSprint = category == "Sprint"
        val unit = if (isSprint) "seconds" else "meters"
        binding.valueInputLayout.suffixText = unit
        binding.timerCard.visibility = if (isSprint) View.VISIBLE else View.GONE
        
        if (isSprint) {
            binding.valueEdit.isEnabled = false
            binding.valueInputLayout.hint = "Stopwatch Timing"
        } else {
            binding.valueEdit.isEnabled = true
            binding.valueInputLayout.hint = "Performance Value"
            resetTimer()
        }
    }

    private fun startTimer() {
        startTime = SystemClock.elapsedRealtime() - lastElapsed
        isRunning = true
        handler.post(timerRunnable)
        binding.startStopBtn.text = "Stop"
        binding.startStopBtn.backgroundTintList = ColorStateList.valueOf(resources.getColor(android.R.color.holo_red_dark, null))
        binding.categorySpinner.isEnabled = false
        binding.testTypeSpinner.isEnabled = false
    }

    private fun stopTimer() {
        isRunning = false
        handler.removeCallbacks(timerRunnable)
        lastElapsed = SystemClock.elapsedRealtime() - startTime
        
        val minutes = (lastElapsed / 60000).toInt()
        val seconds = ((lastElapsed % 60000) / 1000).toInt()
        val hundredths = ((lastElapsed % 1000) / 10).toInt()
        
        binding.timerTv.text = String.format(Locale.US, "%02d:%02d:%02d", minutes, seconds, hundredths)
        val totalSeconds = (minutes * 60) + seconds + (hundredths / 100.0)
        binding.valueEdit.setText(String.format(Locale.US, "%.2f", totalSeconds))
        
        binding.startStopBtn.text = "Start"
        binding.startStopBtn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary_blue, null))
    }

    private fun resetTimer() {
        isRunning = false
        handler.removeCallbacks(timerRunnable)
        lastElapsed = 0L
        binding.timerTv.text = "00:00:00"
        binding.valueEdit.setText("")
        binding.startStopBtn.text = "Start"
        binding.startStopBtn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary_blue, null))
        binding.categorySpinner.isEnabled = true
        binding.testTypeSpinner.isEnabled = true
    }

    private fun formatTime(millis: Long): String {
        val minutes = (millis / 60000)
        val seconds = (millis % 60000) / 1000
        val hundredths = (millis % 1000) / 10
        return String.format(Locale.US, "%02d:%02d:%02d", minutes, seconds, hundredths)
    }

    private fun saveTrial() {
        if (isRunning) {
            Toast.makeText(context, "Please stop the stopwatch first", Toast.LENGTH_SHORT).show()
            return
        }

        val testType = binding.testTypeSpinner.text.toString()
        val category = binding.categorySpinner.text.toString()
        
        if (testType.isBlank() || category.isBlank()) {
            Toast.makeText(context, "Select category and event", Toast.LENGTH_SHORT).show()
            return
        }

        val value = binding.valueEdit.text.toString().toDoubleOrNull() ?: 0.0
        if (value <= 0) {
            Toast.makeText(context, "Enter a valid performance", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.addPerformance(Performance(
            studentId = args.studentId,
            testType = testType,
            value = value,
            unit = if (category == "Sprint") "seconds" else "meters",
            attemptNumber = 1,
            date = System.currentTimeMillis(),
            remarks = binding.remarksEdit.text.toString()
        ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
        _binding = null
    }
}
