package com.octal.examly.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.octal.examly.R
import com.octal.examly.domain.model.UserAnswer

class ResultDetailAdapter : ListAdapter<UserAnswer, ResultDetailAdapter.ResultViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result_detail_min, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuestionNumber: TextView = itemView.findViewById(R.id.tvQuestionNumber)
        private val tvAnswerSummary: TextView = itemView.findViewById(R.id.tvAnswerSummary)
        private val ivStatus: ImageView = itemView.findViewById(R.id.ivStatus)

        fun bind(userAnswer: UserAnswer, number: Int) {
            tvQuestionNumber.text = itemView.context.getString(R.string.question_number, number)
            val isCorrect = userAnswer.isCorrect
            tvAnswerSummary.text = if (isCorrect) itemView.context.getString(R.string.correct) else itemView.context.getString(R.string.wrong)
            ivStatus.setImageResource(if (isCorrect) R.drawable.ic_check else R.drawable.ic_close)
            ivStatus.imageTintList = android.content.res.ColorStateList.valueOf(
                itemView.context.getColor(if (isCorrect) R.color.success else R.color.error)
            )
        }
    }

    private class Diff : DiffUtil.ItemCallback<UserAnswer>() {
        override fun areItemsTheSame(oldItem: UserAnswer, newItem: UserAnswer): Boolean =
            (oldItem.question?.id ?: -1L) == (newItem.question?.id ?: -2L)

        override fun areContentsTheSame(oldItem: UserAnswer, newItem: UserAnswer): Boolean =
            oldItem == newItem
    }
}