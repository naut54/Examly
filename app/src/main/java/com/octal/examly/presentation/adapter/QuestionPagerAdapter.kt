package com.octal.examly.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.chip.Chip
import com.octal.examly.R
import com.octal.examly.domain.model.Question
import com.octal.examly.domain.model.QuestionType

class QuestionPagerAdapter(
    @Suppress("unused") private val fragment: Fragment,
    private val onAnswerSelected: (questionId: Long, selectedAnswerIds: Set<Long>) -> Unit
) : RecyclerView.Adapter<QuestionPagerAdapter.QuestionPageViewHolder>() {

    private val questions = mutableListOf<Question>()

    fun submitList(list: List<Question>) {
        questions.clear()
        questions.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionPageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return QuestionPageViewHolder(view, onAnswerSelected)
    }

    override fun onBindViewHolder(holder: QuestionPageViewHolder, position: Int) {
        val number = position + 1
        holder.bind(questions[position], number)
    }

    override fun getItemCount(): Int = questions.size

    class QuestionPageViewHolder(
        itemView: View,
        private val onAnswerSelected: (questionId: Long, selectedAnswerIds: Set<Long>) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvNumber: TextView = itemView.findViewById(R.id.tv_question_number)
        private val tvText: TextView = itemView.findViewById(R.id.tv_question_text)
        private val ivImage: ImageView? = itemView.findViewById(R.id.iv_question_image)
        private val chipType: Chip? = itemView.findViewById(R.id.chip_question_type)
        private val rvAnswers: RecyclerView = itemView.findViewById(R.id.rv_answers)

        fun bind(question: Question, number: Int) {
            tvNumber.text = itemView.context.getString(R.string.question_number, number)
            tvText.text = question.questionText

            ivImage?.let { image ->
                if (question.imageUri != null) {
                    image.visibility = View.VISIBLE
                    image.load(question.imageUri) {
                        crossfade(true)
                        placeholder(R.drawable.ic_question)
                        error(R.drawable.ic_warning)
                    }
                } else {
                    image.visibility = View.GONE
                }
            }

            chipType?.text = when (question.type) {
                QuestionType.SINGLE_CHOICE -> itemView.context.getString(R.string.single_choice)
                QuestionType.MULTIPLE_CHOICE -> itemView.context.getString(R.string.multiple_choice)
            }

            rvAnswers.layoutManager = LinearLayoutManager(itemView.context)
            val answersAdapter = AnswerOptionsAdapter(question.type) { selectedIds ->
                onAnswerSelected(question.id, selectedIds)
            }
            rvAnswers.adapter = answersAdapter
            answersAdapter.submitList(question.answers)
        }
    }
}
