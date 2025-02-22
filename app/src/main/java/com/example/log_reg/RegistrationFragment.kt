package com.example.log_reg

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class RegistrationFragment : Fragment() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextBirthDate: EditText
    private lateinit var editTextAbout: EditText
    private lateinit var buttonRegister: Button
    private lateinit var buttonBack: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        editTextName = view.findViewById(R.id.editTextName)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextBirthDate = view.findViewById(R.id.editTextBirthDate)
        editTextAbout = view.findViewById(R.id.editTextAbout)
        buttonRegister = view.findViewById(R.id.buttonRegister)
        buttonBack = view.findViewById(R.id.buttonBack)

        buttonRegister.setOnClickListener { registerUser() }
        buttonBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
    }

    private fun registerUser() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("Users", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val name = editTextName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val birthDate = editTextBirthDate.text.toString().trim()
        val about = editTextAbout.text.toString().trim()

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() ||
            email.isEmpty() || birthDate.isEmpty() || about.isEmpty()) {
            Toast.makeText(activity, "Заповніть усі поля!", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(activity, "Невірний формат email!", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidBirthDate(birthDate)) {
            Toast.makeText(activity, "Дата народження має бути у форматі дд-мм-рррр", Toast.LENGTH_SHORT).show()
            return
        }

        // Перевіряємо, чи користувач уже існує
        if (sharedPreferences.contains(username)) {
            Toast.makeText(activity, "Цей логін уже використовується!", Toast.LENGTH_SHORT).show()
            return
        }

        // Зберігаємо дані користувача правильно
        editor.putString(username, password) // Зберігаємо пароль за ключем "username"
        editor.putString("$username-name", name)
        editor.putString("$username-email", email)
        editor.putString("$username-birthDate", birthDate)
        editor.putString("$username-about", about)
        editor.apply()

        Toast.makeText(activity, "Реєстрація успішна!", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidBirthDate(birthDate: String): Boolean {
        val regex = Regex("""^(\d{2})-(\d{2})-(\d{4})$""")
        val matchResult = regex.find(birthDate) ?: return false

        val (day, month, year) = matchResult.destructured
        val dayInt = day.toInt()
        val monthInt = month.toInt()
        val yearInt = year.toInt()

        if (dayInt !in 1..31 || monthInt !in 1..12 || yearInt !in 1900..2024) {
            return false
        }
        return true
    }

    private fun navigateToLogin() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .addToBackStack(null)
            .commit()
    }
}
