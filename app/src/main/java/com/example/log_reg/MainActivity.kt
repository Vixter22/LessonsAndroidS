package com.example.log_reg

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (savedInstanceState == null) {
            val startFragment: Fragment = if (isLoggedIn) {
                bottomNav.visibility = View.VISIBLE  // Показуємо меню після входу
                HomeFragment()
            } else {
                bottomNav.visibility = View.GONE  // Приховуємо меню на екрані логіну
                LoginFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, startFragment)
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_list -> ListFragment()
                R.id.nav_mystery -> MysteryFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()

            true
        }
    }

    fun loginSuccess() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", true)  // Зберігаємо статус логіну
            apply()
        }

        bottomNav.visibility = View.VISIBLE  // Показуємо меню після логіну

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    fun logoutUser() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", false)  // Вихід із системи
            apply()
        }

        bottomNav.visibility = View.GONE  // Приховуємо меню при виході

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }
}
