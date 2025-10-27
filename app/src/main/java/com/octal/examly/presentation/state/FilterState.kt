package com.octal.examly.presentation.state

import com.octal.examly.domain.model.Subject
import com.octal.examly.domain.model.TestAttemptMode
import com.octal.examly.domain.model.User
import java.time.LocalDate

data class FilterState(
    val dateFrom: LocalDate? = null,

    val dateTo: LocalDate? = null,

    val subjects: Set<Subject> = emptySet(),

    val resultPassed: Boolean? = null,

    val modes: Set<TestAttemptMode> = emptySet(),

    val users: Set<User> = emptySet(),

    val isCompleted: Boolean? = null,

    val searchQuery: String? = null
) {
    fun hasActiveFilters(): Boolean {
        return dateFrom != null ||
                dateTo != null ||
                subjects.isNotEmpty() ||
                resultPassed != null ||
                modes.isNotEmpty() ||
                users.isNotEmpty() ||
                isCompleted != null ||
                !searchQuery.isNullOrBlank()
    }

    fun getActiveFilterCount(): Int {
        var count = 0
        if (dateFrom != null || dateTo != null) count++
        if (subjects.isNotEmpty()) count++
        if (resultPassed != null) count++
        if (modes.isNotEmpty()) count++
        if (users.isNotEmpty()) count++
        if (isCompleted != null) count++
        if (!searchQuery.isNullOrBlank()) count++
        return count
    }

    fun clear(): FilterState {
        return FilterState()
    }

    fun clearDateRange(): FilterState = copy(dateFrom = null, dateTo = null)
    fun clearSubjects(): FilterState = copy(subjects = emptySet())
    fun clearResult(): FilterState = copy(resultPassed = null)
    fun clearModes(): FilterState = copy(modes = emptySet())
    fun clearUsers(): FilterState = copy(users = emptySet())
    fun clearStatus(): FilterState = copy(isCompleted = null)
    fun clearSearch(): FilterState = copy(searchQuery = null)

    companion object {
        fun pending(): FilterState = FilterState(isCompleted = false)

        fun completed(): FilterState = FilterState(isCompleted = true)

        fun passed(): FilterState = FilterState(resultPassed = true)

        fun failed(): FilterState = FilterState(resultPassed = false)

        fun practiceOnly(): FilterState = FilterState(modes = setOf(TestAttemptMode.PRACTICE))

        fun examOnly(): FilterState = FilterState(modes = setOf(TestAttemptMode.EXAM))
    }
}
