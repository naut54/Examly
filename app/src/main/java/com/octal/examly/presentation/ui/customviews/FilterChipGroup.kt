package com.octal.examly.presentation.ui.customviews

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.octal.examly.R

class FilterChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ChipGroup(context, attrs, defStyleAttr) {

    private var onFilterChanged: ((Set<String>) -> Unit)? = null
    private val selectedFilters = mutableSetOf<String>()

    init {
        isSingleLine = false
        isSingleSelection = false
    }

    fun addFilterChip(label: String, key: String, isSelected: Boolean = false) {
        val chip = Chip(context).apply {
            text = label
            isCheckable = true
            isChecked = isSelected
            setChipBackgroundColorResource(R.color.surface_variant)
            setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    selectedFilters.add(key)
                } else {
                    selectedFilters.remove(key)
                }
                onFilterChanged?.invoke(selectedFilters.toSet())
            }
        }

        if (isSelected) {
            selectedFilters.add(key)
        }

        addView(chip)
    }

    fun addFilterChips(items: List<Pair<String, String>>) {
        items.forEach { (label, key) ->
            addFilterChip(label, key)
        }
    }

    fun clearChips() {
        removeAllViews()
        selectedFilters.clear()
    }

    fun getSelectedFilters(): Set<String> = selectedFilters.toSet()

    fun setOnFilterChangedListener(listener: (Set<String>) -> Unit) {
        onFilterChanged = listener
    }

    fun clearSelection() {
        for (i in 0 until childCount) {
            (getChildAt(i) as? Chip)?.isChecked = false
        }
        selectedFilters.clear()
    }
}
