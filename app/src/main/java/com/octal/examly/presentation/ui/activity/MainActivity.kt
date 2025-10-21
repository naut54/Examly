package com.octal.examly.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.octal.examly.R
import com.octal.examly.domain.model.UserRole
import com.octal.examly.presentation.ui.fragment.admin.AdminHomeFragment
import com.octal.examly.presentation.ui.fragment.common.HomeFragment
import com.octal.examly.presentation.ui.fragment.common.ResultsFragment
import com.octal.examly.presentation.ui.fragment.common.SettingsFragment
import com.octal.examly.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var bottomNavigationView: BottomNavigationView
    private var currentUserRole: UserRole? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        initializeViews()
        observeCurrentUser()
        observeLogoutEvent()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
        }
    }

    private fun initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
    }

    private fun observeCurrentUser() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUser.collect { user ->
                    if (user == null) {
                        navigateToLogin()
                    } else {
                        currentUserRole = user.role
                        setupBottomNavigation(user.role)

                        // Cargar el fragment inicial si es la primera vez
                        if (savedInstanceState == null) {
                            loadInitialFragment(user.role)
                        }
                    }
                }
            }
        }
    }

    private fun setupBottomNavigation(userRole: UserRole) {
        // Configurar el menú según el rol
        bottomNavigationView.menu.clear()

        if (userRole.isAdmin()) {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_admin)
        } else {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_user)
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    supportActionBar?.title = "Mis Tests"
                    true
                }
                R.id.navigation_admin_home -> {
                    loadFragment(AdminHomeFragment())
                    supportActionBar?.title = "Administración"
                    true
                }
                R.id.navigation_results -> {
                    loadFragment(ResultsFragment())
                    supportActionBar?.title = "Resultados"
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    supportActionBar?.title = "Ajustes"
                    true
                }
                else -> false
            }
        }
    }

    private fun loadInitialFragment(userRole: UserRole) {
        val fragment = if (userRole.isAdmin()) {
            supportActionBar?.title = "Administración"
            AdminHomeFragment()
        } else {
            supportActionBar?.title = "Mis Tests"
            HomeFragment()
        }
        loadFragment(fragment)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observeLogoutEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutEvent.collect {
                    navigateToLogin()
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevenir que el usuario regrese con el botón atrás
        // Mostrar diálogo de confirmación para salir de la app
        AlertDialog.Builder(this)
            .setTitle("Salir")
            .setMessage("¿Deseas salir de la aplicación?")
            .setPositiveButton("Sí") { _, _ ->
                super.onBackPressed()
            }
            .setNegativeButton("No", null)
            .show()
    }
}