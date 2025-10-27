package com.octal.examly.presentation.ui.dialog

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.octal.examly.databinding.DialogFilterBinding
import com.octal.examly.domain.model.Subject
import com.octal.examly.domain.model.TestAttemptStatus
import com.octal.examly.domain.model.TestMode
import com.octal.examly.presentation.state.FilterState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FilterDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!

    private var onFilterApplied: ((FilterState) -> Unit)? = null
    private var currentFilter: FilterState = FilterState()
    private var availableSubjects: List<Subject> = emptyList()

    private var dateFrom: LocalDate? = null
    private var dateTo: LocalDate? = null
    private val selectedSubjects = mutableSetOf<Subject>()
    private var selectedStatus: TestAttemptStatus? = null
    private var selectedResult: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreFilterState()

        setupUI()
        setupSubjectChips()
        setupStatusChips()
        setupResultChips()
    }

    private fun setupUI() {
        binding.etFromDate.setOnClickListener { showDatePicker(isStartDate = true) }
        binding.etToDate.setOnClickListener { showDatePicker(isStartDate = false) }

        binding.btnApply.setOnClickListener { applyFilters() }

        binding.btnClearFilters.setOnClickListener { clearAllFilters() }
    }

    private fun restoreFilterState() {
        dateFrom = currentFilter.dateFrom
        dateTo = currentFilter.dateTo
        selectedSubjects.clear()
        selectedSubjects.addAll(currentFilter.subjects)
        selectedStatus = when (currentFilter.isCompleted) {
            true -> TestAttemptStatus.COMPLETED
            false -> TestAttemptStatus.PENDING
            else -> null
        }
        selectedResult = currentFilter.resultPassed

        updateDateDisplay()
    }

    private fun setupSubjectChips() {
        binding.chipGroupSubjects.removeAllViews()

        availableSubjects.forEach { subject ->
            val chip = Chip(requireContext()).apply {
                text = subject.name
                isCheckable = true
                isChecked = selectedSubjects.contains(subject)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedSubjects.add(subject)
                    } else {
                        selectedSubjects.remove(subject)
                    }
                }
            }
            binding.chipGroupSubjects.addView(chip)
        }
    }

    private fun setupStatusChips() {
        binding.chipAll.isChecked = selectedStatus == null
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedStatus = null
            }
        }

        binding.chipPending.isChecked = selectedStatus == TestAttemptStatus.PENDING
        binding.chipPending.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedStatus = TestAttemptStatus.PENDING
            }
        }

        binding.chipCompleted.isChecked = selectedStatus == TestAttemptStatus.COMPLETED
        binding.chipCompleted.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedStatus = TestAttemptStatus.COMPLETED
            }
        }
    }


    private fun setupResultChips() {
        binding.chipAllResults.isChecked = selectedResult == null
        binding.chipAllResults.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedResult = null
            }
        }

        binding.chipPassed.isChecked = selectedResult == true
        binding.chipPassed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedResult = true
            }
        }

        binding.chipFailed.isChecked = selectedResult == false
        binding.chipFailed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedResult = false
            }
        }
    }



    private fun showDatePicker(isStartDate: Boolean) {
        val now = LocalDate.now()
        val initial = if (isStartDate) dateFrom else dateTo
        val year = initial?.year ?: now.year
        val monthIndex = (initial?.monthValue ?: now.monthValue) - 1
        val day = initial?.dayOfMonth ?: now.dayOfMonth

        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val picked = LocalDate.of(y, m + 1, d)
                if (isStartDate) {
                    dateFrom = picked
                } else {
                    dateTo = picked
                }
                updateDateDisplay()
            },
            year,
            monthIndex,
            day
        ).show()
    }

    private fun updateDateDisplay() {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        binding.etFromDate.setText(dateFrom?.format(formatter) ?: "")
        binding.etToDate.setText(dateTo?.format(formatter) ?: "")
    }


    private fun applyFilters() {
        val isCompleted = when (selectedStatus) {
            TestAttemptStatus.COMPLETED -> true
            TestAttemptStatus.PENDING -> false
            else -> null
        }

        val filterState = FilterState(
            dateFrom = dateFrom,
            dateTo = dateTo,
            subjects = selectedSubjects.toSet(),
            resultPassed = selectedResult,
            modes = emptySet(),
            users = emptySet(),
            isCompleted = isCompleted,
            searchQuery = null
        )

        onFilterApplied?.invoke(filterState)
        dismiss()
    }

    private fun clearAllFilters() {
        selectedSubjects.clear()
        selectedStatus = null
        selectedResult = null
        dateFrom = null
        dateTo = null

        setupSubjectChips()
        setupStatusChips()
        setupResultChips()
        updateDateDisplay()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            currentFilter: FilterState = FilterState(),
            availableSubjects: List<Subject> = emptyList(),
            onApply: (FilterState) -> Unit
        ): FilterDialogFragment {
            return FilterDialogFragment().apply {
                this.onFilterApplied = onApply
                this.currentFilter = currentFilter
                this.availableSubjects = availableSubjects
            }
        }
    }
}
