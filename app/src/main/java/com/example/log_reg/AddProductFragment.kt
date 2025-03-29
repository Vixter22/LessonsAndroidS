package com.example.log_reg.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.log_reg.R
import com.example.log_reg.data.AppDatabase
import com.example.log_reg.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddProductFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var etStock: EditText
    private lateinit var ivProductImage: ImageView
    private lateinit var btnAddProduct: Button
    private lateinit var ivBackAddProduct: ImageView

    // Зберігаємо URI вибраного зображення (як рядок)
    private var selectedImageUri: String = ""

    private lateinit var database: AppDatabase

    // API для вибору зображення з галереї
    private val getImageContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it.toString()
            ivProductImage.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_product_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Ініціалізація елементів форми
        ivBackAddProduct = view.findViewById(R.id.ivBackAddProduct)
        etName = view.findViewById(R.id.etProductName)
        etDescription = view.findViewById(R.id.etProductDescription)
        etPrice = view.findViewById(R.id.etProductPrice)
        etStock = view.findViewById(R.id.etProductStock)
        ivProductImage = view.findViewById(R.id.ivProductImage)
        btnAddProduct = view.findViewById(R.id.btnAddProduct)

        database = AppDatabase.getInstance(requireContext())

        // Обробка кліка для повернення назад
        ivBackAddProduct.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Відкриття галереї для вибору зображення
        ivProductImage.setOnClickListener {
            getImageContent.launch("image/*")
        }

        btnAddProduct.setOnClickListener {
            val name = etName.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val stock = etStock.text.toString().toIntOrNull() ?: 0

            if (name.isEmpty() || description.isEmpty() || price <= 0 || stock < 0 || selectedImageUri.isEmpty()){
                Toast.makeText(requireContext(), "Будь ласка, заповніть всі поля та виберіть зображення", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val product = Product(
                name = name,
                description = description,
                price = price,
                image = selectedImageUri,
                stock = stock
            )

            lifecycleScope.launch(Dispatchers.IO) {
                database.productDao().insert(product)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Товар додано", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }
}
