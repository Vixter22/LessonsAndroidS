package com.example.pr_pict_and_navigation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class PhotoFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var buttonTakePhoto: Button
    private lateinit var buttonChoosePhoto: Button
    private lateinit var buttonGoToFragment2: Button
    private lateinit var buttonGoToFragment3: Button

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var photoFile: File

    companion object {
        private const val PREFS_NAME = "photo_prefs"
        private const val PREF_PHOTO_URI = "photo_uri"
        private const val CAMERA_PERMISSION_REQUEST = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_photo, container, false)

        imageView = view.findViewById(R.id.imageView)
        buttonTakePhoto = view.findViewById(R.id.button_take_photo)
        buttonChoosePhoto = view.findViewById(R.id.button_choose_photo)
        buttonGoToFragment2 = view.findViewById(R.id.button_go_to_fragment2)
        buttonGoToFragment3 = view.findViewById(R.id.button_go_to_fragment3)

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val message = arguments?.getString("source")
        message?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        buttonTakePhoto.setOnClickListener { checkCameraPermissionAndTakePhoto() }
        buttonChoosePhoto.setOnClickListener { choosePhoto() }
        buttonGoToFragment2.setOnClickListener { navigateToFragment(SecondFragment()) }
        buttonGoToFragment3.setOnClickListener { navigateToFragment(ThirdFragment()) }

        loadSavedPhoto()
        return view
    }

    private fun navigateToFragment(fragment: Fragment) {
        val bundle = Bundle()
        bundle.putString("source", "Я прийшов з фото фрагмента")
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            photoFile = File.createTempFile("photo_", ".jpg", requireActivity().getExternalFilesDir(null))
            val photoUri = FileProvider.getUriForFile(requireContext(), "com.example.pr_pict_and_navigation.fileprovider", photoFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraLauncher.launch(intent)
        } catch (e: IOException) {
            Log.e("PhotoFragment", "Помилка створення файлу", e)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageView.setImageBitmap(bitmap)
            savePhotoUri(Uri.fromFile(photoFile).toString())
        }
    }

    private fun choosePhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { selectedImageUri ->
                try {
                    val copiedUri = copyImageToInternalStorage(selectedImageUri)
                    imageView.setImageURI(copiedUri)
                    savePhotoUri(copiedUri.toString())
                } catch (e: Exception) {
                    Log.e("PhotoFragment", "Помилка копіювання зображення", e)
                }
            }
        }
    }

    private fun copyImageToInternalStorage(sourceUri: Uri): Uri {
        val inputStream: InputStream? = requireActivity().contentResolver.openInputStream(sourceUri)
        val imagesDir = File(requireActivity().filesDir, "images").apply { if (!exists()) mkdirs() }
        val destinationFile = File(imagesDir, "selected_image.jpg")
        inputStream?.use { input ->
            FileOutputStream(destinationFile).use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(destinationFile)
    }

    private fun savePhotoUri(uri: String) {
        sharedPreferences.edit().putString(PREF_PHOTO_URI, uri).apply()
    }

    private fun loadSavedPhoto() {
        sharedPreferences.getString(PREF_PHOTO_URI, null)?.let { uriString ->
            try {
                val photoUri = Uri.parse(uriString)
                val file = File(photoUri.path!!)
                if (file.exists()) {
                    imageView.setImageURI(photoUri)
                } else {
                    sharedPreferences.edit().remove(PREF_PHOTO_URI).apply()
                }
            } catch (e: Exception) {
                Log.e("PhotoFragment", "Помилка завантаження зображення", e)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhoto()
        } else {
            Toast.makeText(requireContext(), "Доступ до камери заборонено!", Toast.LENGTH_LONG).show()
        }
    }
}
