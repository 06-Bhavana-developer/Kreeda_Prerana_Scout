package com.example.kreedapreranascout.ui.student

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.repository.StudentRepository
import com.example.kreedapreranascout.databinding.FragmentStudentProfileBinding
import com.example.kreedapreranascout.util.SportsEventUtils
import com.example.kreedapreranascout.util.ViewModelFactory

class StudentProfileFragment : Fragment(R.layout.fragment_student_profile) {
    private var _binding: FragmentStudentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val args: StudentProfileFragmentArgs by navArgs()
    private val viewModel: StudentViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(StudentRepository(db.studentDao(), db.performanceDao(), db.attendanceDao(), db.achievementDao()))
    }
    
    private val performanceAdapter = PerformanceAdapter()
    private val achievementAdapter = AchievementAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudentProfileBinding.bind(view)

        setupRecyclerViews()
        val athleteId = args.studentId

        // Observe Athlete Basic Info
        viewModel.getStudentById(athleteId).observe(viewLifecycleOwner) { athlete ->
            athlete?.let {
                binding.nameTv.text = it.fullName
                binding.studentDetailsTv.text = "${it.rollNumber} | ${it.college ?: "No College"}"
                binding.bmiTv.text = String.format("%.1f", it.bmi)
                binding.ageTv.text = it.age.toString()
                binding.genderTv.text = it.gender
                binding.sportChip.text = it.primarySport
                
                binding.milestoneBadge.visibility = if (it.isMilestoneAchieved) View.VISIBLE else View.GONE
                
                val initials = it.fullName.split(" ")
                    .filter { namePart -> namePart.isNotBlank() }
                    .take(2)
                    .joinToString("") { part -> part.first().uppercase() }
                binding.initialsTv.text = if (initials.isNotEmpty()) initials else "?"
            }
        }

        // Observe Milestone Badges (Achievements)
        viewModel.getAchievements(athleteId).observe(viewLifecycleOwner) { achievements ->
            if (achievements.isNullOrEmpty()) {
                binding.emptyBadgesTv.visibility = View.VISIBLE
                binding.badgesRv.visibility = View.GONE
            } else {
                binding.emptyBadgesTv.visibility = View.GONE
                binding.badgesRv.visibility = View.VISIBLE
                achievementAdapter.submitList(achievements)
            }
        }

        // Observe Performance History
        viewModel.getPerformance(athleteId).observe(viewLifecycleOwner) { history ->
            if (history.isNullOrEmpty()) {
                binding.emptyHistoryLayout.visibility = View.VISIBLE
                binding.performanceRv.visibility = View.GONE
            } else {
                binding.emptyHistoryLayout.visibility = View.GONE
                binding.performanceRv.visibility = View.VISIBLE
                performanceAdapter.submitList(history)
            }
        }

        // Observe Latest Performance for Insights
        viewModel.getLatestPerformance(athleteId).observe(viewLifecycleOwner) { performance ->
            if (performance != null) {
                binding.latestPerformanceCard.visibility = View.VISIBLE
                binding.latestTestTv.text = performance.testType
                
                // Standardized Formatting
                binding.latestValueTv.text = SportsEventUtils.formatValue(performance.value, performance.testType)
                
                viewModel.getPerformanceInsight(athleteId, performance.testType).observe(viewLifecycleOwner) { insight ->
                    binding.insightTv.text = insight
                    val lowerBetter = SportsEventUtils.isLowerBetter(performance.testType)
                    val isImprovement = insight.contains("Improved", ignoreCase = true)
                    
                    if (isImprovement) {
                        binding.insightTv.setTextColor(resources.getColor(R.color.accent_teal, null))
                    } else if (insight.contains("Needs", ignoreCase = true)) {
                        binding.insightTv.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                    } else {
                        binding.insightTv.setTextColor(resources.getColor(R.color.text_secondary, null))
                    }
                }
            } else {
                binding.latestPerformanceCard.visibility = View.GONE
            }
        }

        binding.startTrialBtn.setOnClickListener {
            val action = StudentProfileFragmentDirections.actionStudentProfileToTrialLogger(athleteId)
            findNavController().navigate(action)
        }

        binding.viewCurveBtn.setOnClickListener {
            val action = StudentProfileFragmentDirections.actionStudentProfileToTalentCurve(athleteId)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerViews() {
        binding.performanceRv.adapter = performanceAdapter
        
        binding.badgesRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = achievementAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
