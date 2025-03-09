package com.example.log_reg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.log_reg.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ініціалізація бази даних Room
        database = AppDatabase.getInstance(requireContext())

        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        buttonLogin = view.findViewById(R.id.buttonLogin)
        buttonRegister = view.findViewById(R.id.buttonRegister)

        buttonLogin.setOnClickListener { loginUser() }
        buttonRegister.setOnClickListener { navigateToRegister() }
    }

    private fun loginUser() {
        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity, "Заповніть усі поля!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            // Перевірка користувача за логіном та паролем за допомогою Flow
            val user = database.userDao().checkUser(username, password).first()
            withContext(Dispatchers.Main) {
                if (user != null) {
                    Toast.makeText(activity, "Вхід успішний!", Toast.LENGTH_SHORT).show()
                    saveLoginState(username)
                    (activity as? MainActivity)?.loginSuccess()
                } else {
                    Toast.makeText(activity, "Невірний логін або пароль!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveLoginState(username: String) {
        val sessionPref = requireActivity().getSharedPreferences(
            "UserSession", android.content.Context.MODE_PRIVATE
        )
        with(sessionPref.edit()) {
            putBoolean("isLoggedIn", true)
            putString("current_user", username)
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
