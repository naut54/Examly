package com.octal.examly.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.octal.examly.R
import com.octal.examly.domain.model.TestResult
import com.octal.examly.utils.DateTimeFormatter
import kotlin.math.roundToInt

class ResultsAdapter(
    private val onResultClick: (TestResult) -> Unit
) : ListAdapter<TestResult, ResultsAdapter.ResultViewHolder>(ResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result_card, parent, false)
        return ResultViewHolder(view, onResultClick)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ResultViewHolder(
        itemView: View,
        private val onResultClick: (TestResult) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val testTitleTextView: TextView = itemView.findViewById(R.id.tv_test_title)
        private val scoreTextView: TextView = itemView.findViewById(R.id.tv_score)
        private val dateTextView: TextView = itemView.findViewById(R.id.tv_date)
        private val resultChip: Chip = itemView.findViewById(R.id.chip_result)
        private val modeChip: Chip = itemView.findViewById(R.id.chip_mode)
        private val correctCountTextView: TextView = itemView.findViewById(R.id.tv_correct_count)
        private val wrongCountTextView: TextView = itemView.findViewById(R.id.tv_wrong_count)
        private val timeSpentTextView: TextView = itemView.findViewById(R.id.tv_time_spent)
        private val dateTimeFormatter = DateTimeFormatter()

        fun bind(result: TestResult) {
            testTitleTextView.text = "Test #${result.testId}"

            scoreTextView.text = result.score.roundToInt().toString()

            dateTextView.text = dateTimeFormatter.formatDate(result.completedAt)

            val passed = result.isPassed || result.calculateIsPassed()
            resultChip.text = if (passed) "PASSED" else "FAILED"

            modeChip.text = result.mode.name

            correctCountTextView.text = result.correctAnswers.toString()
            val wrong = result.wrongAnswers.takeIf { it >= 0 } ?: (result.totalQuestions - result.correctAnswers)
            wrongCountTextView.text = wrong.toString()
            timeSpentTextView.text = result.timeSpent?.let { dateTimeFormatter.formatDuration(it) } ?: "-"

            itemView.setOnClickListener {
                onResultClick(result)
            }
        }
    }

    private class ResultDiffCallback : DiffUtil.ItemCallback<TestResult>() {
        override fun areItemsTheSame(oldItem: TestResult, newItem: TestResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TestResult, newItem: TestResult): Boolean {
            return oldItem == newItem
        }
    }
}
