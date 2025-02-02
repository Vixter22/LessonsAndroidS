package com.example.firstprog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val textView = findViewById<TextView>(R.id.textView3)
        textView.text = "This is Second Activity"

        // Отримання даних з Intent
        val source = intent.getStringExtra("from")
        if (!source.isNullOrEmpty()) {
            Toast.makeText(this, "Я прийшов з $source", Toast.LENGTH_SHORT).show()
        }

        // Обробка натискання кнопки для переходу в ThirdActivity
        findViewById<Button>(R.id.buttonToThird).setOnClickListener {
            goToThirdActivity()
        }

        // Обробка натискання кнопки для повернення в MainActivity
        findViewById<Button>(R.id.buttonBackToMain).setOnClickListener {
            goBackToMainActivity()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun goToThirdActivity() {
        val intent = Intent(this, ThirdActivity::class.java)
        intent.putExtra("from", "SecondActivity") // Передаємо джерело переходу
        startActivity(intent)
    }

    private fun goBackToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("from", "SecondActivity") // Передаємо джерело
        startActivity(intent)
    }
}
