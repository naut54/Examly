package com.octal.examly.presentation.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.octal.examly.R

class EditableAnswerOptionsAdapter(
    private val editable: Boolean = true,
    private val onAnswerTextChanged: (position: Int, text: String) -> Unit,
    private val onCorrectChanged: (position: Int, isCorrect: Boolean) -> Unit,
    private val onRemoveClick: (position: Int) -> Unit
) : ListAdapter<Pair<String, Boolean>, EditableAnswerOptionsAdapter.AnswerEditViewHolder>(AnswerPairDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerEditViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_answer_option_editable, parent, false)
        return AnswerEditViewHolder(view, editable, onAnswerTextChanged, onCorrectChanged, onRemoveClick)
    }

    override fun onBindViewHolder(holder: AnswerEditViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class AnswerEditViewHolder(
        itemView: View,
        private val editable: Boolean,
        private val onAnswerTextChanged: (position: Int, text: String) -> Unit,
        private val onCorrectChanged: (position: Int, isCorrect: Boolean) -> Unit,
        private val onRemoveClick: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val etAnswer: TextInputEditText = itemView.findViewById(R.id.etAnswerText)
        private val cbCorrect: CheckBox = itemView.findViewById(R.id.cbCorrect)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)

        private var currentWatcher: TextWatcher? = null

        fun bind(item: Pair<String, Boolean>, position: Int) {
            etAnswer.isEnabled = editable
            cbCorrect.isEnabled = editable
            btnRemove.visibility = if (editable) View.VISIBLE else View.GONE

            currentWatcher?.let { etAnswer.removeTextChangedListener(it) }

            etAnswer.setText(item.first)
            cbCorrect.setOnCheckedChangeListener(null)
            cbCorrect.isChecked = item.second

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    onAnswerTextChanged(bindingAdapterPosition, s?.toString().orEmpty())
                }
            }
            etAnswer.addTextChangedListener(watcher)
            currentWatcher = watcher

            cbCorrect.setOnCheckedChangeListener { _, isChecked ->
                onCorrectChanged(bindingAdapterPosition, isChecked)
            }

            btnRemove.setOnClickListener {
                onRemoveClick(bindingAdapterPosition)
            }
        }
    }

    private class AnswerPairDiff : DiffUtil.ItemCallback<Pair<String, Boolean>>() {
        override fun areItemsTheSame(oldItem: Pair<String, Boolean>, newItem: Pair<String, Boolean>): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Pair<String, Boolean>, newItem: Pair<String, Boolean>): Boolean {
            return oldItem == newItem
        }
    }
}