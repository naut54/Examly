package com.octal.examly.presentation.ui.fragment.common

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.octal.examly.R
import com.octal.examly.databinding.FragmentSettingsBinding
import com.octal.examly.presentation.ui.activity.LoginActivity
import com.octal.examly.presentation.viewmodel.MainViewModel
import com.octal.examly.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeCurrentUser()
        observeSettings()
    }

    private fun setupUI() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setDarkMode(isChecked)
            applyDarkMode(isChecked)
        }

        binding.switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setNotificationsEnabled(isChecked)
            Toast.makeText(
                requireContext(),
                if (isChecked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.cardAbout?.setOnClickListener {
            showAboutDialog()
        }

        try {
            val packageInfo = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0)
            binding.tvVersionInfo?.text = getString(
                R.string.version,
                packageInfo.versionName
            )
        } catch (e: Exception) {
            binding.tvVersionInfo?.text = getString(R.string.version, "1.0.0")
        }
    }

    private fun observeCurrentUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.currentUser.collect { user ->
                    user?.let {
                        binding.tvUsername.text = it.username
                        binding.tvUserRole.text = when (it.role) {
                            com.octal.examly.domain.model.UserRole.USER ->
                                getString(R.string.user)
                            com.octal.examly.domain.model.UserRole.ADMIN ->
                                getString(R.string.admin)
                        }

                        val roleIcon = when (it.role) {
                            com.octal.examly.domain.model.UserRole.USER -> R.drawable.ic_user
                            com.octal.examly.domain.model.UserRole.ADMIN -> R.drawable.ic_admin
                        }
                        binding.ivUserIcon?.setImageResource(roleIcon)
                    }
                }
            }
        }
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.isDarkModeEnabled.collect { isDarkMode ->
                    binding.switchDarkMode.isChecked = isDarkMode
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.isNotificationsEnabled.collect { isEnabled ->
                    binding.switchNotifications?.isChecked = isEnabled
                }
            }
        }
    }

    private fun applyDarkMode(isDarkMode: Boolean) {
        val nightMode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.confirm_logout)
            .setIcon(R.drawable.ic_logout)
            .setPositiveButton(R.string.logout) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar?.visibility = View.VISIBLE
            binding.btnLogout.isEnabled = false

            mainViewModel.logout()

            binding.progressBar?.visibility = View.GONE

            Toast.makeText(
                requireContext(),
                getString(R.string.success_logout),
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.about)
            .setMessage("""
                Examly - Exam Management App

                A comprehensive exam management system for creating,
                assigning, and taking tests.

                Features:
                • Create custom tests
                • Assign tests to users
                • Practice and exam modes
                • Detailed results and metrics
                • User and admin roles

                Developed with Clean Architecture and Material Design 3
            """.trimIndent())
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun clearCache() {
        try {
            requireContext().cacheDir.deleteRecursively()
            Toast.makeText(
                requireContext(),
                "Cache cleared successfully",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Failed to clear cache",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
