package com.example.firstprog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val fromActivity = intent.getStringExtra("FromActivity")
        Toast.makeText(this, "Came from $fromActivity", Toast.LENGTH_SHORT).show()

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            returnToMain()
        }

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            goToThirdActivity()
        }
    }

    private fun returnToMain() {
        val resultIntent = Intent()
        resultIntent.putExtra("Value", "You returned from the Second Activity")
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun goToThirdActivity() {
        val intent = Intent(this, ThirdActivity::class.java)
        intent.putExtra("FromActivity", "Second Activity")
        startActivity(intent)
    }
}