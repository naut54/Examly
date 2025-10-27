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
import com.octal.examly.domain.model.TestAssignment
import com.octal.examly.domain.model.TestAssignmentStatus

class TestAssignmentListAdapter(
    private val onTestClick: (TestAssignment) -> Unit,
    private val onResumeClick: ((TestAssignment) -> Unit)? = null
) : ListAdapter<TestAssignment, TestAssignmentListAdapter.AssignmentViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test_card, parent, false)
        return AssignmentViewHolder(view, onTestClick, onResumeClick)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AssignmentViewHolder(
        itemView: View,
        private val onTestClick: (TestAssignment) -> Unit,
        private val onResumeClick: ((TestAssignment) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val titleTextView: TextView = itemView.findViewById(R.id.tv_test_title)
        private val subjectTextView: TextView = itemView.findViewById(R.id.tv_subject)
        private val statusChip: Chip = itemView.findViewById(R.id.chip_status)
        private val deadlineTextView: TextView = itemView.findViewById(R.id.tv_deadline)
        private val questionCountTextView: TextView = itemView.findViewById(R.id.tv_questions_count)
        private val modeTextView: TextView? = itemView.findViewById(R.id.tv_mode)
        private val timeLimitTextView: TextView? = itemView.findViewById(R.id.tv_time_limit)

        fun bind(assignment: TestAssignment) {
            titleTextView.text = itemView.context.getString(R.string.test_placeholder_title, assignment.testId)
            subjectTextView.text = itemView.context.getString(R.string.subject_placeholder)

            statusChip.text = when (assignment.status) {
                TestAssignmentStatus.PENDING -> itemView.context.getString(R.string.pending)
                TestAssignmentStatus.IN_PROGRESS -> itemView.context.getString(R.string.in_progress)
                TestAssignmentStatus.COMPLETED -> itemView.context.getString(R.string.completed)
            }

            if (assignment.deadline != null) {
                deadlineTextView.visibility = View.VISIBLE
                deadlineTextView.text = itemView.context.getString(R.string.deadline_placeholder)
            } else {
                deadlineTextView.visibility = View.GONE
            }

            questionCountTextView.text = itemView.context.getString(R.string.questions_unknown)
            modeTextView?.text = itemView.context.getString(R.string.mode_unknown)
            timeLimitTextView?.text = itemView.context.getString(R.string.time_limit_unknown)

            itemView.setOnClickListener { onTestClick(assignment) }

            itemView.setOnLongClickListener {
                if (assignment.status == TestAssignmentStatus.IN_PROGRESS) {
                    onResumeClick?.invoke(assignment)
                    true
                } else {
                    false
                }
            }
        }
    }

    private class Diff : DiffUtil.ItemCallback<TestAssignment>() {
        override fun areItemsTheSame(oldItem: TestAssignment, newItem: TestAssignment): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TestAssignment, newItem: TestAssignment): Boolean =
            oldItem == newItem
    }
}
