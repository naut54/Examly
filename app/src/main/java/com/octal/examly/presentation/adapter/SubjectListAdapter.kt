package com.octal.examly.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.octal.examly.R
import com.octal.examly.domain.model.Subject

class SubjectListAdapter(
    private val onSubjectClick: (Subject) -> Unit,
    private val onDeleteClick: ((Subject) -> Unit)? = null
) : ListAdapter<Subject, SubjectListAdapter.SubjectViewHolder>(SubjectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view, onSubjectClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SubjectViewHolder(
        itemView: View,
        private val onSubjectClick: (Subject) -> Unit,
        private val onDeleteClick: ((Subject) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.tv_subject_name)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tv_description)

        fun bind(subject: Subject) {
            nameTextView.text = subject.name

            if (subject.description.isNotBlank()) {
                descriptionTextView.text = subject.description
                descriptionTextView.visibility = View.VISIBLE
            } else {
                descriptionTextView.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onSubjectClick(subject)
            }

        }
    }

    private class SubjectDiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean {
            return oldItem == newItem
        }
    }
}
