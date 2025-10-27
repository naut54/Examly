package com.octal.examly.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.octal.examly.R
import com.octal.examly.domain.model.SubjectStats

class MetricsAdapter : ListAdapter<SubjectStats, MetricsAdapter.MetricsViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_metric_row, parent, false)
        return MetricsViewHolder(view)
    }

    override fun onBindViewHolder(holder: MetricsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MetricsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_title)
        private val subtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        fun bind(item: SubjectStats) {
            title.text = item.subjectName
            val pct = (item.averageScore * 100).toInt()
            subtitle.text = itemView.context.getString(R.string.metric_subject_subtitle, item.testsCount, pct)
        }
    }

    private class Diff : DiffUtil.ItemCallback<SubjectStats>() {
        override fun areItemsTheSame(oldItem: SubjectStats, newItem: SubjectStats): Boolean = oldItem.subjectId == newItem.subjectId
        override fun areContentsTheSame(oldItem: SubjectStats, newItem: SubjectStats): Boolean = oldItem == newItem
    }
}