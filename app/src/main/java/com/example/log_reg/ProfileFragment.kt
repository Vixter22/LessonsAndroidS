package com.example.log_reg

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.log_reg.data.AppDatabase
import com.example.log_reg.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private lateinit var deleteAccountButton: Button
    private lateinit var exitImageView: ImageView // Іконка виходу
    private lateinit var avatarImageView: ImageView
    private lateinit var editAvatarButton: Button
    private lateinit var saveProfileButton: Button
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextBirthDate: EditText
    private lateinit var editTextAbout: EditText

    private lateinit var database: AppDatabase
    private var tempAvatarUri: Uri? = null
    private var currentUsername: String? = null

    private var currentUser: User? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                tempAvatarUri = it
                avatarImageView.setImageURI(it)
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

        // Ініціалізація бази даних
        database = AppDatabase.getInstance(requireContext())

        deleteAccountButton = view.findViewById(R.id.btn_delete_account)
        exitImageView = view.findViewById(R.id.iv_exit) // ініціалізація іконки виходу
        avatarImageView = view.findViewById(R.id.img_avatar)
        editAvatarButton = view.findViewById(R.id.btn_edit_avatar)
        saveProfileButton = view.findViewById(R.id.btn_save_profile)
        editTextName = view.findViewById(R.id.editTextName)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextBirthDate = view.findViewById(R.id.editTextBirthDate)
        editTextAbout = view.findViewById(R.id.editTextAbout)

        // Отримуємо username із SharedPreferences
        val sessionPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        currentUsername = sessionPref.getString("current_user", null)

        if (currentUsername == null) {
            Toast.makeText(requireContext(), "Помилка: користувач не знайдений!", Toast.LENGTH_SHORT).show()
            return
        }

        // Завантаження даних користувача
        loadUserProfile()

        // Обробка кліку на іконку виходу з підтвердженням
        exitImageView.setOnClickListener {
            showLogoutConfirmationDialog()
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
        database.userDao().getUserProfile(currentUsername!!).observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUser = user
                editTextName.setText(user.name)
                editTextEmail.setText(user.email)
                editTextBirthDate.setText(user.birthDate)
                editTextAbout.setText(user.about)

                val avatarUri = user.avatar?.let { Uri.parse(it) }
                if (avatarUri != null) {
                    avatarImageView.setImageURI(avatarUri)
                } else {
                    avatarImageView.setImageResource(R.drawable.ic_avatar_placeholder)
                }
            }
        }
    }

    private fun saveUserProfile() {
        val name = editTextName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val birthDate = editTextBirthDate.text.toString().trim()
        val about = editTextAbout.text.toString().trim()

        // Зберігаємо нову аватарку, якщо обрано
        val photoPath = tempAvatarUri?.let { saveImageToInternalStorage(it) }

        // Оновлюємо дані користувача
        val userToUpdate = currentUser?.copy(
            name = name,
            email = email,
            birthDate = birthDate,
            about = about,
            avatar = photoPath ?: currentUser?.avatar
        ) ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            database.userDao().updateUserProfile(userToUpdate)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Профіль збережено!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val file = File(requireContext().filesDir, "profile_${currentUsername}.jpg")
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Помилка збереження фото", e)
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
         lifecycleScope.launch(Dispatchers.IO) {
             val rowsDeleted = database.userDao().deleteUser(currentUsername!!)
             withContext(Dispatchers.Main) {
                 if (rowsDeleted > 0) {
                     Toast.makeText(requireContext(), "Акаунт видалено", Toast.LENGTH_SHORT).show()
                     (activity as? MainActivity)?.logoutUser()
                 } else {
                     Toast.makeText(requireContext(), "Помилка видалення акаунта!", Toast.LENGTH_SHORT).show()
                 }
             }
         }
     }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Вихід")
            .setMessage("Ви впевнені, що хочете вийти з акаунту?")
            .setPositiveButton("Так") { _, _ ->
                (activity as? MainActivity)?.logoutUser()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }
}
