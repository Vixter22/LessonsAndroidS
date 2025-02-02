package com.example.firstprog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ThirdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_third)

        val textView = findViewById<TextView>(R.id.textViewThird)
        textView.text = "This is Third Activity"

        // Отримання джерела переходу
        val source = intent.getStringExtra("from")
        if (!source.isNullOrEmpty()) {
            Toast.makeText(this, "Я прийшов з $source", Toast.LENGTH_SHORT).show()
        }

        // Обробка кнопки повернення в MainActivity
        findViewById<Button>(R.id.buttonBackToMain).setOnClickListener {
            goBackToMainActivity()
        }

        // Обробка кнопки повернення в SecondActivity
        findViewById<Button>(R.id.buttonBackToSecond).setOnClickListener {
            goBackToSecondActivity()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun goBackToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("from", "ThirdActivity") // Передаємо джерело
        startActivity(intent)
    }

    private fun goBackToSecondActivity() {
        val intent = Intent(this, SecondActivity::class.java)
        intent.putExtra("from", "ThirdActivity") // Передаємо джерело
        startActivity(intent)
    }
}
