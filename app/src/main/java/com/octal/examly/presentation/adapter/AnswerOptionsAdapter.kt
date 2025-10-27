package com.octal.examly.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.octal.examly.R
import com.octal.examly.domain.model.Answer
import com.octal.examly.domain.model.QuestionType

class AnswerOptionsAdapter(
    private val questionType: QuestionType,
    private val onSelectionChanged: (Set<Long>) -> Unit
) : ListAdapter<Answer, AnswerOptionsAdapter.AnswerViewHolder>(AnswerDiffCallback()) {

    private val selectedAnswerIds = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_answer_option, parent, false)
        return AnswerViewHolder(view, questionType) { answer, isSelected ->
            handleSelection(answer.id, isSelected)
        }
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        val answer = getItem(position)
        holder.bind(answer, selectedAnswerIds.contains(answer.id))
    }

    private fun handleSelection(answerId: Long, isSelected: Boolean) {
        when (questionType) {
            QuestionType.SINGLE_CHOICE -> {
                selectedAnswerIds.clear()
                if (isSelected) {
                    selectedAnswerIds.add(answerId)
                }
                notifyDataSetChanged()
            }
            QuestionType.MULTIPLE_CHOICE -> {
                if (isSelected) {
                    selectedAnswerIds.add(answerId)
                } else {
                    selectedAnswerIds.remove(answerId)
                }
            }
        }
        onSelectionChanged(selectedAnswerIds.toSet())
    }

    fun setSelectedAnswers(answerIds: Set<Long>) {
        selectedAnswerIds.clear()
        selectedAnswerIds.addAll(answerIds)
        notifyDataSetChanged()
    }

    fun getSelectedAnswerIds(): Set<Long> = selectedAnswerIds.toSet()

    fun clearSelections() {
        selectedAnswerIds.clear()
        notifyDataSetChanged()
        onSelectionChanged(emptySet())
    }

    class AnswerViewHolder(
        itemView: View,
        private val questionType: QuestionType,
        private val onSelectionChanged: (Answer, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val answerTextView: TextView = itemView.findViewById(R.id.tv_answer_text)
        private val radioButton: RadioButton = itemView.findViewById(R.id.rb_answer)
        private val checkBox: CheckBox = itemView.findViewById(R.id.cb_answer)

        fun bind(answer: Answer, isSelected: Boolean) {
            answerTextView.text = answer.answerText

            when (questionType) {
                QuestionType.SINGLE_CHOICE -> {
                    radioButton.visibility = View.VISIBLE
                    checkBox.visibility = View.GONE

                    radioButton.setOnCheckedChangeListener(null)
                    radioButton.isChecked = isSelected
                    radioButton.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            onSelectionChanged(answer, true)
                        }
                    }
                }
                QuestionType.MULTIPLE_CHOICE -> {
                    radioButton.visibility = View.GONE
                    checkBox.visibility = View.VISIBLE

                    checkBox.setOnCheckedChangeListener(null)
                    checkBox.isChecked = isSelected
                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        onSelectionChanged(answer, isChecked)
                    }
                }
            }

            itemView.setOnClickListener {
                when (questionType) {
                    QuestionType.SINGLE_CHOICE -> {
                        if (!radioButton.isChecked) {
                            radioButton.isChecked = true
                        }
                    }
                    QuestionType.MULTIPLE_CHOICE -> {
                        checkBox.isChecked = !checkBox.isChecked
                    }
                }
            }

            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.bg_selected_answer)
            } else {
                itemView.setBackgroundResource(R.drawable.bg_card)
            }
        }
    }

    private class AnswerDiffCallback : DiffUtil.ItemCallback<Answer>() {
        override fun areItemsTheSame(oldItem: Answer, newItem: Answer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Answer, newItem: Answer): Boolean {
            return oldItem == newItem
        }
    }
}
