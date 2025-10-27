package com.octal.examly.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.chip.Chip
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.octal.examly.R
import com.octal.examly.domain.model.Question

class QuestionListAdapter(
    private val onQuestionClick: (Question) -> Unit,
    private val onDeleteClick: (Question) -> Unit,
    private val onReorder: (List<Question>) -> Unit
) : ListAdapter<Question, QuestionListAdapter.QuestionViewHolder>(QuestionDiffCallback()) {

    private val questions = mutableListOf<Question>()

    override fun submitList(list: List<Question>?) {
        super.submitList(list)
        questions.clear()
        list?.let { questions.addAll(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view, onQuestionClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    fun moveQuestion(fromPosition: Int, toPosition: Int) {
        val movedQuestion = questions.removeAt(fromPosition)
        questions.add(toPosition, movedQuestion)
        notifyItemMoved(fromPosition, toPosition)
        onReorder(questions.toList())
    }

    class QuestionViewHolder(
        itemView: View,
        private val onQuestionClick: (Question) -> Unit,
        private val onDeleteClick: (Question) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val numberTextView: TextView = itemView.findViewById(R.id.tv_question_number)
        private val textTextView: TextView = itemView.findViewById(R.id.tv_question_text)
        private val typeChip: Chip = itemView.findViewById(R.id.chip_question_type)

        fun bind(question: Question, number: Int) {
            numberTextView.text = "$number."
            textTextView.text = question.questionText
            typeChip.text = question.type.name.replace("_", " ")

            itemView.setOnClickListener {
                onQuestionClick(question)
            }
        }
    }

    private class QuestionDiffCallback : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem == newItem
        }
    }

    class DragCallback(
        private val adapter: QuestionListAdapter
    ) : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            adapter.moveQuestion(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

        override fun isLongPressDragEnabled(): Boolean = true
    }
}
