package com.octal.examly.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.octal.examly.R
import com.octal.examly.domain.model.User

class UserListAdapter(
    private val onSelectionChanged: (Set<User>) -> Unit
) : ListAdapter<User, UserListAdapter.UserViewHolder>(UserDiffCallback()) {

    private val selectedUsers = mutableSetOf<User>()
    private var allUsers = listOf<User>()
    private var filteredUsers = listOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view) { user, isChecked ->
            if (isChecked) {
                selectedUsers.add(user)
            } else {
                selectedUsers.remove(user)
            }
            onSelectionChanged(selectedUsers.toSet())
        }
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user, selectedUsers.contains(user))
    }

    fun getSelectedUsers(): Set<User> = selectedUsers.toSet()

    fun clearSelections() {
        selectedUsers.clear()
        notifyDataSetChanged()
        onSelectionChanged(emptySet())
    }

    fun selectAll() {
        selectedUsers.clear()
        selectedUsers.addAll(currentList)
        notifyDataSetChanged()
        onSelectionChanged(selectedUsers.toSet())
    }

    override fun submitList(list: List<User>?) {
        allUsers = list ?: emptyList()
        filteredUsers = allUsers
        super.submitList(filteredUsers)
    }

    fun filter(query: String?) {
        filteredUsers = if (query.isNullOrBlank()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.username.contains(query, ignoreCase = true)
            }
        }
        super.submitList(filteredUsers)
    }

    class UserViewHolder(
        itemView: View,
        private val onCheckChanged: (User, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val usernameTextView: TextView = itemView.findViewById(R.id.tv_username)
        private val roleTextView: TextView = itemView.findViewById(R.id.chip_role)
        private val checkBox: CheckBox = itemView.findViewById(R.id.cb_select_user)

        fun bind(user: User, isSelected: Boolean) {
            usernameTextView.text = user.username
            roleTextView.text = user.role.name

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = isSelected

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onCheckChanged(user, isChecked)
            }

            itemView.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
        }
    }

    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
