package com.example.kreedapreranascout.ui.talent

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.kreedapreranascout.R
import com.example.kreedapreranascout.data.local.AppDatabase
import com.example.kreedapreranascout.data.model.Performance
import com.example.kreedapreranascout.data.repository.StudentRepository
import com.example.kreedapreranascout.databinding.FragmentTalentCurveBinding
import com.example.kreedapreranascout.ui.student.StudentViewModel
import com.example.kreedapreranascout.util.SportsEventUtils
import com.example.kreedapreranascout.util.ViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Locale

class TalentCurveFragment : Fragment(R.layout.fragment_talent_curve) {
    private var _binding: FragmentTalentCurveBinding? = null
    private val binding get() = _binding!!
    
    private val args: TalentCurveFragmentArgs by navArgs()
    private val viewModel: StudentViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ViewModelFactory(StudentRepository(db.studentDao(), db.performanceDao(), db.attendanceDao(), db.achievementDao()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTalentCurveBinding.bind(view)

        setupChart()
        setupEventSpinner()

        binding.eventSpinner.setOnItemClickListener { _, _, _, _ ->
            loadChartData()
        }
    }

    private fun setupEventSpinner() {
        val events = arrayOf("100m Sprint", "200m Sprint", "Long Jump", "High Jump", "Shot Put")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, events)
        binding.eventSpinner.setAdapter(adapter)
        binding.eventSpinner.setText(events[0], false)
        loadChartData()
    }

    private fun loadChartData() {
        val selectedEvent = binding.eventSpinner.text.toString()
        viewModel.getLastFivePerformances(args.studentId, selectedEvent).observe(viewLifecycleOwner) { performances ->
            if (!performances.isNullOrEmpty() && performances.size >= 2) {
                updateGraph(performances.reversed()) // Chronological order
                binding.emptyStateLayout.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
            } else {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.contentLayout.visibility = View.GONE
            }
        }
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            setDragEnabled(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setDrawBorders(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.parseColor("#9E9E9E")
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = "Trial ${value.toInt() + 1}"
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#33FFFFFF")
                textColor = Color.parseColor("#9E9E9E")
                setDrawAxisLine(false)
            }

            axisRight.isEnabled = false
            legend.apply {
                isEnabled = true
                textColor = Color.parseColor("#9E9E9E")
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
            }
            setExtraOffsets(10f, 10f, 10f, 10f)
        }
    }

    private fun updateGraph(filteredList: List<Performance>) {
        val selectedEvent = binding.eventSpinner.text.toString()
        val entries = filteredList.mapIndexed { index, perf ->
            Entry(index.toFloat(), perf.value.toFloat())
        }

        val lineDataSet = LineDataSet(entries, selectedEvent).apply {
            color = Color.parseColor("#2196F3")
            setCircleColor(Color.parseColor("#2196F3"))
            lineWidth = 4f
            circleRadius = 6f
            setDrawCircleHole(true)
            circleHoleColor = Color.parseColor("#121212")
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#2196F3")
            fillAlpha = 30
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = String.format(Locale.US, "%.2f", value)
            }
        }

        binding.lineChart.apply {
            // Adjust axis if lower is better to make graph intuitive (optional, but standard is higher is up)
            // For now keep standard, but let's ensure labels match requirements
            data = LineData(lineDataSet)
            animateY(1000)
            invalidate()
        }

        updateSummaryCards(filteredList)
    }

    private fun updateSummaryCards(performances: List<Performance>) {
        val selectedEvent = binding.eventSpinner.text.toString()
        val latest = performances.last()
        val first = performances.first()
        
        binding.latestValueTv.text = SportsEventUtils.formatValue(latest.value, selectedEvent)

        val percentage = SportsEventUtils.getImprovementPercentage(first.value, latest.value, selectedEvent)
        val improved = if (SportsEventUtils.isLowerBetter(selectedEvent)) latest.value < first.value else latest.value > first.value

        val green = Color.parseColor("#4CAF50")
        val red = Color.parseColor("#F44336")
        val neutral = Color.parseColor("#9E9E9E")

        binding.improvementTv.text = String.format(Locale.US, "%s%.1f%%", if (percentage >= 0) "+" else "", percentage)
        binding.improvementTv.setTextColor(if (improved) green else if (percentage == 0.0) neutral else red)

        val statusText = SportsEventUtils.getTrendStatus(performances, selectedEvent)
        val diffValue = Math.abs(latest.value - first.value)
        val unit = SportsEventUtils.getUnit(selectedEvent)
        val trendDetail = "Overall ${if (improved) "improvement" else "change"} of ${String.format(Locale.US, "%.2f", diffValue)} $unit (${String.format(Locale.US, "%.1f%%", Math.abs(percentage))}) since Trial 1."
        
        binding.summaryTv.text = "$statusText. $trendDetail"
        binding.summaryTv.setTextColor(if (improved) Color.WHITE else Color.parseColor("#FFCDD2"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
