package com.example.firstprog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private var flag = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        flag = sharedPreferences.getBoolean("isFlagOn", true)

        val textView: TextView = findViewById(R.id.textView)
        textView.text = if (flag) getString(R.string.on) else getString(R.string.off)

        findViewById<Button>(R.id.second).setOnClickListener {
            goToSecondActivity()
        }

        findViewById<Button>(R.id.third).setOnClickListener {
            goToThirdActivity()
        }

        // Отримання повідомлення про попередню активність
        val from = intent.getStringExtra("from")
        if (!from.isNullOrEmpty()) {
            Toast.makeText(this, "Я прийшов з $from", Toast.LENGTH_SHORT).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun goToSecondActivity() {
        val intent = Intent(this, SecondActivity::class.java)
        intent.putExtra("from", "MainActivity") // Передаємо джерело переходу
        startActivity(intent)
    }

    private fun goToThirdActivity() {
        val intent = Intent(this, ThirdActivity::class.java)
        intent.putExtra("from", "MainActivity") // Передаємо джерело переходу
        startActivity(intent)
    }

    fun onClick(view: View) {
        val textView: TextView = findViewById(R.id.textView)

        flag = !flag

        textView.text = if (flag) getString(R.string.on) else getString(R.string.off)

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFlagOn", flag)
        editor.apply()
    }
}
