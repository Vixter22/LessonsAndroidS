package com.example.pr_pict_and_navigation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Якщо активність створюється вперше, додаємо фрагмент
        if (savedInstanceState == null) {
            replaceFragment(PhotoFragment()) // Відображаємо PhotoFragment
        }
    }

    // Функція для заміни фрагментів
    private fun replaceFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment) // Замінюємо контейнер
        transaction.commit() // Виконуємо транзакцію
    }
}
