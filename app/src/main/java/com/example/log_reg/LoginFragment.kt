package com.example.log_reg

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class LoginFragment : Fragment() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        buttonLogin = view.findViewById(R.id.buttonLogin)
        buttonRegister = view.findViewById(R.id.buttonRegister)

        buttonLogin.setOnClickListener { loginUser() }
        buttonRegister.setOnClickListener { navigateToRegister() }
    }

    private fun loginUser() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("Users", Context.MODE_PRIVATE)

        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val storedPassword = sharedPreferences.getString(username, null)

        if (storedPassword != null && storedPassword == password) {
            Toast.makeText(activity, "Вхід успішний!", Toast.LENGTH_SHORT).show()
            saveLoginState(username)  // Передаємо ім'я користувача
            (activity as? MainActivity)?.loginSuccess()
        } else {
            Toast.makeText(activity, "Невірний логін або пароль!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLoginState(username: String) {
        val sessionPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sessionPref.edit()) {
            putBoolean("isLoggedIn", true)
            putString("current_user", username) // Зберігаємо ім'я користувача
            apply()
        }
    }

    private fun navigateToRegister() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, RegistrationFragment())
            .addToBackStack(null)
            .commit()
    }
}
