package com.example.firstprog

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private var photoUri: Uri? = null
    private val sharedPrefs by lazy { getSharedPreferences("photo_prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageViewPhoto)

        // Завантаження збереженого зображення
        loadSavedImage()

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoUri?.let {
                    imageView.setImageURI(it)
                    saveImageUri(it.toString())
                } ?: Toast.makeText(this, "Помилка отримання фото", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Фото не зроблено", Toast.LENGTH_SHORT).show()
            }
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let {
                    // Отримуємо дозвіл на постійний доступ до файлу
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    imageView.setImageURI(it)
                    saveImageUri(it.toString())
                } ?: Toast.makeText(this, "Помилка вибору фото", Toast.LENGTH_SHORT).show()
            }
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                takePhoto()
            } else {
                Toast.makeText(this, "Доступ до камери заборонений", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener {
            checkPermissionsAndTakePhoto()
        }

        findViewById<Button>(R.id.btnPickPhoto).setOnClickListener {
            pickImageFromGallery()
        }
    }

    private fun checkPermissionsAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun takePhoto() {
        val photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)

        Log.d("PhotoURI", "photoUri = $photoUri")

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            takePictureLauncher.launch(takePictureIntent)
        } else {
            Toast.makeText(this, "Камера недоступна", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImageFromGallery() {
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickImageLauncher.launch(pickIntent)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            Log.d("FileCreation", "Файл створено: ${absolutePath}")
        }
    }

    private fun saveImageUri(uri: String) {
        sharedPrefs.edit().putString("saved_image_uri", uri).apply()
    }

    private fun loadSavedImage() {
        val savedUri = sharedPrefs.getString("saved_image_uri", null)
        savedUri?.let {
            val uri = Uri.parse(it)
            try {
                contentResolver.openInputStream(uri)?.close() // Перевіряємо доступність файлу
                imageView.setImageURI(uri)
            } catch (e: Exception) {
                Log.e("LoadImage", "Файл недоступний або видалений")
            }
        }
    }
}
