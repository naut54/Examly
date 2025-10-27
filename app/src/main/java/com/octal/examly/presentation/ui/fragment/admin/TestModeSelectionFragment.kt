package com.octal.examly.presentation.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.octal.examly.R
import com.octal.examly.databinding.FragmentTestModeSelectionBinding
import com.octal.examly.domain.model.TestMode
import com.octal.examly.presentation.viewmodel.CreateTestViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestModeSelectionFragment : Fragment() {

    private var _binding: FragmentTestModeSelectionBinding? = null
    private val binding get() = _binding!!

    private val createTestViewModel: CreateTestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestModeSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        binding.cardFixedMode.setOnClickListener {
            selectMode(TestMode.FIXED)
        }

        binding.cardRandomMode.setOnClickListener {
            selectMode(TestMode.RANDOM)
        }
    }

    private fun selectMode(mode: TestMode) {
        createTestViewModel.setTestMode(mode)

        when (mode) {
            TestMode.FIXED -> {
                findNavController().navigate(
                    R.id.action_modeSelection_to_fixedSetup
                )
            }
            TestMode.RANDOM -> {
                findNavController().navigate(
                    R.id.action_modeSelection_to_randomSetup
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
