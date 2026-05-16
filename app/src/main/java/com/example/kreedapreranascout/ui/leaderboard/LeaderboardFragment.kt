package com.example.kreedapreranascout.ui.leaderboard

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.local.LeaderboardResult
import com.example.kreedapreranascout.data.repository.StudentRepository
import com.example.kreedapreranascout.databinding.FragmentLeaderboardBinding
import com.example.kreedapreranascout.ui.student.StudentViewModel
import com.example.kreedapreranascout.util.ViewModelFactory

class LeaderboardFragment : Fragment(R.layout.fragment_leaderboard) {
    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(StudentRepository(db.studentDao(), db.performanceDao(), db.attendanceDao(), db.achievementDao()))
    }
    
    private val leaderboardAdapter = LeaderboardAdapter()
    private var leaderboardLiveData: LiveData<List<LeaderboardResult>>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLeaderboardBinding.bind(view)

        setupSpinner()
        setupRecyclerView()

        binding.testFilterSpinner.setOnItemClickListener { _, _, _, _ ->
            val selectedTest = binding.testFilterSpinner.text.toString()
            updateLeaderboard(selectedTest)
        }

        // Trigger initial update based on default value
        val initialTest = binding.testFilterSpinner.text.toString()
        if (initialTest.isNotEmpty()) {
            updateLeaderboard(initialTest)
        }
    }

    private fun setupSpinner() {
        val tests = arrayOf("100m Sprint", "200m Sprint", "Long Jump", "High Jump", "Shot Put")
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tests)
        binding.testFilterSpinner.setAdapter(arrayAdapter)
    }

    private fun setupRecyclerView() {
        binding.leaderboardRv.apply {
            adapter = leaderboardAdapter
            // Enable built-in item animations
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        }
    }

    private fun updateLeaderboard(testType: String) {
        Log.d("Leaderboard", "Fetching leaderboard for: $testType")
        
        // Remove previous observer to prevent multiple data streams
        leaderboardLiveData?.removeObservers(viewLifecycleOwner)
        
        leaderboardLiveData = viewModel.getLeaderboard(testType)
        leaderboardLiveData?.observe(viewLifecycleOwner) { results ->
            if (results.isNullOrEmpty()) {
                showEmptyState(testType)
            } else {
                showContent(results)
            }
        }
    }

    private fun showEmptyState(testType: String) {
        binding.leaderboardRv.visibility = View.GONE
        binding.emptyStateLayout.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(300).start()
        }
        binding.emptyStateTv.text = "No athletes found for '$testType'"
    }

    private fun showContent(results: List<LeaderboardResult>) {
        binding.emptyStateLayout.visibility = View.GONE
        binding.leaderboardRv.apply {
            if (visibility != View.VISIBLE) {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(300).start()
            }
        }
        leaderboardAdapter.submitList(results)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
