package com.example.log_reg

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private lateinit var deleteAccountButton: Button
    private lateinit var logoutButton: Button
    private lateinit var avatarImageView: ImageView
    private lateinit var editAvatarButton: Button
    private lateinit var saveProfileButton: Button
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextBirthDate: EditText
    private lateinit var editTextAbout: EditText

    private lateinit var sharedPreferences: SharedPreferences
    private var tempAvatarUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            Log.d("ProfileFragment", "📷 Обране фото: $uri")
            tempAvatarUri = uri
            avatarImageView.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deleteAccountButton = view.findViewById(R.id.btn_delete_account)
        logoutButton = view.findViewById(R.id.btn_logout)
        avatarImageView = view.findViewById(R.id.img_avatar)
        editAvatarButton = view.findViewById(R.id.btn_edit_avatar)
        saveProfileButton = view.findViewById(R.id.btn_save_profile)
        editTextName = view.findViewById(R.id.editTextName)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextBirthDate = view.findViewById(R.id.editTextBirthDate)
        editTextAbout = view.findViewById(R.id.editTextAbout)

        sharedPreferences = requireActivity().getSharedPreferences("Users", Context.MODE_PRIVATE)
        loadUserProfile()

        logoutButton.setOnClickListener {
            (activity as? MainActivity)?.logoutUser()
        }

        editAvatarButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveProfileButton.setOnClickListener {
            saveUserProfile()
        }

        deleteAccountButton.setOnClickListener {
            confirmDeleteAccount()
        }
    }

    private fun loadUserProfile() {
        val sessionPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = sessionPref.getString("current_user", null)

        if (username == null) {
            Log.e("ProfileFragment", "❌ Користувач не знайдений!")
            return
        }

        val avatarUriString = sharedPreferences.getString("$username-avatar", null)
        if (!avatarUriString.isNullOrEmpty()) {
            val savedUri = Uri.parse(avatarUriString)
            val file = File(savedUri.path ?: "")

            if (file.exists()) {
                avatarImageView.setImageURI(savedUri)
                Log.d("ProfileFragment", "✅ Фото завантажено: $savedUri")
            } else {
                Log.e("ProfileFragment", "❌ Файл не знайдено: $savedUri")
                avatarImageView.setImageResource(R.drawable.ic_avatar_placeholder)
            }
        } else {
            avatarImageView.setImageResource(R.drawable.ic_avatar_placeholder)
        }

        editTextName.setText(sharedPreferences.getString("$username-name", ""))
        editTextEmail.setText(sharedPreferences.getString("$username-email", ""))
        editTextBirthDate.setText(sharedPreferences.getString("$username-birthDate", ""))
        editTextAbout.setText(sharedPreferences.getString("$username-about", ""))
    }

    private fun saveUserProfile() {
        val sessionPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = sessionPref.getString("current_user", null)

        if (username == null) {
            Toast.makeText(requireContext(), "Помилка: користувач не знайдений!", Toast.LENGTH_SHORT).show()
            return
        }

        val name = editTextName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val birthDate = editTextBirthDate.text.toString().trim()
        val about = editTextAbout.text.toString().trim()

        with(sharedPreferences.edit()) {
            putString("$username-name", name)
            putString("$username-email", email)
            putString("$username-birthDate", birthDate)
            putString("$username-about", about)
            apply()
        }

        tempAvatarUri?.let { uri ->
            val savedUri = saveImageToInternalStorage(uri)
            if (savedUri != null) {
                with(sharedPreferences.edit()) {
                    putString("$username-avatar", savedUri.toString())
                    apply()
                }
                Log.d("ProfileFragment", "✅ Фото збережено: $savedUri")
            } else {
                Log.e("ProfileFragment", "❌ Помилка збереження фото")
            }
        }

        Toast.makeText(requireContext(), "Профіль збережено!", Toast.LENGTH_SHORT).show()
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val fileName = "profile_avatar.jpg"
            val file = File(requireContext().filesDir, fileName)

            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Log.d("ProfileFragment", "✅ Фото збережено в: ${file.absolutePath}")
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "❌ Помилка збереження фото", e)
            null
        }
    }

    private fun confirmDeleteAccount() {
        AlertDialog.Builder(requireContext())
            .setTitle("Видалити акаунт?")
            .setMessage("Цю дію не можна скасувати. Ви впевнені?")
            .setPositiveButton("Так") { _, _ -> deleteAccount() }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    private fun deleteAccount() {
        val sessionPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = sessionPref.getString("current_user", null)

        if (username != null) {
            with(sharedPreferences.edit()) {
                remove(username) // Видаляємо пароль користувача
                remove("$username-name")
                remove("$username-email")
                remove("$username-birthDate")
                remove("$username-about")
                remove("$username-avatar")
                apply()
            }

            with(sessionPref.edit()) {
                remove("current_user")
                apply()
            }

            Toast.makeText(requireContext(), "Акаунт видалено", Toast.LENGTH_SHORT).show()

            // Викликаємо logoutUser(), який вже приховує панель меню
            (activity as? MainActivity)?.logoutUser()
        } else {
            Toast.makeText(requireContext(), "Помилка: користувач не знайдений!", Toast.LENGTH_SHORT).show()
        }
    }
}
