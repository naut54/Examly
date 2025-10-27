package com.octal.examly.presentation.ui.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.octal.examly.databinding.ViewUserSelectorBinding
import com.octal.examly.domain.model.User
import com.octal.examly.presentation.adapter.UserListAdapter

class UserSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewUserSelectorBinding
    private lateinit var adapter: UserListAdapter
    private var onSelectionChanged: ((List<User>) -> Unit)? = null
    private val selectedUsers = mutableListOf<User>()

    init {
        binding = ViewUserSelectorBinding.inflate(LayoutInflater.from(context), this, true)
        orientation = VERTICAL
        setupRecyclerView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        adapter = UserListAdapter { selected ->
            selectedUsers.clear()
            selectedUsers.addAll(selected)
            updateSelectedCount()
            onSelectionChanged?.invoke(selectedUsers.toList())
        }

        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@UserSelectorView.adapter
        }
    }

    private fun setupButtons() {
        binding.btnSelectAll.setOnClickListener {
            adapter.selectAll()
            selectedUsers.clear()
            selectedUsers.addAll(adapter.getSelectedUsers())
            updateSelectedCount()
            onSelectionChanged?.invoke(selectedUsers.toList())
        }

        binding.btnDeselectAll.setOnClickListener {
            adapter.clearSelections()
            selectedUsers.clear()
            updateSelectedCount()
            onSelectionChanged?.invoke(selectedUsers.toList())
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterUsers(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText)
                return true
            }
        })
    }

    fun setUsers(users: List<User>) {
        adapter.submitList(users)
    }

    fun getSelectedUsers(): List<User> = selectedUsers.toList()

    fun setOnSelectionChangedListener(listener: (List<User>) -> Unit) {
        onSelectionChanged = listener
    }

    private fun updateSelectedCount() {
        binding.tvSelectedCount.text = "Selected: ${selectedUsers.size}"
    }

    private fun filterUsers(query: String?) {
        adapter.filter(query)
    }
}
