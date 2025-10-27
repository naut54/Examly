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
import com.octal.examly.domain.model.Test

class TestListAdapter(
    private val onTestClick: (Test) -> Unit
) : ListAdapter<Test, TestListAdapter.TestViewHolder>(TestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test_card, parent, false)
        return TestViewHolder(view, onTestClick)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TestViewHolder(
        itemView: View,
        private val onTestClick: (Test) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val titleTextView: TextView = itemView.findViewById(R.id.tv_test_title)
        private val subjectTextView: TextView = itemView.findViewById(R.id.tv_subject)
        private val statusTextView: Chip = itemView.findViewById(R.id.chip_status)
        private val deadlineTextView: TextView = itemView.findViewById(R.id.tv_deadline)
        private val questionCountTextView: TextView = itemView.findViewById(R.id.tv_questions_count)

        fun bind(test: Test) {
            titleTextView.text = test.title
            subjectTextView.text = "Subject: ${test.subjectId}"
            statusTextView.text = test.mode.name.replace("_", " ")

            deadlineTextView.visibility = View.GONE

            val count = test.configuration.numberOfQuestions
            questionCountTextView.text = if (count != null) {
                "$count questions"
            } else {
                "Questions: —"
            }

            itemView.setOnClickListener {
                onTestClick(test)
            }
        }
    }

    private class TestDiffCallback : DiffUtil.ItemCallback<Test>() {
        override fun areItemsTheSame(oldItem: Test, newItem: Test): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Test, newItem: Test): Boolean {
            return oldItem == newItem
        }
    }
}
