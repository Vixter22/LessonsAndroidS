package com.example.firstprog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ThirdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        val fromActivity = intent.getStringExtra("FromActivity")
        Toast.makeText(this, "Came from $fromActivity", Toast.LENGTH_SHORT).show()

        findViewById<Button>(R.id.btnToMain).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnToSecond).setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            intent.putExtra("FromActivity", "Third Activity")
            startActivity(intent)
            finish()
        }
    }
}